package com.example.openphysicaltherapy

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
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.openphysicaltherapy.ui.theme.OpenPhysicalTherapyTheme
import kotlinx.coroutines.launch

class CreateExerciseActivity : ComponentActivity() {
    private var exercise = Exercise("")

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
        val pagerState = rememberPagerState(pageCount = { exercise.steps.size })
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
                            }
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalPager(state = pagerState) { page ->
                            // Our page content
                            Text(
                                text = "Page: $page",
                                modifier = Modifier.fillMaxWidth()
                            )
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
                                pagerState.pageCount.inc()
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(exercise.steps.size - 1)
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
