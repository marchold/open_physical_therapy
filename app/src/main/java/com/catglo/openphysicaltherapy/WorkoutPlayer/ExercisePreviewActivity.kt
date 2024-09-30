package com.catglo.openphysicaltherapy.WorkoutPlayer

import android.app.Activity
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.catglo.openphysicaltherapy.ui.theme.OpenPhysicalTherapyTheme
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale

@AndroidEntryPoint
class ExercisePreviewActivity : ComponentActivity() {
    private var textToSpeech : TextToSpeech? = null
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
                            PlayExerciseView(exerciseToPlay){
                                finish()
                            }
                        }
                    }
                }
            }
        }
    }

    public override fun onDestroy() {
        textToSpeech?.apply {
            stop()
            shutdown()
        }
        super.onDestroy()
    }

}


