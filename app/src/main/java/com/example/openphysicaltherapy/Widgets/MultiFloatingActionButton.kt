package com.example.openphysicaltherapy.Widgets

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.openphysicaltherapy.Widgets.FloatingButtonState.Collapsed
import com.example.openphysicaltherapy.Widgets.FloatingButtonState.Expanded

data class FloatingButtonItem(
    val iconResource: ImageVector,
    val label: String,
    val onClick: () -> Unit)

enum class FloatingButtonState {
    Expanded,
    Collapsed;
}

@Composable
fun rememberMultiFabState() =
    remember { mutableStateOf(Collapsed) }


@Composable
fun MultiFloatingActionButton(
    modifier: Modifier = Modifier,
    items: List<FloatingButtonItem>,
    state: MutableState<FloatingButtonState> = rememberMultiFabState(),
    icon: ImageVector,
    stateChanged: (floatingButtonState: FloatingButtonState) -> Unit = {}
) {
    val rotation by animateFloatAsState(
        if (state.value == FloatingButtonState.Expanded) { 45f } else {  0f },
        label = "FloatingActionButtonRotation"
    )

    Column(
        modifier = modifier.wrapContentSize(),
        horizontalAlignment = Alignment.End
    ) {
        AnimatedVisibility(
            visible = state.value == Expanded,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            LazyColumn(
                modifier = Modifier
                    .wrapContentSize()
                    .padding(bottom = 15.dp),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(15.dp)
            ) {

                items(items.size) { index ->
                    FloatingActionButtonItem(
                        item = items[index],state
                    )
                    if (index == items.size-1) {
                        Spacer(modifier = Modifier.size(14.dp))
                    }
                }

            }
        }
        // Main FloatingActionButton
        FloatingActionButton(
            onClick = {
                state.value = when (state.value){
                    Expanded -> Collapsed
                    Collapsed -> Expanded
                }
                stateChanged(state.value)
            },
            containerColor = MaterialTheme.colorScheme.background,
            shape = CircleShape
        ) {
            Icon(
                imageVector = icon,
                contentDescription = "stringResource(R.string.main_fab_button)",
                modifier = Modifier.rotate(rotation),
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}


@Composable
fun FloatingActionButtonItem(
    item: FloatingButtonItem,
    state: MutableState<FloatingButtonState>
) {
    Row(
        modifier = Modifier
            .wrapContentSize()
            .padding(end = 15.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = item.label,
            style = typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(all = 8.dp)
        )
        FloatingActionButton(
            onClick =
            {
                item.onClick()
                state.value = Collapsed
            },
            modifier = Modifier.size(40.dp),
            containerColor = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.primary,
            shape = CircleShape
        ) {
            Icon(
                imageVector = item.iconResource,
                contentDescription = "FloatingButtonMainIcon",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}