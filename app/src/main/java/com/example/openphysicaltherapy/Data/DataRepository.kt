package com.example.openphysicaltherapy.Data

import android.content.Context
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import javax.inject.Inject

interface WorkoutRepositoryInterface {
    fun getWorkout(name: String): Workout?
    fun saveWorkout(workout: Workout)
    fun getWorkoutList():List<WorkoutListItem>
    fun deleteWorkout(name: String)
}

class WorkoutRepository @Inject constructor(@ApplicationContext val context: Context):WorkoutRepositoryInterface {
    private fun file(name: String): File {
        val path = File(File(context.filesDir, "workouts"), name)
        if (!path.exists()) path.mkdirs()
        return File(path, "${name}-index.json")
    }

    override fun getWorkout(name: String): Workout? {
        val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
        val jsonAdapter: JsonAdapter<Workout> = moshi.adapter(Workout::class.java)
        val json = FileInputStream(file( name))
            .bufferedReader()
            .use {
                it.readText()
            }
        val workout = jsonAdapter.fromJson(json)
        return workout
    }

    override fun saveWorkout(workout: Workout) {
        val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
        val jsonAdapter: JsonAdapter<Workout> = moshi.adapter(Workout::class.java)
        val json: String = jsonAdapter.toJson(workout)
        val outputFile = file(workout.name)
        FileOutputStream(outputFile).use {
            it.write(json.toByteArray())
        }
    }

    override fun getWorkoutList(): List<WorkoutListItem> {
        val path = File(context.filesDir, "workouts")
        if (!path.exists()) return listOf<WorkoutListItem>()
        val workoutFiles = path.listFiles()
        val result = mutableListOf<WorkoutListItem>()
        workoutFiles?.forEach {
            val name = it.name
            result.add(WorkoutListItem(name))
        }
        return result
    }

    override fun deleteWorkout(name: String) {
        var path = File(context.filesDir, "workouts")
        path = File(path,name)
        if (!path.exists()) return
        path.deleteRecursively()
    }
}

interface ExerciseRepositoryInterface {
    fun getExercise(name: String): Exercise?
    fun saveExercise(exercise: Exercise)
    fun getExerciseList():List<ExerciseListItem>
    fun deleteExercise(name: String)
}

class ExerciseRepository @Inject constructor(@ApplicationContext val context: Context) : ExerciseRepositoryInterface {

    private fun file(name: String): File {
        val path = File(File(context.filesDir, "exercises"), name)
        if (!path.exists()) path.mkdirs()
        return File(path, "${name}-index.json")
    }

    override fun getExercise(name: String): Exercise? {
        val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
        val jsonAdapter: JsonAdapter<Exercise> = moshi.adapter(Exercise::class.java)
        val json = FileInputStream(file( name))
            .bufferedReader()
            .use {
                it.readText()
            }
        val exercise = jsonAdapter.fromJson(json)
        return exercise
    }

    override fun saveExercise(exercise: Exercise) {
        val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
        val jsonAdapter: JsonAdapter<Exercise> = moshi.adapter(Exercise::class.java)
        val json: String = jsonAdapter.toJson(exercise)
        val outputFile = file( exercise.name)
        FileOutputStream(outputFile).use {
            it.write(json.toByteArray())
        }
    }

    override fun getExerciseList() : List<ExerciseListItem> {
        val path = File(context.filesDir, "exercises")
        if (!path.exists()) return listOf<ExerciseListItem>()
        val exerciseFiles = path.listFiles()
        val result = mutableListOf<ExerciseListItem>()
        exerciseFiles?.forEach {
            val name = it.name
            result.add(ExerciseListItem(name))
        }
        return result
    }

    override fun deleteExercise(name: String) {
        var path = File(context.filesDir, "exercises")
        path = File(path,name)
        if (!path.exists()) return
        path.deleteRecursively()
    }

}