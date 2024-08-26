package com.catglo.openphysicaltherapy.EditExercise

import android.content.Context
import androidx.compose.runtime.toMutableStateList
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.catglo.openphysicaltherapy.Data.Exercise
import com.catglo.openphysicaltherapy.Data.ExerciseListItem
import com.catglo.openphysicaltherapy.Data.ExerciseRepository
import com.catglo.openphysicaltherapy.Data.ExerciseStep
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

@HiltViewModel
open class EditExerciseViewModel @Inject constructor(private val repo: ExerciseRepository, @ApplicationContext val context: Context) : ViewModel() {
    private var exercise = Exercise("")

    private var _exerciseSteps = exercise.steps.toMutableStateList()
    fun getExerciseSteps():List<ExerciseStep>{
        return _exerciseSteps
    }
    fun addStep(){
        _exerciseSteps.add(ExerciseStep(1))
        exercise.steps = _exerciseSteps
    }

    private val _name = MutableLiveData<String>(exercise.name)
    val name: LiveData<String> = _name
    fun updateName(newName:String){
        _name.value = newName
        exercise.name = newName
    }

    fun save(){
        repo.saveExercise(exercise)
    }

    fun load(fileName:String) {
        repo.getExercise(fileName)?.let {
            exercise = it
            _name.value = it.name
            _exerciseSteps = exercise.steps.toMutableStateList()
        }
    }

    private var listOfExercises : List<ExerciseListItem>? = null
    fun isExerciseNameUnique(name:String):Boolean{
        if (listOfExercises == null){
            listOfExercises = ExerciseRepository(context).getExerciseList()
        }
        listOfExercises?.forEach {
            if (it.name == name) {
                return false
            }
        }
        return true
    }

    fun fileName(): String {
        return exercise.fileName
    }

    fun saveForPreview() {
        repo.saveExercise(exercise, true)
    }

}