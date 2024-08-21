package com.example.openphysicaltherapy.EditExercise

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.openphysicaltherapy.Data.InstructionalSlide
import com.example.openphysicaltherapy.Widgets.NumberPickerTextField
import com.example.openphysicaltherapy.R
import com.example.openphysicaltherapy.viewModelFactory

@Composable
fun EditableInstructionalSlideView(
    slideIndex: Int,
    slide: InstructionalSlide,
    stepIndex: Int,
    stepViewModel: ExerciseStepViewModel
) {
    val slideViewModel = viewModel<InstructionalSlideViewModel>(
        key = "$stepIndex - $slideIndex",
        factory = viewModelFactory {
            InstructionalSlideViewModel(slide)
        })


    Box {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Slide ${slideIndex + 1}")
        }
        if (slideIndex > 0) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Center
            ) {
                IconButton(onClick = {
                    stepViewModel.removeSlide(slideIndex)
                }) {
                    Icon(Icons.Outlined.Close, contentDescription = "Remove Slide Icon")
                }
            }
        }
    }
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Outlined.Info,
            contentDescription = "Information Icon",
            modifier = Modifier.padding(start = 10.dp, top = 0.dp, end = 10.dp, bottom = 0.dp)
        )
        TextField(
            modifier = Modifier.fillMaxWidth(),
            value = slideViewModel.text,
            onValueChange = { newValue ->
                slideViewModel.updateText(newValue)
            },
            label = {
                Text(text = "Instructional text")
            }
        )
    }

    NumberPickerTextField(
        intLiveData = slideViewModel.duration,
        icon = ImageVector.vectorResource(R.drawable.icon_timer),
        minimumValue = 3,
        maximumValue = 500,
        title = "Duration in seconds"
    ) {
        slideViewModel.updateDuration(it)
    }
    Row {
        // TODO:
        //   Icon (image, video)
        //   Video/Audio+Image file picker
    }
}