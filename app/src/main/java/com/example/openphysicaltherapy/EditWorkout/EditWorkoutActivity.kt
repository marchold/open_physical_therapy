package com.example.openphysicaltherapy.EditWorkout

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.openphysicaltherapy.Data.ExerciseListItem
import com.example.openphysicaltherapy.Widgets.actionBarColors
import com.example.openphysicaltherapy.ui.theme.OpenPhysicalTherapyTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class EditWorkoutActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val workoutToEdit = intent.getStringExtra("EditWorkout")
        setContent {
            EditWorkoutView(workoutToEdit)
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun EditWorkoutView(workoutToEdit: String?) {
        val workoutViewModel = hiltViewModel<EditWorkoutViewModel>()
        workoutToEdit?.let {
            workoutViewModel.load(it)
        }

        val workoutExercises = workoutViewModel.getExercises()
        val pagerState = rememberPagerState(pageCount = { workoutExercises.size })
        val nameState by workoutViewModel.name.observeAsState()

        val coroutineScope = rememberCoroutineScope()
        var isWorkoutNameInvalid by remember { mutableStateOf(false) }
        var isWorkoutDuplicateError by remember { mutableStateOf(false) }
        var isSaveButtonEnabled by remember {
            mutableStateOf(
                (workoutViewModel.name.value?.length ?: 0) > 0
            )
        }
        val showConfirmDiscardAlert = remember { mutableStateOf(false) }

        if (showConfirmDiscardAlert.value) {
            AlertDialog(
                onDismissRequest = { showConfirmDiscardAlert.value = false },
                dismissButton = {
                    Button(onClick = {
                        showConfirmDiscardAlert.value = false
                    }) { Text(text = "Cancel") }
                },
                title = { Text("Discard Changes") },
                text = { Text("Are you sure you want to exit without saving?") },
                confirmButton = { Button(onClick = { finish() }) { Text(text = "Discard Changes") } }
            )
        }

        OpenPhysicalTherapyTheme {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("Create Workout") },
                        actions = {
                            IconButton(onClick = {
                                showConfirmDiscardAlert.value = true
                            }) {
                                Icon(
                                    Icons.Filled.Close,
                                    contentDescription = "Discard Changes Button"
                                )
                            }
                            IconButton(
                                onClick = {
                                    workoutViewModel.save()
                                    finish()
                                },
                                enabled = isSaveButtonEnabled
                            )
                            {
                                Icon(Icons.Filled.Done, contentDescription = "Save Workout Button")
                            }
                        },
                        colors = actionBarColors()
                    )
                },
                content = { paddingValues ->
                    Column(modifier = Modifier.padding(paddingValues)) {
                        Spacer(modifier = Modifier.height(10.dp))
                        TextField(
                            modifier = Modifier.fillMaxWidth(),
                            value = nameState!!,
                            onValueChange = {
                                isSaveButtonEnabled = true
                                if (it.isBlank()) {
                                    isWorkoutNameInvalid = true
                                    isSaveButtonEnabled = false
                                } else {
                                    isWorkoutNameInvalid = false
                                }
                                if (workoutViewModel.isWorkoutNameUnique(it)) {
                                    isWorkoutDuplicateError = false
                                } else {
                                    isWorkoutDuplicateError = true
                                    isSaveButtonEnabled = false
                                }
                                workoutViewModel.updateName(it)
                            },
                            label = {
                                Text(text = "Workout Name")
                            },
                            maxLines = 1
                        )
                        if (isWorkoutNameInvalid) {
                            Text(
                                text = "Please enter a valid Workout name",
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 12.sp
                            )
                        }
                        if (isWorkoutDuplicateError) {
                            Text(
                                text = "Workout name already exists",
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 12.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalPager(state = pagerState) { exerciseIndex ->
                            Column {
                                WorkoutExerciseView(workoutExercises[exerciseIndex], exerciseIndex)
                            }
                        }

                    }
                },
                bottomBar = {
                    BottomAppBar {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            IconButton(onClick = {
                                // TODO: Launch an exercise picker
                            }) {
                                Icon(Icons.Filled.Add, contentDescription = "Add Exercise")
                            }
                        }
                    }
                }
            )
        }
    }

    @Composable
    private fun WorkoutExerciseView(exerciseListItem: ExerciseListItem, exerciseIndex: Int) {

    }
}