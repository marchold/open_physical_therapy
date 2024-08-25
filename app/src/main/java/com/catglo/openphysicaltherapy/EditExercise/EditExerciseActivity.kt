package com.catglo.openphysicaltherapy.EditExercise

import android.content.Intent
import android.os.Bundle
import android.util.Log
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.catglo.openphysicaltherapy.R
import com.catglo.openphysicaltherapy.Widgets.actionBarColors
import com.catglo.openphysicaltherapy.WorkoutPlayer.ExercisePreviewActivity
import com.catglo.openphysicaltherapy.ui.theme.OpenPhysicalTherapyTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch


@AndroidEntryPoint
class EditExerciseActivity() : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val exerciseToEdit = intent.getStringExtra("EditExercise")
        enableEdgeToEdge()
        setContent {
            EditExerciseView(exerciseToEdit)
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun EditExerciseView(exerciseToEdit: String?) {
        val exerciseViewModel = hiltViewModel<EditExerciseViewModel>()
        exerciseToEdit?.let {
            exerciseViewModel.load(it)
        }

        val exerciseSteps = exerciseViewModel.getExerciseSteps()
        val pagerState = rememberPagerState(pageCount = {exerciseSteps.size})
        val nameState by exerciseViewModel.name.observeAsState()

        val coroutineScope = rememberCoroutineScope()
        var isExerciseNameInvalid by remember { mutableStateOf(false) }
        var isExerciseDuplicateError by remember { mutableStateOf(false) }
        var isSaveButtonEnabled by remember { mutableStateOf(
            (exerciseViewModel.name.value?.length ?: 0) > 0
        ) }
        val showConfirmDiscardAlert = remember { mutableStateOf( false ) }

        if (showConfirmDiscardAlert.value){
            AlertDialog(
                onDismissRequest = { showConfirmDiscardAlert.value = false },
                dismissButton = { Button(onClick = { showConfirmDiscardAlert.value = false }) { Text(text = "Cancel") } },
                title={ Text("Discard Changes") },
                text={ Text("Are you sure you want to exit without saving?") },
                confirmButton = { Button(onClick = { finish() }) { Text(text = "Discard Changes") } }
            )
        }

        OpenPhysicalTherapyTheme {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("Edit Exercise") },
                        actions = {
                            IconButton(onClick = {
                                exerciseViewModel.saveForPreview()
                                startActivity(
                                    Intent(this@EditExerciseActivity,
                                    ExercisePreviewActivity::class.java).apply {
                                        putExtra("Exercise", "exercise_preview")
                                    }
                                )
                            }) {
                                Icon(imageVector = ImageVector.vectorResource(id = R.drawable.icon_preview),
                                    contentDescription = "Preview Changes Button")
                            }
                            IconButton(onClick = {
                                showConfirmDiscardAlert.value = true
                            }) {
                                Icon(Icons.Filled.Close, contentDescription = "Discard Changes Button")
                            }
                            IconButton(onClick = {
                                exerciseViewModel.save()
                                finish()
                            },
                                enabled = isSaveButtonEnabled)
                            {
                                Icon(Icons.Filled.Done, contentDescription = "Save Exercise Button")
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
                                if (it.isBlank()){
                                    isExerciseNameInvalid = true
                                    isSaveButtonEnabled = false
                                }else {
                                    isExerciseNameInvalid = false
                                }
                                if (exerciseViewModel.isExerciseNameUnique(it)){
                                    isExerciseDuplicateError = false
                                }else{
                                    isExerciseDuplicateError = true
                                    isSaveButtonEnabled = false
                                }
                                exerciseViewModel.updateName(it)
                            },
                            label = {
                                Text(text = stringResource(R.string.name_exercise))
                            },
                            maxLines = 1
                        )
                        if (isExerciseNameInvalid){
                            Text(text = "Please enter a valid exercise name", color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                        }
                        if (isExerciseDuplicateError){
                            Text(text = "Exercise name already exists", color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Step ${pagerState.currentPage+1}",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            textAlign = TextAlign.Center
                        )
                        HorizontalDivider(thickness = 2.dp, color = MaterialTheme.colorScheme.onBackground, modifier = Modifier.padding(0.dp,0.dp,0.dp,8.dp))
                        HorizontalPager(state = pagerState ) { stepIndex ->
                            Column {
                                ExerciseStepView(exerciseSteps[stepIndex],stepIndex, exerciseViewModel.fileName() )
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
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(pagerState.currentPage - 1)
                                }
                            },
                                enabled = pagerState.currentPage>0) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous Step")
                            }
                            IconButton(onClick = {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                }
                            },
                                enabled = pagerState.currentPage<pagerState.pageCount-1) {
                                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next Step")
                            }
                            IconButton(onClick = {
                                exerciseViewModel.addStep()
                                Log.i("pager","pager state page count = ${pagerState.pageCount}")
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(pagerState.pageCount-1)
                                }
                            }) {
                                Icon(Icons.Filled.Add, contentDescription = "Add Step")
                            }
                        }
                    }

                }
            )
        }
    }
}


