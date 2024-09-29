package com.catglo.openphysicaltherapy.EditWorkout

import android.content.Context
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.catglo.openphysicaltherapy.Data.Workout
import com.catglo.openphysicaltherapy.Data.ExerciseListItem
import com.catglo.openphysicaltherapy.Data.WorkoutListItem
import com.catglo.openphysicaltherapy.Data.WorkoutRepository
import com.catglo.openphysicaltherapy.Data.zipFolder
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject

@HiltViewModel
class EditWorkoutViewModel @Inject constructor(private val repo: WorkoutRepository, @ApplicationContext val context: Context) : ViewModel() {
    private var workout = Workout("", listOf())

    var originalName: String? = null
        private set

    var hasBeenEdited = false

    private var _exercises = workout.exercises.toMutableStateList()
    fun getExercises():SnapshotStateList<ExerciseListItem>{
        return _exercises
    }
    fun addExercise(item: ExerciseListItem){
        _exercises.add(item)
        workout.exercises = _exercises
        hasBeenEdited = true
    }
    fun moveExercise(from:Int, to:Int){
        val fromItem = _exercises[from]
        _exercises.removeAt(from)
        _exercises.add(to, fromItem)
        workout.exercises = _exercises
        hasBeenEdited = true
    }

    fun removeExercise(index: Int) {
        _exercises.removeAt(index)
        workout.exercises = _exercises
        hasBeenEdited = true
    }

    private val _name = MutableLiveData<String>("")
    val name: LiveData<String> = _name
    fun updateName(newName:String){
        _name.value = newName
        workout.name = newName
        hasBeenEdited = true
    }

    fun save(){
        repo.saveWorkout(workout)
    }

    fun saveForPreview(){
        repo.saveWorkout(workout, true)
    }

    fun exportWorkout() : File {
        return repo.exportWorkout(workout)
    }


    fun load(fileName:String) {
        repo.getWorkout(fileName)?.let {
            workout = it
            originalName = it.name
            _name.value = it.name
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