package com.example.openphysicaltherapy

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
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
    private val _numberOfReps = MutableLiveData<Int>(1)
    val numberOfReps: LiveData<Int> = _numberOfReps
    fun updateNumberOfReps(newValue:Int){
        _numberOfReps.value = newValue
    }

    val slides = mutableStateListOf(InstructionalSlide())
    //private val _slidesStateFlow = MutableStateFlow(slides)
    //val slidesFlow: StateFlow<List<InstructionalSlide>> get() = _slidesStateFlow

    fun addSlides(){
        slides.add(InstructionalSlide())
    }

    fun removeSlide(slideIndex: Int) {
        slides.removeAt(slideIndex)
    }
}

class Exercise: ViewModel() {

    val steps = mutableStateListOf(ExerciseStep())
    //private val _stepsStateFlow = MutableStateFlow(steps)
    //val stepsFlow: StateFlow<List<ExerciseStep>> get() = _stepsStateFlow
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
        val outputFile = file(context, name.value!!)
        if (!outputFile.exists()){
            ExerciseListItem.addExercise(name.value!!)
        }
        FileOutputStream(outputFile).use {
            it.write(json.toByteArray())
        }

    }

    fun load(context:Context, name:String) {
        val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
        val jsonAdapter: JsonAdapter<Exercise> = moshi.adapter(Exercise::class.java)
        val json = FileInputStream(file(context, name))
            .bufferedReader()
            .use {
                it.readText()
            }
        val exercise = jsonAdapter.fromJson(json)
        this._name.value = name
        this.steps.addAll(exercise?.steps!!)
    }

    companion object {

        private fun file(context: Context, name: String):File{
            val path = File(File(context.filesDir, "exercises"), name)
            if (!path.exists()) path.mkdirs()
            return File(path, "${name}-index.json")
        }

    }
}

data class ExerciseListItem(val name: String){
    companion object {
        private val listItems = mutableStateListOf<ExerciseListItem>()
        fun listOfExercises(context: Context): MutableList<ExerciseListItem> {
            val path = File(context.filesDir, "exercises")
            if (!path.exists()) return mutableListOf()
            val exerciseFiles = path.listFiles()
            listItems.clear()
            exerciseFiles?.forEach {
                val name = it.name
                listItems.add(ExerciseListItem(name))
            }
            return listItems
        }
        fun deleteExercise(context: Context, name:String){
            var path = File(context.filesDir, "exercises")
            path = File(path,name)
            if (!path.exists()) return
            path.deleteRecursively()
            listItems.removeIf { it.name == name }
        }
        fun addExercise(name:String){
            listItems.add(ExerciseListItem(name))
        }
    }
}

//
//private val _ExerciseList_itemList = mutableStateListOf<ExerciseListItem>()
//val exerciseListItemList: List<ExerciseListItem> = _ExerciseList_itemList
//
////fun updateItems() {
////    viewModelScope.launch {
////        _itemList.addAll(itemRepository.getItems())
////    }
////}
//
//fun updateOneItem(newVal:Int){
//    val index = _ExerciseList_itemList.indexOf(item)
//    _ExerciseList_itemList[index] = _ExerciseList_itemList[index].copy(weight = newVal)
//}
//
