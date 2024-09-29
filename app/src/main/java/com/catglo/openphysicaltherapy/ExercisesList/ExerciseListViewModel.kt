package com.catglo.openphysicaltherapy.ExercisesList

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel
import com.catglo.openphysicaltherapy.Data.Exercise
import com.catglo.openphysicaltherapy.Data.ExerciseListItem
import com.catglo.openphysicaltherapy.Data.ExerciseNameConflict
import com.catglo.openphysicaltherapy.Data.ExerciseRepository
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
        repo.getExerciseList().forEach { exerciseListItem ->
            listItems.add(exerciseListItem)
        }
    }
    fun deleteExercise(fileName:String){
        repo.deleteExercise(fileName)
        listItems.removeIf { it.fileName == fileName }
    }
    fun getExercise(index: Int): ExerciseListItem {
        return ExerciseListItem(listItems[index].name,listItems[index].fileName)
    }

    fun getExercises(): SnapshotStateList<ExerciseListItem> {
        return listItems
    }

    fun importExercise(importZipFileUri: Uri): ExerciseNameConflict? {
        return repo.importExercise(importZipFileUri)
    }

    fun renameExercise(exercise: Exercise) {
        repo.renameExercise(exercise)
        listItems.forEachIndexed { _, exerciseListItem ->
            if (exerciseListItem.fileName == exercise.fileName){
                exerciseListItem.name = exercise.name
            }
        }
    }
}