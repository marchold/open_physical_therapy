package com.catglo.openphysicaltherapy.WorkoutList

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel
import com.catglo.openphysicaltherapy.Data.ExerciseListItem
import com.catglo.openphysicaltherapy.Data.ExerciseNameConflict
import com.catglo.openphysicaltherapy.Data.ExerciseRepository
import com.catglo.openphysicaltherapy.Data.WorkoutListItem
import com.catglo.openphysicaltherapy.Data.WorkoutNameConflict
import com.catglo.openphysicaltherapy.Data.WorkoutRepository
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
    fun getWorkout(index: Int): WorkoutListItem {
        return WorkoutListItem(listItems[index].name,listItems[index].fileName)
    }

    fun getWorkouts(): SnapshotStateList<WorkoutListItem> {
        return listItems
    }

    fun importWorkout(importZipFileUri: Uri): WorkoutNameConflict? {
        return repo.importWorkout(importZipFileUri)
    }
}