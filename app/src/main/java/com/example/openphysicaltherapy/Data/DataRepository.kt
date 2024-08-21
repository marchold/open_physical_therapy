package com.example.openphysicaltherapy.Data

import android.content.Context
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import javax.inject.Inject

interface ExerciseRepositoryInterface {
    fun getExercise(context: Context, name: String): Exercise?
    fun saveExercise(context: Context, exercise: Exercise)
    fun getExerciseList(context: Context):List<ExerciseListItem>
    fun deleteExercise(context: Context, name: String)
}

class ExerciseRepository @Inject constructor() : ExerciseRepositoryInterface {

    private fun file(context: Context, name: String): File {
        val path = File(File(context.filesDir, "exercises"), name)
        if (!path.exists()) path.mkdirs()
        return File(path, "${name}-index.json")
    }

    override fun getExercise(context: Context, name: String): Exercise? {
        val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
        val jsonAdapter: JsonAdapter<Exercise> = moshi.adapter(Exercise::class.java)
        val json = FileInputStream(file(context, name))
            .bufferedReader()
            .use {
                it.readText()
            }
        val exercise = jsonAdapter.fromJson(json)
        return exercise
    }

    override fun saveExercise(context: Context, exercise: Exercise) {
        val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
        val jsonAdapter: JsonAdapter<Exercise> = moshi.adapter(Exercise::class.java)
        val json: String = jsonAdapter.toJson(exercise)
        val outputFile = file(context, exercise.name)
//        if (!outputFile.exists()){
//            addExercise(context,exercise.name)
//        }
        FileOutputStream(outputFile).use {
            it.write(json.toByteArray())
        }
    }

    override fun getExerciseList(context: Context) : List<ExerciseListItem> {
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

    override fun deleteExercise(context: Context, name: String) {
        var path = File(context.filesDir, "exercises")
        path = File(path,name)
        if (!path.exists()) return
        path.deleteRecursively()
//        listItems.removeIf { it.name == name }
    }

}