package com.catglo.openphysicaltherapy.Data

import android.content.Context
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.lang.reflect.Type
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
        val json = FileInputStream(file( fileName))
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
        if (!forPreview) {
            FileOutputStream(outputFile).use {
                it.write(json.toByteArray())
            }
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
        val jsonAdapter: JsonAdapter<List<WorkoutListItem>> = moshi.adapter<List<WorkoutListItem>>(type)
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

    override fun saveExercise(exercise: Exercise, forPreview: Boolean) {
        val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
        val jsonAdapter: JsonAdapter<Exercise> = moshi.adapter(Exercise::class.java)
        val json: String = jsonAdapter.toJson(exercise)
        var outputFile : File
        if (forPreview) {
            outputFile = path("exercise_preview")
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

}