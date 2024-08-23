package com.example.openphysicaltherapy.ExercisesList

import android.content.Intent
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
import com.example.openphysicaltherapy.EditExercise.EditExerciseActivity
import com.example.openphysicaltherapy.Widgets.FloatingButtonItem
import com.example.openphysicaltherapy.Widgets.MultiFloatingActionButton
import com.example.openphysicaltherapy.R
import com.example.openphysicaltherapy.Widgets.DismissBackground

@Composable
fun ExercisesListView(exerciseListViewModel: ExerciseListViewModel) {
    val context = LocalContext.current

    val exercisesState = remember { exerciseListViewModel.getExercises() }
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
                Text("Delete ${exerciseListViewModel.getExercise(itemToDelete).name}?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        exerciseListViewModel.deleteExercise(exerciseListViewModel.getExercise(itemToDelete).name)
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

                    })
                ),
                icon = Icons.Filled.Add,
            )
        },
    ) { innerPadding ->
        Column(Modifier.padding(innerPadding)) {
            LazyColumn(Modifier.padding(top=10.dp)){
                items(exercisesState.size) { index ->
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
                                    Intent(context, EditExerciseActivity::class.java).apply {
                                        this.putExtra(
                                            "EditExercise",
                                            exerciseListViewModel.getExercise(index).name
                                        )
                                        startActivity(context, this, null)
                                    }

                                },
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(text = exerciseListViewModel.getExercise(index).name,
                                    modifier = Modifier.padding(10.dp))
                            }
                            HorizontalDivider()
                        })
                }
            }
        }
    }
}
