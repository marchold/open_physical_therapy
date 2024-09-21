package com.catglo.openphysicaltherapy.WorkoutList


import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Create
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat.startActivity
import com.catglo.openphysicaltherapy.Data.ExerciseRepository
import com.catglo.openphysicaltherapy.Data.WorkoutNameConflict
import com.catglo.openphysicaltherapy.EditWorkout.EditWorkoutActivity
import com.catglo.openphysicaltherapy.R
import com.catglo.openphysicaltherapy.Widgets.DismissBackground
import com.catglo.openphysicaltherapy.Widgets.FloatingButtonItem
import com.catglo.openphysicaltherapy.Widgets.MultiFloatingActionButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutsListView(workoutsListViewModel: WorkoutListViewModel) {
    val context = LocalContext.current
    workoutsListViewModel.reload()
    val workoutsState = remember { workoutsListViewModel.getWorkouts() }
    var itemToDelete by remember { mutableIntStateOf(0) }
    val openDialog = remember { mutableStateOf(false)  }
    if (openDialog.value) {

        AlertDialog(
            onDismissRequest = {
                openDialog.value = false
            },
            title = {
                Text(text = "Are You Sure")
            },
            text = {
                Text("Delete ${workoutsListViewModel.getWorkout(itemToDelete).name}?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        workoutsListViewModel.deleteWorkout(workoutsListViewModel.getWorkout(itemToDelete).fileName)
                        openDialog.value = false
                    }) {
                    Text("Delete")
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                        openDialog.value = false
                    }) {
                    Text("Cancel")
                }
            }
        )
    }

    var openConflictResolveAlert by remember { mutableStateOf(false)  }
    var conflict by remember { mutableStateOf<WorkoutNameConflict?>(null) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) {
        it?.let {
            workoutsListViewModel.importWorkout(it)?.let { workoutNameConflict ->
                if (workoutNameConflict.existingNameConflictWorkout != null
                    && workoutNameConflict.exerciseConflicts.isNotEmpty())
                {
                    openConflictResolveAlert = true
                }
                conflict = workoutNameConflict
            }
        }
    }

    if (openConflictResolveAlert){

        AlertDialog(
            onDismissRequest =
            {
                //TODO: We should probobly delete the newly imported workout and its exercises in this case
                openConflictResolveAlert=false
            },
            title = {
                if (conflict?.existingNameConflictWorkout != null && (conflict?.exerciseConflicts?.size ?: 0) > 0){
                    //Conflict in both workout name and some exercise names
                    Text("There are workouts and exercises with the same names as the import")
                } else if (conflict?.existingNameConflictWorkout != null) {
                    //Workout has a name conflict but none of the exercises do
                    Text("There is a workout with this name already")
                } else {
                    //Workout name is unique but some of the exercises are not
                    Text("There are ${ (conflict?.exerciseConflicts?.size ?: 0) } exercises with the same names already")
                }
            },
            text = {Text("Keep both or overwrite?")},
            confirmButton = {
                Button(
                    onClick = {
                        conflict?.existingNameConflictWorkout?.fileName?.let {
                            workoutsListViewModel.deleteWorkout(it)
                        }
                        conflict?.exerciseConflicts?.forEach { exerciseNameConflict ->
                            exerciseNameConflict.existingNameConflictExercise?.fileName?.let { exerciseFileName ->
                                ExerciseRepository(context).deleteExercise(
                                    exerciseFileName
                                )
                            }
                        }
                        openConflictResolveAlert = false
                    })
                {
                    Text("Overwrite")
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                        if (conflict?.existingNameConflictWorkout != null) {
                            conflict?.newWorkout?.let { workoutsListViewModel.renameWorkout(it) }
                        }
                        conflict?.exerciseConflicts?.forEach { exerciseNameConflict ->
                            if (exerciseNameConflict.existingNameConflictExercise != null) {
                                val exerciseRepo = ExerciseRepository(context)
                                exerciseRepo.renameExercise(exerciseNameConflict.newExercise)
                            }
                        }
                        openConflictResolveAlert = false
                    })
                {
                    Text("Keep Both")
                }
            })
    }

    Scaffold(
        floatingActionButton = {
            MultiFloatingActionButton(
                modifier = Modifier,
                items = listOf(
                    FloatingButtonItem(Icons.Filled.Create, "Create Workout", onClick = {
                        startActivity(context,
                            Intent(context, EditWorkoutActivity::class.java),null)
                    }),
                    FloatingButtonItem(ImageVector.vectorResource(R.drawable.icon_import), "Import Workout File", onClick = {
                        launcher.launch(arrayOf("*/*"))
                    })
                ),
                icon = Icons.Filled.Add,
            )
        },
    ) { innerPadding ->
        Column(Modifier.padding(innerPadding)) {
            LazyColumn {
                items(workoutsState.size) { index ->
                    val dismissState = rememberSwipeToDismissBoxState(
                        confirmValueChange = {
                            when(it) {
                                SwipeToDismissBoxValue.StartToEnd -> {
                                    itemToDelete = index
                                    openDialog.value = true
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
                            Row(modifier = Modifier
                                .height(55.dp)
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.background)
                                .clickable
                                {
                                    Intent(context, EditWorkoutActivity::class.java).apply {
                                        this.putExtra(
                                            "EditWorkout",
                                            workoutsListViewModel.getWorkout(index).fileName
                                        )
                                        startActivity(context, this, null)
                                    }

                                },
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(text = workoutsListViewModel.getWorkout(index).name,
                                    modifier = Modifier.padding(10.dp))
                            }
                            HorizontalDivider()
                        })
                }
            }
        }
    }
}
