package com.catglo.openphysicaltherapy.EditExercise

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
import com.catglo.openphysicaltherapy.Data.ExerciseStep
import com.catglo.openphysicaltherapy.Widgets.NumberPickerTextField
import com.catglo.openphysicaltherapy.R

@Composable
fun ExerciseStepView(editExerciseViewModel:EditExerciseViewModel, stepIndex: Int) {

    val slides = editExerciseViewModel.getSlides(stepIndex)

    LazyColumn {

        items(slides.size) { slideIndex ->
            EditableInstructionalSlideView(
                stepIndex = stepIndex,
                slideIndex = slideIndex,
                editExerciseViewModel = editExerciseViewModel
            )
        }
        item {
            Row(modifier = Modifier.fillMaxWidth()) {
                Spacer(modifier = Modifier.weight(1f))
                Button(onClick = {
                    //exercise.steps[page].addSlides()
                    editExerciseViewModel.addSlide(stepIndex)
                }) {
                    Text("Add slide")
                }
            }
        }
    }
}