package com.example.openphysicaltherapy

import android.content.Context
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
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

data class InstructionalSlide(var text: String = "",
                              var duration:Int=3,
                              var countdown: Boolean = false)

data class ExerciseStep(var numberOfReps: Int = 1,
                        var slides: MutableList<InstructionalSlide> = mutableListOf(  InstructionalSlide() ))

data class Exercise(var name: String,
                    var steps: MutableList<ExerciseStep> = mutableListOf(ExerciseStep()))

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
