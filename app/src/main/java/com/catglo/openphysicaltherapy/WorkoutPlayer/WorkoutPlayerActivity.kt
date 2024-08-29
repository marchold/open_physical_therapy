package com.catglo.openphysicaltherapy.WorkoutPlayer

import android.os.Bundle
import android.speech.tts.TextToSpeech
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.catglo.openphysicaltherapy.Data.ExerciseRepository
import com.catglo.openphysicaltherapy.Data.Workout
import com.catglo.openphysicaltherapy.Data.WorkoutRepository
import com.catglo.openphysicaltherapy.secondsToInterval
import com.catglo.openphysicaltherapy.ui.theme.OpenPhysicalTherapyTheme
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale

@AndroidEntryPoint
class WorkoutPlayerActivity : ComponentActivity() {
    private  var  textToSpeech: TextToSpeech? = null

    fun textToSpeech(text: String) {
        textToSpeech = TextToSpeech(
            this
        ) {
            if (it == TextToSpeech.SUCCESS) {
                textToSpeech?.let { txtToSpeech ->
                    txtToSpeech.language = Locale.US
                    txtToSpeech.setSpeechRate(1.0f)
                    txtToSpeech.speak(
                        text,
                        TextToSpeech.QUEUE_ADD,
                        null,
                        null
                    )
                }
            }

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val workoutToPlay = intent.getStringExtra("Workout")
        if (workoutToPlay == null){
            finish()
            return
        }

        val workout = WorkoutRepository(this).getWorkout(workoutToPlay)
        val exercises = workout?.exercises ?: listOf()

        enableEdgeToEdge()
        setContent {
            var currentExerciseIndex by remember { mutableIntStateOf(0) }
            var currentExerciseFileName by remember { mutableStateOf(exercises[0].fileName) }
            var showExerciseIntro by remember { mutableStateOf(true) }

            OpenPhysicalTherapyTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(modifier = Modifier.padding(innerPadding)) {
                        if (showExerciseIntro) {
                            ExerciseIntroView(exerciseToPlay = currentExerciseFileName, textToSpeech = {
                                textToSpeech(it)
                            }) {
                                showExerciseIntro = false
                            }
                        }
                        else {
                            PlayExerciseView(exerciseToPlay = currentExerciseFileName) {
                                if (currentExerciseIndex < exercises.size - 1) {
                                    currentExerciseIndex++
                                    currentExerciseFileName =
                                        exercises[currentExerciseIndex].fileName
                                    showExerciseIntro = true
                                } else {
                                    finish()
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ExerciseIntroView(exerciseToPlay: String, textToSpeech: ((text:String)->Unit)? = null, onOkClicked: () -> Unit) {
    val exercise = ExerciseRepository(LocalContext.current).getExercise(exerciseToPlay)
    textToSpeech?.invoke("You're next exercise is ${exercise?.name}")
    Column(horizontalAlignment = Alignment.CenterHorizontally){
        Spacer(modifier = Modifier.height(50.dp))
        val name = exercise?.name ?: ""
        Text(text = "You're next exercise is $name",
            fontSize = 32.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(15.dp),
            lineHeight = 40.sp)

        Spacer(modifier = Modifier.weight(1f))

        val totalDuration = (exercise?.totalDuration() ?: 0).secondsToInterval()
        Text("Time")
        Text(text = totalDuration)
        Spacer(modifier = Modifier.height(50.dp))


        Spacer(modifier = Modifier.weight(1f))
        Button(onClick = { onOkClicked() }) {
            Text(text = "Continue")
        }
        Spacer(modifier = Modifier.height(50.dp))
    }
}
