package com.example.openphysicaltherapy

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContract
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.openphysicaltherapy.ui.theme.OpenPhysicalTherapyTheme
import kotlinx.coroutines.launch

class GetCustomContents(): ActivityResultContract<String, List<@JvmSuppressWildcards Uri>>() {

    override fun createIntent(context: Context, input: String): Intent {
        return Intent(Intent.ACTION_GET_CONTENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = input //The input option es the MIME Type that you need to use
            //putExtra(Intent.EXTRA_LOCAL_ONLY, true) //Return data on the local device
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false)
                .addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }

    override fun parseResult(resultCode: Int, intent: Intent?): List<Uri> {
        return intent.takeIf {
            resultCode == Activity.RESULT_OK
        }?.getClipDataUris() ?: emptyList()
    }

    internal companion object {

        //Collect all Uris from files selected
        internal fun Intent.getClipDataUris(): List<Uri> {
            // Use a LinkedHashSet to maintain any ordering that may be
            // present in the ClipData
            val resultSet = LinkedHashSet<Uri>()
            data?.let { data ->
                resultSet.add(data)
            }
            val clipData = clipData
            if (clipData == null && resultSet.isEmpty()) {
                return emptyList()
            } else if (clipData != null) {
                for (i in 0 until clipData.itemCount) {
                    val uri = clipData.getItemAt(i).uri
                    if (uri != null) {
                        resultSet.add(uri)
                    }
                }
            }
            return ArrayList(resultSet)
        }
    }
}

class CreateExerciseActivity : ComponentActivity() {
    private var exercise = Exercise("", steps = mutableListOf(ExerciseStep(1, mutableListOf(
        ExerciseInstruction()
    ) )))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CreateExerciseScreen()
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun CreateExerciseScreen(){
        var pageCount by remember { mutableIntStateOf(exercise.steps.size) }
        val pagerState = rememberPagerState(pageCount = { pageCount })
        var exerciseName by remember { mutableStateOf("") }
        val coroutineScope = rememberCoroutineScope()

        OpenPhysicalTherapyTheme {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text(getString(R.string.title_activity_create_exercise)) },
                        actions = {
                            IconButton(onClick = {
                                exercise.save(this@CreateExerciseActivity)
                                finish()
                            }) {
                                Icon(Icons.Filled.Done, contentDescription = "Save Exercise Button")
                            }
                        },
                        colors = TopAppBarColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            scrolledContainerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                },
                content = { paddingValues ->
                    Column(modifier = Modifier.padding(paddingValues)) {
                        Spacer(modifier = Modifier.height(10.dp))
                        TextField(
                            modifier = Modifier.fillMaxWidth(),
                            value = exerciseName,
                            onValueChange = {
                                exerciseName = it
                                exercise.name = it
                            },
                            label = {
                                Text(text = stringResource(R.string.please_name_exercise))
                            },
                            maxLines = 1
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalPager(state = pagerState) { page ->
                            Column {
                                Text(
                                    text = "Step $page",
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.Center
                                )
                                ExerciseStepScreen()
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
                            }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous Step")
                            }
                            IconButton(onClick = {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                }
                            }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next Step")
                            }
                            IconButton(onClick = {
                                exercise.steps.add(ExerciseStep(0, mutableListOf()))
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(exercise.steps.size-1)
                                }
                                pageCount++
                            }) {
                                Icon(Icons.Filled.Add, contentDescription = "Add Step")
                            }
                        }
                    }

                }
            )
        }
    }

    @Composable
    fun ExerciseStepScreen(){
        var numberOfReps by remember { mutableStateOf("1") }
        val filePicker = rememberLauncherForActivityResult(
            contract = GetCustomContents(isMultiple = true),
            onResult = { uris ->
                uris?.forEach { uri ->
                    Log.d("MainActivity", "uri: $uri")
                }
            })
        Column {
            exercise.steps.forEachIndexed { index, step ->
                TextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = numberOfReps,
                    onValueChange = { newValue ->
                        if (newValue.isBlank()){
                            exercise.steps[index].numberOfReps = 0
                            numberOfReps = ""
                        }
                        else {
                            newValue.toIntOrNull()?.let {
                                exercise.steps[index].numberOfReps = it
                                numberOfReps = it.toString()
                            }
                        }
                    },
                    keyboardOptions = KeyboardOptions.Default.copy(
                        keyboardType = KeyboardType.NumberPassword
                    ),
                    label = {
                        Text(text = "Number of reps")
                    },
                    maxLines = 1
                )
                step.instructions.forEachIndexed{ instructionIndex, instruction ->
                    var instructionText by remember { mutableStateOf("") }
                    var duration by remember { mutableStateOf("") }
                    TextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = instructionText,
                        onValueChange = { newValue ->
                            instructionText = newValue
                        },
                        label = {
                            Text(text = "Instructional text")
                        }
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            ImageVector.vectorResource(R.drawable.icon_timer),
                            contentDescription = "Timer Icon",
                            modifier = Modifier.padding(start = 10.dp, top=0.dp, end=10.dp, bottom = 0.dp))
                        TextField(
                            modifier = Modifier.fillMaxWidth(),
                            value = duration,
                            onValueChange = { newValue ->
                                duration = newValue
                            },
                            keyboardOptions = KeyboardOptions.Default.copy(
                                keyboardType = KeyboardType.NumberPassword
                            ),
                            label = {
                                Text(text = "Duration in seconds")
                            }
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = ImageVector.vectorResource(id = R.drawable.icon_file_import),
                            contentDescription = "Import files icon",
                            modifier = Modifier.padding(start = 10.dp, top=0.dp, end=10.dp, bottom = 0.dp)
                        )
                        FileChooser(R.drawable.icon_add_image){
                            filePicker.launch("image/*", )
                        }
                        FileChooser(R.drawable.icon_add_video){
                            filePicker.launch("video/*", )
                        }
                        FileChooser(R.drawable.icon_add_audio) {
                            filePicker.launch("audio/*", )
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun FileChooser( iconResource: Int, filePicker: () -> Unit ){
        IconButton(
            onClick = { filePicker() },
            Modifier
                .background(MaterialTheme.colorScheme.secondary)
                .border(5.dp, MaterialTheme.colorScheme.onSecondary)

        ){
            Icon(ImageVector.vectorResource(iconResource),
                contentDescription = "Select Image File",
                tint = MaterialTheme.colorScheme.onSecondary

            )
        }
    }
}


/*
class ExerciseInstruction(
    var text:String,
    var audio:Boolean,
    var image:Boolean,
    var video:Boolean,
    var duration: Int, //In seconds
    var countdown:Boolean,
)

class ExerciseStep(
    var numberOfReps:Int,
    var instructions: MutableList<ExerciseInstruction>
)

class Exercise(
    var name:String,
    var icon:Boolean,
    var steps:MutableList<ExerciseStep>
){
 */
