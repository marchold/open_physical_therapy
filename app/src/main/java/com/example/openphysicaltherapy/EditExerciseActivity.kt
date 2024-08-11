package com.example.openphysicaltherapy

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Info
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
import androidx.compose.material3.TopAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.openphysicaltherapy.ui.theme.OpenPhysicalTherapyTheme
import kotlinx.coroutines.launch

class EditExerciseActivity() : ComponentActivity() {
    private val exercise by viewModels<Exercise>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CreateExerciseScreen()
        }
    }

    @Preview(showSystemUi = true)
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun CreateExerciseScreen(){

        val name by exercise.name.observeAsState()
        val pagerState = PagerState { exercise.stepsFlow.value.size }
        val coroutineScope = rememberCoroutineScope()

        OpenPhysicalTherapyTheme {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text(getString(R.string.title_activity_create_exercise)) },
                        actions = {
                            IconButton(onClick = {
                                //exercise.save(this@CreateExerciseActivity)
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
                            value = name!!,
                            onValueChange = {
                                exercise.updateName(it)
                            },
                            label = {
                                Text(text = stringResource(R.string.name_exercise))
                            },
                            maxLines = 1
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Step ${pagerState.currentPage}",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            textAlign = TextAlign.Center
                        )
                        HorizontalDivider(thickness = 2.dp, color = MaterialTheme.colorScheme.onBackground, modifier = Modifier.padding(0.dp,0.dp,0.dp,8.dp))
                        HorizontalPager(state = pagerState ) { page ->
                            Column {
                                ExerciseStepScreen(page)
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
                                exercise.addStep()
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage((exercise.steps.size) -1)
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


    @Composable
    fun ExerciseStepScreen(page: Int) {
        val scrollState = rememberScrollState()
        Column(Modifier.verticalScroll(scrollState)) {
            NumberPickerTextField(
                intLiveData = exercise.steps[page].numberOfReps,
                icon = ImageVector.vectorResource(R.drawable.icon_repeat),
                minimumValue = 1,
                maximumValue = 20,
                "Number of reps"
            ) {
                exercise.steps[page].updateNumberOfReps(it)
            }

            exercise.steps[page].slides.forEachIndexed{ slideIndex, slide ->
                Box {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text("Slide $slideIndex")
                    }
                    if (slideIndex>0) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth(),
                            horizontalAlignment = Alignment.End,
                            verticalArrangement = Arrangement.Center
                        ) {
                            IconButton(onClick = { exercise.steps[page].removeSlide(slideIndex) }) {
                                Icon(Icons.Outlined.Close, contentDescription = "Remove Slide Icon")
                            }
                        }
                    }
                }
                val instructionText by slide.text.observeAsState()
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Outlined.Info,
                        contentDescription = "Information Icon",
                        modifier = Modifier.padding(start = 10.dp, top=0.dp, end=10.dp, bottom = 0.dp))
                    TextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = instructionText!!,
                        onValueChange = { newValue ->
                            slide.updateText(newValue)
                        },
                        label = {
                            Text(text = "Instructional text")
                        }
                    )
                }
                NumberPickerTextField(
                    intLiveData = slide.duration,
                    icon = ImageVector.vectorResource(R.drawable.icon_timer),
                    minimumValue = 3,
                    maximumValue = 500,
                    title = "Duration in seconds"
                ) {
                    slide.updateDuration(it)
                }
                Row {
                    // TODO:
                    //   Icon (image, video)
                    //   Video/Audio+Image file picker
                }

            }
            Row(modifier = Modifier.fillMaxWidth()) {
                Spacer(modifier = Modifier.weight(1f))
                Button(onClick = {
                    exercise.steps[page].addSlides()
                }) {
                    Text("Add slide")
                }
            }
        }
    }
}
