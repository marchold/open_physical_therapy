package com.example.openphysicaltherapy

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

class InstructionalSlide:ViewModel(){
    private val _text = MutableLiveData<String>("")
    val text: LiveData<String> = _text
    fun updateText(newValue: String){
        _text.value = newValue
    }
//    var audio:Boolean = false
//    var image:Boolean = false
//    var video:Boolean = false
    private val _duration = MutableLiveData<Int>(10)
    val duration:LiveData<Int> = _duration
    fun updateDuration(newValue: Int){
        _duration.value = newValue
    }

    val _countdown = MutableLiveData<Boolean>(false)
    val countdown:LiveData<Boolean> = _countdown
    fun updateCountdown(newValue: Boolean){
        _countdown.value = newValue
    }
}

class ExerciseStep:ViewModel() {
    private val _numberOfReps = MutableLiveData<Int>(0)
    val numberOfReps: LiveData<Int> = _numberOfReps
    fun updateNumberOfReps(newValue:Int){
        _numberOfReps.value = newValue
    }

    val slides = mutableStateListOf(InstructionalSlide())
    private val _slidesStateFlow = MutableStateFlow(slides)
    val slidesFlow: StateFlow<List<InstructionalSlide>> get() = _slidesStateFlow

    fun addSlides(){
        slides.add(InstructionalSlide())
    }

    fun removeSlide(slideIndex: Int) {
        slides.removeAt(slideIndex)
    }
}

class Exercise: ViewModel() {

    val steps = mutableStateListOf(ExerciseStep())
    private val _stepsStateFlow = MutableStateFlow(steps)
    val stepsFlow: StateFlow<List<ExerciseStep>> get() = _stepsStateFlow
    fun addStep(){
        steps.add(ExerciseStep())
    }

    private val _name = MutableLiveData<String>("")
    val name: LiveData<String> = _name
    fun updateName(newName:String){
        _name.value = newName
    }

    fun save(context: Context){
        val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
        val jsonAdapter: JsonAdapter<Exercise> = moshi.adapter(Exercise::class.java)
        val json: String = jsonAdapter.toJson(this)
        FileOutputStream(file(context, name.value!!)).use {
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

