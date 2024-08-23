package com.example.openphysicaltherapy.EditExercise

import android.content.Context
import androidx.compose.runtime.toMutableStateList
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.openphysicaltherapy.Data.Exercise
import com.example.openphysicaltherapy.Data.Workout
import com.example.openphysicaltherapy.Data.ExerciseListItem
import com.example.openphysicaltherapy.Data.ExerciseRepository
import com.example.openphysicaltherapy.Data.ExerciseStep
import com.example.openphysicaltherapy.NavigationItem
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

@HiltViewModel
class EditExerciseViewModel @Inject constructor(private val repo: ExerciseRepository, @ApplicationContext val context: Context) : ViewModel() {
    private var exercise = Exercise("")

    var originalName: String? = null
        private set

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

    fun save(){
        originalName?.let { originalName ->
            if (originalName != name.value) {
                repo.deleteExercise(originalName)
            }
        }
        repo.saveExercise(exercise)
    }

    fun load(name:String) {
        repo.getExercise(name)?.let {
            exercise = it
            originalName = name
            _name.value = name
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

}