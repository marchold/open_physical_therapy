package com.catglo.openphysicaltherapy.WorkoutPlayer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.catglo.openphysicaltherapy.ui.theme.OpenPhysicalTherapyTheme

class WorkoutPlayerActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val workoutToPlay = intent.getStringExtra("Workout")
        if (workoutToPlay == null){
            finish()
        }
        enableEdgeToEdge()
        setContent {
            OpenPhysicalTherapyTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(modifier = Modifier.padding(innerPadding)) {

                    }
                }
            }
        }
    }
}
