package com.catglo.openphysicaltherapy.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.AbsoluteAlignment
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.catglo.openphysicaltherapy.Data.Exercise
import com.catglo.openphysicaltherapy.Data.ExerciseListItem
import com.catglo.openphysicaltherapy.Data.ExerciseRepository
import com.catglo.openphysicaltherapy.secondsToInterval

@Composable
fun ExerciseListItemView(exerciseListItem: ExerciseListItem, onClick: (() -> Unit)? = null) {
    var totalDuration by remember { mutableStateOf("") }
    var numberOfRepsText by remember { mutableStateOf("") }
    val context = LocalContext.current
    var exercise : Exercise?
    LaunchedEffect(key1 = Unit) {
        exercise = ExerciseRepository(context).getExercise(exerciseListItem.fileName)
        exercise?.totalDuration()?.secondsToInterval()?.let { timeIntervalString ->
            totalDuration = timeIntervalString
        }
        exercise?.numberOfReps?.let { numberOfReps ->
            numberOfRepsText = "$numberOfReps reps"
        }
    }
    Column {
        Row(
            modifier = Modifier
                .height(55.dp)
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
                .clickable(enabled = onClick != null)
                {
                    if (onClick != null) onClick()
                },
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = exerciseListItem.name,
                modifier = Modifier.padding(10.dp)
            )
            Spacer(modifier = Modifier.weight(1f))
            Column(horizontalAlignment = AbsoluteAlignment.Right) {
                Spacer(modifier = Modifier.height(5.dp))
                Text(text = totalDuration, fontSize = 15.sp)
                Spacer(modifier = Modifier.weight(1f))
                Text(text = numberOfRepsText, fontSize = 15.sp)
                Spacer(modifier = Modifier.height(5.dp))
            }
            Spacer(modifier = Modifier.width(15.dp))
        }
        HorizontalDivider()
    }
}