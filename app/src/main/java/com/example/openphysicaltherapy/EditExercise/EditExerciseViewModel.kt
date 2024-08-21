package com.example.openphysicaltherapy.EditExercise

import android.content.Context
import androidx.compose.runtime.toMutableStateList
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.openphysicaltherapy.Data.Exercise
import com.example.openphysicaltherapy.Data.ExerciseRepository
import com.example.openphysicaltherapy.Data.ExerciseStep
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class EditExerciseViewModel @Inject constructor(private val repo: ExerciseRepository) : ViewModel() {
    private var exercise = Exercise("")

    private var _exerciseSteps = exercise.steps.toMutableStateList()
    fun getExerciseSteps():List<ExerciseStep>{
        return _exerciseSteps
    }
    fun addStep(){
        _exerciseSteps.add(ExerciseStep(1))
        exercise.steps = _exerciseSteps
    }

    private val _name = MutableLiveData<String>("")
    val name: LiveData<String> = _name
    fun updateName(newName:String){
        _name.value = newName
        exercise.name = newName
    }

    fun save(context: Context){
        repo.saveExercise(context, exercise)
    }

    fun load(context: Context, name:String) {
        val exercise = repo.getExercise(context, name)
        this._name.value = name
        exercise?.name = name
        exercise?.steps?.let {
            _exerciseSteps = it.toMutableStateList()
            this.exercise.steps = it
        }
    }
}