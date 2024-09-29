package com.catglo.openphysicaltherapy.EditWorkout

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import com.catglo.openphysicaltherapy.Data.ExerciseListItem
import com.catglo.openphysicaltherapy.EditExercise.EditExerciseViewModel
import com.catglo.openphysicaltherapy.ExercisesList.ExerciseListViewModel
import com.catglo.openphysicaltherapy.R
import com.catglo.openphysicaltherapy.Widgets.DismissBackground
import com.catglo.openphysicaltherapy.Widgets.DragAndDropLazyColumn
import com.catglo.openphysicaltherapy.Widgets.actionBarColors
import com.catglo.openphysicaltherapy.WorkoutPlayer.WorkoutPlayerActivity
import com.catglo.openphysicaltherapy.ui.ExerciseListItemView
import com.catglo.openphysicaltherapy.ui.theme.OpenPhysicalTherapyTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.io.File
import com.catglo.openphysicaltherapy.toValidFileName

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
        OpenPhysicalTherapyTheme {
            val exerciseListViewModel = hiltViewModel<ExerciseListViewModel>()
            exerciseListViewModel.reload()
            val allExercises = exerciseListViewModel.getExercises()


            val workoutsExercises = workoutViewModel.getExercises()
            val nameState by workoutViewModel.name.observeAsState()

            var isWorkoutNameInvalid by remember { mutableStateOf(false) }
            var isWorkoutDuplicateError by remember { mutableStateOf(false) }
            var isSaveButtonEnabled by remember { mutableStateOf((workoutToEdit?.length ?: 0) > 0)}

            var showExportBottomSheet by remember {   mutableStateOf(false) }
            val exportBottomSheetState = rememberModalBottomSheetState()
            if (showExportBottomSheet){
                ModalBottomSheet(onDismissRequest = {

                }, sheetState = exportBottomSheetState) {
                    Column {
                        TextButton(onClick = {
                            exportToDocumentsFolder(workoutViewModel)
                            showExportBottomSheet = false
                        }) {
                            Text("Save to documents folder")
                        }
                        TextButton(onClick = {
                            exportToShareIntent(workoutViewModel)
                            showExportBottomSheet = false
                        }) {
                            Text("Share to other apps")
                        }
                    }
                }
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
                    }
                    LazyColumn {
                        val filteredItems = allExercises.filter { it.name.contains(text, ignoreCase = true) }
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

            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("Edit Workout") },
                        actions = {
                            IconButton(onClick = {
                                //Export
                                showExportBottomSheet = true
                            }) {
                                Icon(ImageVector.vectorResource(id = R.drawable.icon_export),"Export Icon")
                            }
                            IconButton(onClick = {
                                //Launch preview
                                workoutViewModel.saveForPreview()
                                val intent = Intent(this@EditWorkoutActivity, WorkoutPlayerActivity::class.java)
                                intent.putExtra("Workout", "workout_preview")
                                startActivity(intent)
                            }) {
                                Icon(ImageVector.vectorResource(id = R.drawable.icon_preview),"Preview Icon")
                            }
                            IconButton(onClick = {
                                if (workoutViewModel.hasBeenEdited) {
                                    showConfirmDiscardAlert.value = true
                                } else {
                                    finish()
                                }
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

                        DragAndDropLazyColumn(
                            list = workoutsExercises,
                            itemContent = { item, index ->
                                val dismissState = rememberSwipeToDismissBoxState(
                                    confirmValueChange = {
                                        when(it) {
                                            SwipeToDismissBoxValue.StartToEnd -> {
                                                workoutViewModel.removeExercise(index)
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
                                        ExerciseListItemView(exerciseListItem = item)
                                    })
                            },
                            moveListItem = { from, to ->
                                workoutViewModel.moveExercise(from, to)
                                workoutsExercises
                            })
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

    private var exportFile: File? = null
    private val createFileLauncher =
        registerForActivityResult(ActivityResultContracts.CreateDocument("application/zip")) { exportFileUri ->
            exportFileUri?.let { outputUri ->
                contentResolver.openOutputStream(outputUri)?.let { outputStream ->
                    exportFile?.inputStream()?.copyTo(outputStream)
                }
            }
        }
    private fun exportToDocumentsFolder(workoutViewModel: EditWorkoutViewModel) {
        exportFile = workoutViewModel.exportWorkout()
        workoutViewModel.name.value?.toValidFileName()?.let {
            createFileLauncher.launch(it)
        }
    }


    private fun exportToShareIntent(workoutViewModel: EditWorkoutViewModel) {
        val exportZipFile = FileProvider
            .getUriForFile(
                applicationContext,
                "$packageName.fileprovider",
                workoutViewModel.exportWorkout())
        val shareIntent = Intent()
        shareIntent.setAction(Intent.ACTION_SEND)
        shareIntent.putExtra(Intent.EXTRA_STREAM, exportZipFile)
        shareIntent.setType("application/zip")
        startActivity(
            Intent.createChooser(
                shareIntent,
                "Send to"
            )
        )
    }

}