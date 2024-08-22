package com.example.openphysicaltherapy.WorkoutList

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel
import com.example.openphysicaltherapy.Data.ExerciseListItem
import com.example.openphysicaltherapy.Data.ExerciseRepository
import com.example.openphysicaltherapy.Data.WorkoutListItem
import com.example.openphysicaltherapy.Data.WorkoutRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

@HiltViewModel
class WorkoutListViewModel @Inject constructor(private val repo: WorkoutRepository, @ApplicationContext val context: Context) : ViewModel() {
    private val listItems = mutableStateListOf<WorkoutListItem>()
    init {
        reload()
    }
    fun reload(){
        listItems.clear()
        repo.getWorkoutList().forEach { workoutListIten ->
            listItems.add(workoutListIten)
        }
    }
    fun deleteExercise(name:String){
        repo.deleteWorkout(name)
        listItems.removeIf { it.name == name }
    }
    fun getWorkout(index: Int): ExerciseListItem {
        return ExerciseListItem(listItems[index].name)
    }

    fun getWorkouts(): SnapshotStateList<WorkoutListItem> {
        return listItems
    }
}