package com.catglo.openphysicaltherapy.Data

import android.content.Context
import android.net.Uri
import android.util.Log
import com.catglo.openphysicaltherapy.findNextNameNumber
import com.catglo.openphysicaltherapy.toValidFileName
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.FilenameFilter
import java.lang.reflect.Type
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import javax.inject.Inject


interface WorkoutRepositoryInterface {
    fun getWorkout(fileName: String): Workout?
    fun saveWorkout(workout: Workout, forPreview: Boolean=false):File
    fun getWorkoutList():List<WorkoutListItem>
    fun deleteWorkout(fileName: String)
}

class WorkoutRepository @Inject constructor(@ApplicationContext val context: Context):WorkoutRepositoryInterface {
    private fun file(name: String): File {
        val path = File(File(context.filesDir, "workouts"), name)
        if (!path.exists()) path.mkdirs()
        return File(path, "${name}-index.json")
    }

    override fun getWorkout(fileName: String): Workout? {
        val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
        val jsonAdapter: JsonAdapter<Workout> = moshi.adapter(Workout::class.java)
        val file = file( fileName)
        if (!file.exists()) return null
        val json = FileInputStream(file)
            .bufferedReader()
            .use {
                it.readText()
            }
        val workout = jsonAdapter.fromJson(json)
        workout?.exercises?.forEach { Log.i("workout","${it.name} ${it.fileName}") }

        return workout
    }

    override fun saveWorkout(workout: Workout, forPreview: Boolean) : File {
        val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
        val jsonAdapter: JsonAdapter<Workout> = moshi.adapter(Workout::class.java)
        val json: String = jsonAdapter.toJson(workout)
        val outputFile = if (forPreview) {
            file("workout_preview")
        } else {
            file(workout.fileName)
        }
        outputFile.deleteRecursively()
        FileOutputStream(outputFile).use {
            it.write(json.toByteArray())
        }
        if (!forPreview) {
            val list = getWorkoutList().toMutableList()
            var fileNameExists = false
            for (i in 0 until list.size) {
                if (list[i].fileName == workout.fileName) {
                    fileNameExists = true
                    list[i].name = workout.name
                }
            }
            if (!fileNameExists) {
                list.add(WorkoutListItem(workout.name, workout.fileName))
            }
            saveWorkoutList(list)
        }
        return outputFile
    }

    override fun getWorkoutList() : List<WorkoutListItem> {
        val file = File(context.filesDir, "workout.list")
        if (!file.exists()) return listOf<WorkoutListItem>()
        val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
        val type: Type = Types.newParameterizedType(
            MutableList::class.java,
            WorkoutListItem::class.java
        )
        val jsonAdapter: JsonAdapter<List<WorkoutListItem>> = moshi.adapter<List<WorkoutListItem>>(type)
        val json = FileInputStream(file)
            .bufferedReader()
            .use {
                it.readText()
            }
        val exerciseList = jsonAdapter.fromJson(json)
        return exerciseList ?: listOf()
    }

    private fun saveWorkoutList(list: List<WorkoutListItem>) {
        val file = File(context.filesDir, "workout.list")
        val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
        val type: Type = Types.newParameterizedType(
            MutableList::class.java,
            WorkoutListItem::class.java
        )
        val jsonAdapter: JsonAdapter<List<WorkoutListItem>> = moshi.adapter(type)
        val json: String = jsonAdapter.toJson(list)
        FileOutputStream(file).use {
            it.write(json.toByteArray())
        }
    }

    override fun deleteWorkout(fileName: String) {
        var path = File(context.filesDir, "workouts")
        path = File(path,fileName)
        if (!path.exists()) return
        path.deleteRecursively()
        val list = getWorkoutList().toMutableList()
        val filteredList = list.filter { it.fileName != fileName }
        saveWorkoutList(filteredList)
    }

