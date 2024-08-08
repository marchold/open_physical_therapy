package com.example.openphysicaltherapy

import android.view.LayoutInflater
import android.widget.DatePicker
import android.widget.NumberPicker
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun NumberPicker(
    onNumberSelected: (Int) -> Unit,
    value:Int=0,
    minimumValue:Int=0,
    maximumValue:Int=10
) {
    AndroidView(
        modifier = Modifier.fillMaxWidth(),
        factory = { context ->
            val view = LayoutInflater.from(context).inflate(R.layout.number_picker, null)
            val numberPicker = view.findViewById<NumberPicker>(R.id.numberPicker)
            numberPicker.maxValue = maximumValue
            numberPicker.minValue = minimumValue
            numberPicker.value = value
            numberPicker
        }
    )
}