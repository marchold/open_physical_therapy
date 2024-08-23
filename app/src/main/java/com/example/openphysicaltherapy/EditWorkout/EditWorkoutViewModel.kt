package com.example.openphysicaltherapy.EditWorkout

import android.content.Context
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.openphysicaltherapy.Data.Workout
import com.example.openphysicaltherapy.Data.ExerciseListItem
import com.example.openphysicaltherapy.Data.WorkoutListItem
import com.example.openphysicaltherapy.Data.WorkoutRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

@HiltViewModel
class EditWorkoutViewModel @Inject constructor(private val repo: WorkoutRepository, @ApplicationContext val context: Context) : ViewModel() {
    private var workout = Workout("", listOf())

    var originalName: String? = null
        private set

    private var _exercises = workout.exercises.toMutableStateList()
    fun getExercises():List<ExerciseListItem>{
        return _exercises
    }
    fun addExercise(item: ExerciseListItem){
        _exercises.add(item)
        workout.exercises = _exercises
    }
    fun moveExercise(from:Int, to:Int){
        val fromItem = _exercises[from]
        _exercises.removeAt(from)
        _exercises.add(to, fromItem)
        workout.exercises = _exercises
    }

    private val _name = MutableLiveData<String>("")
    val name: LiveData<String> = _name
    fun updateName(newName:String){
        _name.value = newName
        workout.name = newName
    }

    fun save(){
        originalName?.let { originalName ->
            if (originalName != name.value) {
                repo.deleteWorkout(originalName)
            }
        }
        repo.saveWorkout(workout)
    }

    fun load(name:String) {
        repo.getWorkout(name)?.let {
            workout = it
            originalName = name
            _name.value = name
            _exercises.clear()
            _exercises.addAll(workout.exercises)
        }
    }

    private var listOfExercises : List<WorkoutListItem>? = null
    fun isWorkoutNameUnique(name:String):Boolean{
        if (listOfExercises == null){
            listOfExercises = WorkoutRepository(context).getWorkoutList()
        }
        if (name == originalName) return true
        listOfExercises?.forEach {
            if (it.name == name) {
                return false
            }
        }
        return true
    }

}