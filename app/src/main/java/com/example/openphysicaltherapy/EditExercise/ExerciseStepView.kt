package com.example.openphysicaltherapy.EditExercise

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.openphysicaltherapy.Data.ExerciseStep
import com.example.openphysicaltherapy.Widgets.NumberPickerTextField
import com.example.openphysicaltherapy.R
import com.example.openphysicaltherapy.viewModelFactory

@Composable
fun ExerciseStepView(step: ExerciseStep, stepIndex: Int) {

    val stepViewModel = viewModel<ExerciseStepViewModel>(
        key = stepIndex.toString(),
        factory = viewModelFactory { ExerciseStepViewModel(step) })

    val slides = stepViewModel.getSlides()

    LazyColumn {
        item {
            NumberPickerTextField(
                intLiveData = stepViewModel.numberOfReps,
                icon = ImageVector.vectorResource(R.drawable.icon_repeat),
                minimumValue = 1,
                maximumValue = 20,
                "Number of reps"
            ) {
                stepViewModel.updateNumberOfReps(it)
            }
        }
        items(slides.size) { slideIndex ->
            EditableInstructionalSlideView(
                stepIndex = stepIndex,
                slideIndex = slideIndex,
                slide = slides[slideIndex],
                stepViewModel = stepViewModel
            )
        }
        item {
            Row(modifier = Modifier.fillMaxWidth()) {
                Spacer(modifier = Modifier.weight(1f))
                Button(onClick = {
                    //exercise.steps[page].addSlides()
                    stepViewModel.addSlide()
                }) {
                    Text("Add slide")
                }
            }
        }
    }
}