    fun importWorkout(importZipFileUri: Uri): WorkoutNameConflict? {
        val outputFolder = File(context.cacheDir, "workout_import")
        outputFolder.deleteRecursively()
        outputFolder.mkdirs()
        context.contentResolver.openInputStream(importZipFileUri)?.let { inputStream ->
            ZipInputStream(inputStream).use { zipInputStream ->
                var entry = zipInputStream.nextEntry
                while (entry != null) {
                    val file = File(outputFolder, entry.name)
                    Log.i("Workout Import","Unzip  ${entry.name}")
                    if (zipInputStream.copyTo(FileOutputStream(file))==0L){
                        Log.e("Workout Import","Failed to copy unzipped file ${entry.name}")
                    }
                    entry = zipInputStream.nextEntry
                }
            }
        }

        val oldJsonFileName = outputFolder.listFiles(FilenameFilter { _, name -> name.endsWith(".json") })?.first()
        val newFileName = "workout_"+System.currentTimeMillis().toString()
        val newJsonFileName =  file(newFileName)
        oldJsonFileName?.renameTo(newJsonFileName)

        val workout = getWorkout(newFileName)
        val workoutList = getWorkoutList()
        var conflictWorkout:WorkoutListItem? = null
        workoutList.forEach { workoutListItem ->
            if (workoutListItem.name.trimCounter() == workout?.name?.trimCounter()) {
                conflictWorkout = workoutListItem
            }
        }

        val exerciseRepo = ExerciseRepository(context)
        val exerciseNameConflicts = mutableListOf<ExerciseNameConflict>()

        workout?.exercises?.forEach { exercise ->
            val exerciseZipFile = File(outputFolder, exercise.fileName + ".zip")
            val importedExercise = exerciseRepo.importExercise(Uri.fromFile(exerciseZipFile))
            importedExercise?.newExercise?.fileName?.let {
                exercise.fileName = it
            }
            if (importedExercise?.existingNameConflictExercise!=null) exerciseNameConflicts.add(importedExercise)
        }

        workout?.let {
            workout.fileName = newFileName
            saveWorkout(it)
            return WorkoutNameConflict(workout, conflictWorkout, exerciseNameConflicts)
        }
        return null
    }

    fun exportWorkout(workout: Workout): File {
        val workoutJsonFile = saveWorkout(workout, true)
        val exportZipFileName = workout.name.toValidFileName()
        val exportZipFile = File(context.filesDir,exportZipFileName)

        val stagingFolder = File(context.cacheDir,"workout_export")
        stagingFolder.deleteRecursively()
        stagingFolder.mkdir()

        //Copy all of this workouts exercises as zip files to this folder
        val exerciseRepo = ExerciseRepository(context)
        workout.exercises.forEach { exercise ->
            exerciseRepo.getExercise(exercise.fileName)?.let {
                val exportedExerciseZipFile = exerciseRepo.exportExercise(it)
                if (!exportedExerciseZipFile.renameTo(File(stagingFolder,it.fileName + ".zip"))){
                    Log.e("WorkoutRepo","Failed to rename file")
                }
            }
        }

        workoutJsonFile.copyTo(File(stagingFolder, "$exportZipFileName.json"))
        zipFolder(stagingFolder,exportZipFile)
        return exportZipFile
    }

    fun renameWorkout(workout: Workout) {
        val workoutList = getWorkoutList()
        val newName = findNextNameNumber(workout.name, { workoutList.size }, { workoutList[it].name })
        if (newName != workout.name) {
            workout.name = newName
            for (i in 0..<workoutList.size){
                if (workoutList[i].fileName == workout.fileName){
                    workoutList[i].name = workout.name
                }
            }
            saveWorkoutList(workoutList)
            saveWorkout(workout)
        }
    }
}

interface ExerciseRepositoryInterface {
    fun getExercise(fileName: String): Exercise?
    fun saveExercise(exercise: Exercise, forPreview: Boolean = false)
    fun getExerciseList():List<ExerciseListItem>
    fun deleteExercise(fileName: String)
}

class ExerciseRepository @Inject constructor(@ApplicationContext val context: Context) : ExerciseRepositoryInterface {

    fun previewPath():File{
        return path("exercise_preview")
    }

    private fun path(fileName: String): File {
        val path = File(File(context.filesDir, "exercises"), fileName)
        if (!path.exists()) path.mkdirs()
        return path
    }

