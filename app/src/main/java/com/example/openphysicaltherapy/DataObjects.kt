package com.example.openphysicaltherapy

import android.content.Context
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

class ExerciseInstruction(
    var text:String = "",
    var audio:Boolean = false,
    var image:Boolean = false,
    var video:Boolean = false,
    var duration: Int = 10, //In seconds
    var countdown:Boolean = true,
)

class ExerciseStep(
    var numberOfReps:Int,
    var instructions: MutableList<ExerciseInstruction>
)

class Exercise(
    var name: String,
    var icon:Boolean = false,
    var steps:MutableList<ExerciseStep> = emptyList<ExerciseStep>().toMutableList()
){
    fun save(context:Context){
        val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
        val jsonAdapter: JsonAdapter<Exercise> = moshi.adapter(Exercise::class.java)
        val json: String = jsonAdapter.toJson(this)
        FileOutputStream(file(context, name)).use {
            it.write(json.toByteArray())
        }
    }

    companion object {
        fun load(context:Context, name:String):Exercise? {
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
        private fun file(context: Context, name: String):File{
            val path = File(File(context.filesDir, "exercises"), name)
            if (!path.exists()) path.mkdirs()
            return File(path, "${name}-index.json")
        }
        fun listOfExercises(context: Context):MutableList<String>{
            val path = File(context.filesDir, "exercises")
            if (!path.exists()) return emptyList<String>().toMutableList()
            val exerciseFiles = path.listFiles()
            val results = emptyList<String>().toMutableList()
            exerciseFiles?.forEach {
                val name = it.name
                results.add(name)
            }
            return results
        }
    }
}

