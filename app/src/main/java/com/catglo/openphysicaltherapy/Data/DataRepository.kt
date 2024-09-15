package com.catglo.openphysicaltherapy.Data

import android.content.Context
import android.net.Uri
import android.util.Log
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.hilt.android.qualifiers.ApplicationContext
import okio.Buffer
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.lang.reflect.Type
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import javax.inject.Inject

interface WorkoutRepositoryInterface {
    fun getWorkout(fileName: String): Workout?
    fun saveWorkout(workout: Workout, forPreview: Boolean=false)
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
        return workout
    }

    override fun saveWorkout(workout: Workout, forPreview: Boolean) {
        val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
        val jsonAdapter: JsonAdapter<Workout> = moshi.adapter(Workout::class.java)
        val json: String = jsonAdapter.toJson(workout)
        val outputFile = if (forPreview) {
            file("workout_preview")
        } else {
            file(workout.fileName)
        }
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
        val outputFolder = File(context.filesDir, "exercise_import")
        outputFolder.deleteRecursively()
        outputFolder.mkdirs()
        context.contentResolver.openInputStream(importZipFileUri)?.let { inputStream ->
            ZipInputStream(inputStream).use { zipInputStream ->
                var entry = zipInputStream.nextEntry
                while (entry != null) {
                    val file = File(outputFolder, entry.name)
                    zipInputStream.copyTo(FileOutputStream(file))
                    entry = zipInputStream.nextEntry
                }
            }
        }
        val fileName = "exercise_"+System.currentTimeMillis().toString()
        File(outputFolder,"exercise_preview-index.json")
            .renameTo(File(outputFolder, "${fileName}-index.json"))
        val renameFolderTo = File(File(outputFolder.parentFile,"exercises"),fileName)
        outputFolder.renameTo(renameFolderTo)

        val repo = ExerciseRepository(context)
        val exercise = repo.getExercise(fileName)
        val exerciseList = repo.getExerciseList()
        var conflictExercise:ExerciseListItem? = null
        exerciseList.forEach { exerciseListItem ->
            if (exerciseListItem.name == exercise?.name) {
                conflictExercise = exerciseListItem
            }
        }
        exercise?.let {
            repo.saveExercise(it)
            if (conflictExercise!=null){
                return ExerciseNameConflict(exercise, conflictExercise)
            }
        }
        return null
    }

    fun renameExercise(exercise: Exercise) {
        //Look through all the exercises for ones with this name
        //but with a number appended like (2)
        //Count the number appended and generate a new name with one bigger number
        val findNameNumber = Regex("${exercise.name}\\((\\d+)\\)$")
        val exerciseList = getExerciseList()
        var count = 0
        var isDuplicate = false
        exerciseList.forEach { exerciseListItem ->
            if (exerciseListItem.name == exercise.name) isDuplicate = true
            findNameNumber.find(exerciseListItem.name).let { matchResult ->
                matchResult?.groups?.get(1)?.value?.toIntOrNull()?.let { counter ->
                    if (counter > count) {
                        count = counter
                        isDuplicate = true
                    }
                }
            }
        }
        if (isDuplicate){
            count += 1
            exercise.name = "${exercise.name}($count)"

            for (i : Int in 0..exerciseList.size - 1){
                if (exerciseList[i].fileName == exercise.fileName){
                    exerciseList[i].name = exercise.name
                }
            }
            saveExerciseList(exerciseList)
            saveExercise(exercise)
        }

    }

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

    fun unzipFolder(zipFile: File, outputFolder: File) {
        ZipInputStream(FileInputStream(zipFile)).use { zipInputStream ->
            val entry = zipInputStream.nextEntry
            while (entry != null) {
                val file = File(outputFolder, entry.name)
                zipInputStream.copyTo(FileOutputStream(file))
            }
        }
    }
}