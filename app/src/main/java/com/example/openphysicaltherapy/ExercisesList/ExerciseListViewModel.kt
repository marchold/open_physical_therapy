package com.example.openphysicaltherapy.ExercisesList

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel
import com.example.openphysicaltherapy.Data.ExerciseListItem
import com.example.openphysicaltherapy.Data.ExerciseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

@HiltViewModel
class ExerciseListViewModel @Inject constructor(private val repo: ExerciseRepository, @ApplicationContext val context: Context) : ViewModel() {
    private val listItems = mutableStateListOf<ExerciseListItem>()
    init {
        reload()
    }
    fun reload(){
        listItems.clear()
        repo.getExerciseList(context).forEach { exerciseListItem ->
            listItems.add(exerciseListItem)
        }
    }
    fun deleteExercise(name:String){
        repo.deleteExercise(context, name)
        listItems.removeIf { it.name == name }
    }
    fun getExercise(index: Int): ExerciseListItem {
        return ExerciseListItem(listItems[index].name)
    }

    fun getExercises(): SnapshotStateList<ExerciseListItem> {
        return listItems
    }
}