package com.example.openphysicaltherapy.EditWorkout

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.openphysicaltherapy.ExercisesList.ExerciseListViewModel
import com.example.openphysicaltherapy.Widgets.actionBarColors
import com.example.openphysicaltherapy.ui.theme.OpenPhysicalTherapyTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

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
        LaunchedEffect(Unit) {
            workoutToEdit?.let {
                workoutViewModel.load(it)
            }
        }
        val exerciseListViewModel = hiltViewModel<ExerciseListViewModel>()
        exerciseListViewModel.reload()

        val exercisesState = exerciseListViewModel.getExercises()
        val workoutsExercises = workoutViewModel.getExercises()
        val nameState by workoutViewModel.name.observeAsState()

        var isWorkoutNameInvalid by remember { mutableStateOf(false) }
        var isWorkoutDuplicateError by remember { mutableStateOf(false) }
        var isSaveButtonEnabled by remember { mutableStateOf((workoutToEdit?.length ?: 0) > 0)}
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

        var showExercisePicker by remember { mutableStateOf(false)  }
        val sheetState = rememberModalBottomSheetState(
            skipPartiallyExpanded = true
        )
        val scope = rememberCoroutineScope()

        if (showExercisePicker) {
            ModalBottomSheet(
                onDismissRequest = {
                    showExercisePicker = false
                },
                shape = MaterialTheme.shapes.large,
                sheetState = sheetState
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    IconButton(
                        onClick =
                        {
                            scope.launch { sheetState.hide() }.invokeOnCompletion {
                                if (!sheetState.isVisible) {
                                    showExercisePicker = false
                                }
                            }
                        }, modifier = Modifier
                    ) {
                        Icon(Icons.Filled.Close, contentDescription = "Close Button")
                    }
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier
                            .fillMaxWidth()
                            .align(alignment = Alignment.Center)
                    ) {
                        Text(text = "Pick Exercise", fontWeight = FontWeight.Bold)
                    }
                }
                var text by remember { mutableStateOf("") }
                Row(
                    modifier = Modifier
                        .height(55.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    BasicTextField(
                        modifier = Modifier
                            .weight(5f)
                            .fillMaxWidth()
                            .padding(horizontal = 5.dp),
                        value = text,
                        onValueChange = {
                            text = it
                        },

                        textStyle = TextStyle(
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        decorationBox = { innerTextField ->
                            if (text.isEmpty()) {
                                Text(
                                    text = "Search Exercises",
                                    color = Color.Gray.copy(alpha = 0.5f),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                            innerTextField()
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Search
                        ),
                        keyboardActions = KeyboardActions(onSearch = {
                            Log.i("search", "onSearchClicked()")
                        }),
                        singleLine = true
                    )

//                    Icon(
//                        modifier = Modifier
//                            .fillMaxSize()
//                            .padding(10.dp),
//                        imageVector = Icons.Outlined.Search,
//                        contentDescription = "stringResource(R.string.search)",
//                        tint = MaterialTheme.colorScheme.primary,
//                    )


                }
                LazyColumn {
                    val filteredItems = exercisesState.filter { it.name.contains(text, ignoreCase = true) }
                    items(filteredItems.size){ index ->
                        Row(modifier = Modifier
                            .height(55.dp)
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.background)
                            .clickable
                            {
                                workoutViewModel.addExercise(filteredItems[index])
                                showExercisePicker = false
                            },
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(text = filteredItems[index].name,
                                modifier = Modifier.padding(10.dp))
                        }
                        HorizontalDivider()
                    }
                }

            }
        }

        OpenPhysicalTherapyTheme {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("Edit Workout") },
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

                        LazyColumn {
                            items(workoutsExercises.size){ index ->
                                Row(modifier = Modifier
                                    .height(55.dp)
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.background),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text(text = workoutsExercises[index].name,
                                        modifier = Modifier.padding(10.dp))
                                }
                                HorizontalDivider()
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
                                showExercisePicker = true
                            }) {
                                Icon(Icons.Filled.Add, contentDescription = "Add Exercise")
                            }
                        }
                    }
                }
            )
        }
    }

}