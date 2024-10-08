package com.catglo.openphysicaltherapy.EditExercise


import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import com.catglo.openphysicaltherapy.R
import com.catglo.openphysicaltherapy.Widgets.NumberPickerTextField
import com.catglo.openphysicaltherapy.Widgets.actionBarColors
import com.catglo.openphysicaltherapy.WorkoutPlayer.ExercisePreviewActivity
import com.catglo.openphysicaltherapy.ui.theme.OpenPhysicalTherapyTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.io.File


@AndroidEntryPoint
class EditExerciseActivity() : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //The exercise to edit is injected from intent.getStringExtra("EditExercise")
        enableEdgeToEdge()
        setContent {
            EditExerciseView()
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun EditExerciseView() {
        val editExerciseViewModel = hiltViewModel<EditExerciseViewModel>()

        val exerciseSteps = editExerciseViewModel.getExerciseSteps()
        val pagerState = rememberPagerState(pageCount = {exerciseSteps.size})
        val nameState by editExerciseViewModel.name.observeAsState()

        val coroutineScope = rememberCoroutineScope()
        var isExerciseNameInvalid by remember { mutableStateOf(false) }
        var isExerciseDuplicateError by remember { mutableStateOf(false) }
        var isSaveButtonEnabled by remember { mutableStateOf(
            (editExerciseViewModel.name.value?.length ?: 0) > 0
        ) }
        
        OpenPhysicalTherapyTheme {
            val showConfirmDiscardAlert = remember { mutableStateOf( false ) }

            BackHandler {
                if (editExerciseViewModel.hasBeenEdited){
                    showConfirmDiscardAlert.value = true
                } else {
                    finish()
                }
            }

            if (showConfirmDiscardAlert.value){
                AlertDialog(
                    onDismissRequest = { showConfirmDiscardAlert.value = false },
                    dismissButton = { Button(onClick = { showConfirmDiscardAlert.value = false }) { Text(text = "Cancel") } },
                    title={ Text("Discard Changes") },
                    text={ Text("Are you sure you want to exit without saving?") },
                    confirmButton = { Button(onClick = { finish() }) { Text(text = "Discard Changes") } }
                )
            }

            var showConfirmDeleteStepAlert by remember { mutableStateOf( false ) }
            var stepIndexToDelete by remember { mutableIntStateOf( 0 ) }
            if (showConfirmDeleteStepAlert){
                AlertDialog(
                    onDismissRequest = { showConfirmDeleteStepAlert = false },
                    dismissButton = { Button(onClick = { showConfirmDeleteStepAlert = false }) { Text(text = "Cancel") } },
                    title={ Text("Delete this step") },
                    text={ Text("Are you sure you want to remove this step?") },
                    confirmButton = { Button(onClick =
                    {
                        val currentPage = stepIndexToDelete
                        editExerciseViewModel.removeStep(currentPage)
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(currentPage-1)
                        }
                        showConfirmDeleteStepAlert = false
                    }) { Text(text = "Delete Step") } }
                )
            }

            val exportBottomSheetState = rememberModalBottomSheetState()
            var showExportBottomSheet by remember { mutableStateOf(false) }
            if (showExportBottomSheet) {
                ModalBottomSheet(onDismissRequest = {

                }, sheetState = exportBottomSheetState) {
                    Column {
                        TextButton(onClick = { exportToDocumentsFolder(editExerciseViewModel) }) {
                            Text("Save to documents folder")
                        }
                        TextButton(onClick = { exportToShareIntent(editExerciseViewModel) }) {
                            Text("Share to other apps")
                        }
                    }
                }
            }
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("Edit Exercise") },
                        actions = {
                            IconButton(onClick = {
                                showExportBottomSheet = true
                            }) {
                                Icon(imageVector = ImageVector.vectorResource(R.drawable.icon_export),
                                    contentDescription = "Export")
                            }
                            IconButton(onClick = {
                                editExerciseViewModel.saveForPreview()
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
                                if (editExerciseViewModel.hasBeenEdited){
                                    showConfirmDiscardAlert.value = true
                                } else {
                                    finish()
                                }
                            }) {
                                Icon(Icons.Filled.Close, contentDescription = "Discard Changes Button")
                            }
                            IconButton(onClick = {
                                editExerciseViewModel.save()
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
                                if (editExerciseViewModel.isExerciseNameUnique(it)){
                                    isExerciseDuplicateError = false
                                }else{
                                    isExerciseDuplicateError = true
                                    isSaveButtonEnabled = false
                                }
                                editExerciseViewModel.updateName(it)
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

                       NumberPickerTextField(
                            intLiveData = editExerciseViewModel.numberOfReps(),
                            icon = ImageVector.vectorResource(R.drawable.icon_repeat),
                            minimumValue = 1,
                            maximumValue = 20,
                            "Number of reps"
                        ) {
                            editExerciseViewModel.updateNumberOfReps(it)
                        }


                        Spacer(modifier = Modifier.height(10.dp))
                        Box {
                            Text(
                                text = "Step ${pagerState.currentPage + 1}",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp)
                                    .align(Alignment.Center),
                                textAlign = TextAlign.Center
                            )
                            if (pagerState.currentPage > 0) {
                                IconButton(onClick = {
                                    stepIndexToDelete = pagerState.currentPage
                                    showConfirmDeleteStepAlert = true
                                }) {
                                    Icon(Icons.Filled.Close, contentDescription = "Delete Step")
                                }
                            }
                        }
                        HorizontalDivider(thickness = 2.dp, color = MaterialTheme.colorScheme.onBackground, modifier = Modifier.padding(0.dp,0.dp,0.dp,8.dp))
                        HorizontalPager(state = pagerState ) { stepIndex ->
                            Column {
                                ExerciseStepView(
                                    editExerciseViewModel = editExerciseViewModel,
                                    stepIndex = stepIndex
                                )
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
                                editExerciseViewModel.addStep()
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



    private var exportFile: File? = null
    private val createFileLauncher =
        registerForActivityResult(ActivityResultContracts.CreateDocument("application/zip")) { exportFileUri ->
            exportFileUri?.let { outputUri ->
                contentResolver.openOutputStream(outputUri)?.let { outputStream ->
                    exportFile?.inputStream()?.copyTo(outputStream)
                }
            }
        }

    private fun exportToDocumentsFolder(exerciseViewModel: EditExerciseViewModel) {
        createFileLauncher.launch(exerciseViewModel.prettyFileName())
        exportFile = exerciseViewModel.exportExercise()
    }


    private fun exportToShareIntent(exerciseViewModel: EditExerciseViewModel) {
        val exportZipFile = FileProvider
            .getUriForFile(
                applicationContext,
                "$packageName.fileprovider",
                exerciseViewModel.exportExercise())
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


