package com.example.openphysicaltherapy.Widgets

import android.view.LayoutInflater
import android.widget.NumberPicker
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.openphysicaltherapy.R
import kotlinx.coroutines.launch

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
            numberPicker.setOnValueChangedListener { numberPicker, i, i2 ->
                onNumberSelected(i2)
            }
            numberPicker
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NumberPickerTextField(
    intLiveData: MutableState<Int>,
    icon: ImageVector,
    minimumValue:Int,
    maximumValue:Int,
    title:String,
    onNumberSelected:(Int)->Unit)
{
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    var showBottomSheet by remember { mutableStateOf(false) }

    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = "$title Icon",
            modifier = Modifier.padding(start = 10.dp, top=0.dp, end=10.dp, bottom = 0.dp))
        Box(modifier = Modifier
            .fillMaxWidth()
            .clickable {
                if (!showBottomSheet) {
                    showBottomSheet = true
                }
            }) {
            TextField(
                value = intLiveData.value.toString(),
                onValueChange = { },
                enabled = false,
                readOnly = true,
                modifier = Modifier.fillMaxWidth(),
                colors = when (showBottomSheet) {
                    true -> TextFieldDefaults.colors(
                        disabledLabelColor = MaterialTheme.colorScheme.primary,
                        disabledTextColor = MaterialTheme.colorScheme.onBackground,
                        disabledIndicatorColor = MaterialTheme.colorScheme.primary,
                    )

                    false -> TextFieldDefaults.colors(
                        disabledLabelColor = MaterialTheme.colorScheme.onBackground,
                        disabledTextColor = MaterialTheme.colorScheme.onBackground
                    )
                },
                label = { Text(text = title) }
            )
        }
       // HorizontalDivider(color = Color.Blue, thickness = 1.dp)
    }

    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = {
                showBottomSheet = false
            },
            sheetState = sheetState
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                IconButton(
                    onClick =
                    {
                        scope.launch { sheetState.hide() }.invokeOnCompletion {
                            if (!sheetState.isVisible) {
                                showBottomSheet = false
                            }
                        }
                    }, modifier = Modifier
                ) {
                    Icon(Icons.Filled.Close, contentDescription = "Close Button")
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier
                    .fillMaxWidth()
                    .align(alignment = Alignment.Center)) {
                    Text(text = title.uppercase(), fontWeight = FontWeight.Bold)
                }
            }
            NumberPicker(
                onNumberSelected = onNumberSelected,
                value = intLiveData.value!!,
                minimumValue = minimumValue,
                maximumValue = maximumValue
            )
        }
    }
}