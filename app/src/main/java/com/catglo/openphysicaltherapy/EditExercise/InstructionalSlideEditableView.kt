package com.catglo.openphysicaltherapy.EditExercise

import android.net.Uri
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.catglo.openphysicaltherapy.Data.InstructionalSlide
import com.catglo.openphysicaltherapy.OpenPhysicalTherapyApplication
import com.catglo.openphysicaltherapy.R
import com.catglo.openphysicaltherapy.Widgets.ImagePickCaptureButton
import com.catglo.openphysicaltherapy.Widgets.NumberPickerTextField
import com.catglo.openphysicaltherapy.viewModelFactory
import kotlin.reflect.jvm.internal.impl.descriptors.Visibilities.Local

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditableInstructionalSlideView(
    slideIndex: Int,
    slide: InstructionalSlide,
    stepIndex: Int,
    stepViewModel: ExerciseStepViewModel,
    exerciseName: String
) {
    val application = LocalContext.current.applicationContext as OpenPhysicalTherapyApplication
    val slideViewModel = viewModel<InstructionalSlideViewModel>(
        key = "$stepIndex - $slideIndex",
        factory = viewModelFactory {
            InstructionalSlideViewModel(exerciseName,slide,application)
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
        Spacer(modifier = Modifier.width(35.dp))
        ImagePickCaptureButton(
            onImageFilePicked = { uri ->
                Log.i("file","got image $uri")
                slideViewModel.updateImage(uri)
            },
            onVideoFilePicked = { uri ->
                Log.i("file","got video $uri")
                slideViewModel.updateVideo(uri)
            })
        IconButton(onClick = {

        }) {
            Icon(imageVector = ImageVector.vectorResource(id = R.drawable.icon_add_audio),
                contentDescription = "Add Audio")
        }
    }
}