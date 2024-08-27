package com.catglo.openphysicaltherapy.WorkoutPlayer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.catglo.openphysicaltherapy.Data.Workout
import com.catglo.openphysicaltherapy.Data.WorkoutRepository
import com.catglo.openphysicaltherapy.ui.theme.OpenPhysicalTherapyTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class WorkoutPlayerActivity : ComponentActivity() {
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
            OpenPhysicalTherapyTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(modifier = Modifier.padding(innerPadding)) {
                        PlayExerciseView(exerciseToPlay = currentExerciseFileName) {
                            if (currentExerciseIndex < exercises.size - 1) {
                                currentExerciseIndex++
                                currentExerciseFileName = exercises[currentExerciseIndex].fileName
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
