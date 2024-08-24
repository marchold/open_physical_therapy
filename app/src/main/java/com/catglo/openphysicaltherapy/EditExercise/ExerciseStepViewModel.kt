package com.catglo.openphysicaltherapy.EditExercise

import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.toMutableStateList
import androidx.lifecycle.ViewModel
import com.catglo.openphysicaltherapy.Data.ExerciseStep
import com.catglo.openphysicaltherapy.Data.InstructionalSlide

class ExerciseStepViewModel(private val exerciseStep: ExerciseStep) : ViewModel() {

    private var _slides = exerciseStep.slides.toMutableStateList()
    fun getSlides():List<InstructionalSlide>{
        return _slides
    }
    fun addSlide(){
        _slides.add(InstructionalSlide())
        exerciseStep.slides = _slides
    }
    fun removeSlide(slideIndex: Int) {
        _slides.removeAt(slideIndex)
        exerciseStep.slides = _slides
    }

    var numberOfReps = mutableIntStateOf(exerciseStep.numberOfReps)
        private set

    fun updateNumberOfReps(newNumberOfReps:Int){
        numberOfReps.value = newNumberOfReps
        exerciseStep.numberOfReps = newNumberOfReps
    }
}