    private fun file(fileName: String): File {
        return File(path(fileName), "${fileName}-index.json")
    }

    override fun getExercise(fileName: String): Exercise? {
        val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
        val jsonAdapter: JsonAdapter<Exercise> = moshi.adapter(Exercise::class.java)
        val file = file(fileName)
        if (!file.exists()) {
            Log.e("ExerciseRepo", "File not found: $fileName")
            return null
        }
        val json = FileInputStream(file)
            .bufferedReader()
            .use {
                it.readText()
            }
        val exercise = jsonAdapter.fromJson(json)
        exercise?.fileName = fileName
        return exercise
    }

    fun cleanup(exercise: Exercise){
        val usedFiles = mutableListOf<String>()
        exercise.steps.forEach { step ->
            step.slides.forEach { slide ->
                slide.videoFileName?.let { usedFiles.add(it) }
                slide.imageFileName?.let { usedFiles.add(it) }
                slide.audioFileName?.let { usedFiles.add(it) }
            }
        }
        usedFiles.add(exercise.fileName)
        path(exercise.fileName).listFiles()?.forEach { file ->
            if (!usedFiles.contains(file.name)) {
                file.delete()
            }
        }
    }

    override fun saveExercise(exercise: Exercise, forPreview: Boolean) {
        val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
        val jsonAdapter: JsonAdapter<Exercise> = moshi.adapter(Exercise::class.java).indent("    ")
        val json: String = jsonAdapter.toJson(exercise)
        var outputFile : File
        if (forPreview) {
            outputFile = previewPath()
            if (outputFile.exists()) outputFile.deleteRecursively()
            outputFile.mkdirs()
            path(exercise.fileName).copyRecursively(outputFile)
            outputFile = File(outputFile, "exercise_preview-index.json")
        } else {
            outputFile = file( exercise.fileName)
            val list = getExerciseList().toMutableList()
            var fileNameExists = false
            for (i in 0 until list.size) {
                if (list[i].fileName == exercise.fileName) {
                    fileNameExists = true
                    list[i].name = exercise.name
                }
            }
            if (!fileNameExists) {
                list.add(ExerciseListItem(exercise.name, exercise.fileName))
            }
            saveExerciseList(list)
        }
        FileOutputStream(outputFile).use {
            it.write(json.toByteArray())
        }
    }

    override fun getExerciseList() : List<ExerciseListItem> {
        val file = File(context.filesDir, "exercise.list")
        if (!file.exists()) return listOf<ExerciseListItem>()
        val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
        val type: Type = Types.newParameterizedType(
            MutableList::class.java,
            ExerciseListItem::class.java
        )
        val jsonAdapter: JsonAdapter<List<ExerciseListItem>> = moshi.adapter<List<ExerciseListItem>>(type)
        val json = FileInputStream(file)
            .bufferedReader()
            .use {
                it.readText()
            }
        val exerciseList = jsonAdapter.fromJson(json)
        return exerciseList ?: listOf()
    }

    private fun saveExerciseList(list: List<ExerciseListItem>) {
        val file = File(context.filesDir, "exercise.list")
        val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
        val type: Type = Types.newParameterizedType(
            MutableList::class.java,
            ExerciseListItem::class.java
        )
        val jsonAdapter: JsonAdapter<List<ExerciseListItem>> = moshi.adapter<List<ExerciseListItem>>(type)
        val json: String = jsonAdapter.toJson(list)
        FileOutputStream(file).use {
            it.write(json.toByteArray())
        }
    }

    override fun deleteExercise(fileName: String) {
        var path = File(context.filesDir, "exercises")
        path = File(path,fileName)
        if (!path.exists()) return
        path.deleteRecursively()
        val list = getExerciseList().toMutableList()
        val filteredList = list.filter { it.fileName != fileName }
        saveExerciseList(filteredList)
    }

