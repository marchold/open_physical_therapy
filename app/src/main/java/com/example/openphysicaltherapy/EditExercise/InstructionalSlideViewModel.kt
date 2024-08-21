package com.example.openphysicaltherapy.EditExercise

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.openphysicaltherapy.Data.InstructionalSlide

class InstructionalSlideViewModel(private val slide: InstructionalSlide) : ViewModel() {
    var duration = mutableIntStateOf(slide.duration)
        private set

    fun updateDuration(newDuration:Int){
        duration.value = newDuration
        slide.duration = newDuration
    }

    var text by mutableStateOf(slide.text)
        private set

    fun updateText(newText:String){
        text = newText
        slide.text = newText
    }

}