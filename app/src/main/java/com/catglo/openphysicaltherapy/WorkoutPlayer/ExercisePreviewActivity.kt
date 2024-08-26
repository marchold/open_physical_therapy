package com.catglo.openphysicaltherapy.WorkoutPlayer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.catglo.openphysicaltherapy.ui.theme.OpenPhysicalTherapyTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ExercisePreviewActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val exerciseToPlay = intent.getStringExtra("Exercise")
        if (exerciseToPlay == null){
            finish()
        } else {
            enableEdgeToEdge()
            setContent {
                OpenPhysicalTherapyTheme {
                    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                        Box(modifier = Modifier.padding(innerPadding)) {
                            PlayExerciseView(exerciseToPlay)
                        }
                    }
                }
            }
        }
    }
}