    fun importExercise(importZipFileUri: Uri): ExerciseNameConflict? {
        //First unzip everything in to a temporary folder
        val outputFolder = File(context.filesDir, "exercise_import")
        outputFolder.deleteRecursively()
        outputFolder.mkdirs()
        var unzippedFiles = 0
        context.contentResolver.openInputStream(importZipFileUri)?.let { inputStream ->
            ZipInputStream(inputStream).use { zipInputStream ->
                var entry = zipInputStream.nextEntry
                while (entry != null) {
                    val file = File(outputFolder, entry.name)
                    if (zipInputStream.copyTo(FileOutputStream(file))==0L){
                        Log.e("Exercise Import","Failed to copy unzipped file")
                    }
                    unzippedFiles++
                    entry = zipInputStream.nextEntry
                }
            }
        }
        if (unzippedFiles==0){
            Log.e("Exercise Import","No files were unzipped")
        }

        //Next we want to generate a unique name for this exercise
        //rename the exercise json file and the folder
        val fileName = "exercise_"+System.currentTimeMillis().toString()
        val renameJsonSuccess = File(outputFolder,"exercise_preview-index.json")
            .renameTo(File(outputFolder, "${fileName}-index.json"))
        val renameFolderTo = File(File(outputFolder.parentFile,"exercises"),fileName)
        renameFolderTo.parentFile?.mkdirs()
        val renameFolderSuccess = outputFolder.renameTo(renameFolderTo)

        if (!renameJsonSuccess || !renameFolderSuccess){
            Log.e("Exercise Import","Rename error")
        }

        //Finally look for name conflicts
        val exercise = getExercise(fileName)
        if (exercise==null){
            Log.e("Exercise Import","Exercise failed to load")
        }
        val exerciseList = getExerciseList()
        var conflictExercise:ExerciseListItem? = null
        exerciseList.forEach { exerciseListItem ->
            if (exerciseListItem.name.trimCounter() == exercise?.name?.trimCounter()) {
                conflictExercise = exerciseListItem
            }
        }
        exercise?.let {
            it.fileName = fileName
            saveExercise(it)
            return ExerciseNameConflict(exercise, conflictExercise)
        }
        return null
    }



    fun renameExercise(exercise: Exercise) {
        val exerciseList = getExerciseList()
        val newName = findNextNameNumber(exercise.name, { exerciseList.size }, { exerciseList[it].name })
        if (newName != exercise.name) {
            exercise.name = newName
            for (i in 0..<exerciseList.size){
                if (exerciseList[i].fileName == exercise.fileName){
                    exerciseList[i].name = exercise.name
                }
            }
            saveExerciseList(exerciseList)
            saveExercise(exercise)
        }
    }

    fun exportExercise(exercise: Exercise): File {
        cleanup(exercise)
        saveExercise(exercise, true)
        val exportZipFileName = exercise.prettyFileName()
        val exportFile = File(context.filesDir,exportZipFileName + ".zip")
        zipFolder(previewPath(),exportFile)
        return exportFile
    }
}

private fun String.trimCounter(): String {
    val findNameNumber = Regex("\\((\\d+)\\)$")
    findNameNumber.find(this)?.groups?.get(1)?.range?.start?.let {
        return this.substring(0,it-1).trim()
    }
    return this.trim()
}

fun zipFolder(inputFolder: File, outputFile: File) {
    ZipOutputStream(FileOutputStream(outputFile)).use { zipOutputStream ->
        inputFolder.walkTopDown().forEach { file ->
            val relativePath = file.relativeTo(inputFolder).path
            if (file.isFile) {
                zipOutputStream.putNextEntry(ZipEntry(relativePath))
                FileInputStream(file).use { inputStream ->
                    inputStream.copyTo(zipOutputStream)
                }
                zipOutputStream.closeEntry()
            } else if (file.isDirectory && relativePath.isNotEmpty()) {
                zipOutputStream.putNextEntry(ZipEntry("$relativePath/"))
                zipOutputStream.closeEntry()
            }
        }
    }

}

fun unzipFolder(zipFile: File, outputFolder: File) {
    ZipInputStream(FileInputStream(zipFile)).use { zipInputStream ->
        val entry = zipInputStream.nextEntry
        while (entry != null) {
            val file = File(outputFolder, entry.name)
            zipInputStream.copyTo(FileOutputStream(file))
        }
    }
}

