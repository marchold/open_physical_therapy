package com.catglo.openphysicaltherapy.ExercisesList

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Create
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.core.content.ContextCompat.startActivity
import com.catglo.openphysicaltherapy.Data.ExerciseNameConflict
import com.catglo.openphysicaltherapy.EditExercise.EditExerciseActivity
import com.catglo.openphysicaltherapy.Widgets.FloatingButtonItem
import com.catglo.openphysicaltherapy.Widgets.MultiFloatingActionButton
import com.catglo.openphysicaltherapy.R
import com.catglo.openphysicaltherapy.Widgets.DismissBackground
import com.catglo.openphysicaltherapy.ui.ExerciseListItemView

@Composable
fun ExercisesListView(exerciseListViewModel: ExerciseListViewModel) {
    val context = LocalContext.current

    var openConflictResolveAlert by remember { mutableStateOf(false)  }
    var conflict by remember { mutableStateOf<ExerciseNameConflict?>(null) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) {
        it?.let {
            exerciseListViewModel.importExercise(it)?.let { exerciseConflict ->
                if (exerciseConflict.oldExercise != null) openConflictResolveAlert = true
                conflict = exerciseConflict
            }
        }
    }
    if (openConflictResolveAlert){
        AlertDialog(
            onDismissRequest = { openConflictResolveAlert = false },
            title = { Text("You already have an exercise named ${conflict?.oldExercise?.name}")},
            text = { Text(text = "Would you like to replace it or keep them both?")},
            confirmButton = {
                Button(onClick = {
                    conflict?.oldExercise?.fileName?.let {
                        exerciseListViewModel.deleteExercise(it)
                    }
                    openConflictResolveAlert = false
                }) {
                    Text("Replace")
                }
            },
            dismissButton = {
                Button(onClick = {
                    conflict?.newExercise?.let {
                        exerciseListViewModel.renameExercise(it)
                    }
                    openConflictResolveAlert = false
                }) {
                    Text("Keep Both")
                }
            })
    }


    val exercisesState = remember { exerciseListViewModel.getExercises() }
    var itemToDelete by remember { mutableIntStateOf(0) }
    val openDeleteConfirmAlert = remember { mutableStateOf(false)  }
    if (openDeleteConfirmAlert.value) {

        AlertDialog(
            onDismissRequest = {
                openDeleteConfirmAlert.value = false
            },
            title = {
                Text(text = "Are You Sure")
            },
            text = {
                Text("Delete ${exerciseListViewModel.getExercise(itemToDelete).name}?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        exerciseListViewModel.deleteExercise(exerciseListViewModel.getExercise(itemToDelete).fileName)
                        openDeleteConfirmAlert.value = false
                    }) {
                    Text("Delete")
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                        openDeleteConfirmAlert.value = false
                    }) {
                    Text("Cancel")
                }
            }
        )
    }
    Scaffold(
        floatingActionButton = {
            MultiFloatingActionButton(
                modifier = Modifier,
                items = listOf(
                    FloatingButtonItem(Icons.Filled.Create, "Create Exercise", onClick = {
                        startActivity(context,
                            Intent(context, EditExerciseActivity::class.java),null)
                    }),
                    FloatingButtonItem(ImageVector.vectorResource(R.drawable.icon_import), "Import Exercise File", onClick = {
                        launcher.launch(arrayOf("*/*"))
                    })
                ),
                icon = Icons.Filled.Add,
            )
        },
    ) { innerPadding ->
        Column(Modifier.padding(innerPadding)) {
            LazyColumn{
                items(exercisesState.size) { index ->
                    val dismissState = rememberSwipeToDismissBoxState(
                        confirmValueChange = {
                            when(it) {
                                SwipeToDismissBoxValue.StartToEnd -> {
                                    itemToDelete = index
                                    openDeleteConfirmAlert.value = true
                                }
                                SwipeToDismissBoxValue.Settled -> return@rememberSwipeToDismissBoxState false
                                else -> {}
                            }
                            return@rememberSwipeToDismissBoxState false
                        },
                        positionalThreshold = { it * .25f }
                    )
                    SwipeToDismissBox(
                        state = dismissState,
                        modifier = Modifier.fillMaxSize(),
                        backgroundContent = { DismissBackground(dismissState) },
                        enableDismissFromEndToStart = false,
                        content = {
                            ExerciseListItemView(exerciseListViewModel.getExercise(index)){
                                Intent(context, EditExerciseActivity::class.java).apply {
                                    this.putExtra(
                                        "EditExercise",
                                        exerciseListViewModel.getExercise(index).fileName
                                    )
                                    startActivity(context, this, null)
                                }
                            }
                        })
                }
            }
        }
    }
}

fun importExercise() {


//    private var exportFile: File? = null
//    private val createFileLauncher =
//        registerForActivityResult(ActivityResultContracts.CreateDocument("application/zip")) { exportFileUri ->
//            exportFileUri?.let { outputUri ->
//                contentResolver.openOutputStream(outputUri)?.let { outputStream ->
//                    exportFile?.inputStream()?.copyTo(outputStream)
//                }
//            }
//        }
//
//    private fun exportToDocumentsFolder(exerciseViewModel: EditExerciseViewModel) {
//        createFileLauncher.launch(exerciseViewModel.prettyFileName())
//        exportFile = exerciseViewModel.exportExercise()
//    }
}
