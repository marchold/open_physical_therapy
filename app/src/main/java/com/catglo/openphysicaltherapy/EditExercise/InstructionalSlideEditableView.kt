package com.catglo.openphysicaltherapy.EditExercise

import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.catglo.openphysicaltherapy.Data.InstructionalSlide
import com.catglo.openphysicaltherapy.OpenPhysicalTherapyApplication
import com.catglo.openphysicaltherapy.R
import com.catglo.openphysicaltherapy.Widgets.ImagePickCaptureButton
import com.catglo.openphysicaltherapy.Widgets.NumberPickerTextField
import com.catglo.openphysicaltherapy.secondsToInterval
import com.catglo.openphysicaltherapy.viewModelFactory


@androidx.annotation.OptIn(UnstableApi::class)
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
    val slideImage = slideViewModel.getImageFile()

    Box(
        modifier = Modifier.fillMaxWidth()
    ) {
        key(slideViewModel.mediaUriKey.value) {
            Box {
                if ((slideImage?.exists()) != true) {
                    Image(
                        painterResource(R.drawable.landscape_placeholder_svgrepo_com),
                        "placeholder image",
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight()
                    )
                }

                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current).data(slideImage).build(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(500.dp),
                    contentDescription = null,
                )

                slideViewModel.videoFileUri()?.path?.let { path ->
                    val exoPlayer = ExoPlayer.Builder(LocalContext.current).build()
                    AndroidView(
                        modifier = Modifier
                            .fillMaxSize()
                            .height(400.dp),
                        factory = { context ->
                            PlayerView(context).apply {
                                player = exoPlayer
                                hideController()
                                player?.repeatMode = Player.REPEAT_MODE_ONE
                                //player = viewModel.player
                                //artworkDisplayMode = PlayerView.ARTWORK_DISPLAY_MODE_FIT
                                //artworkPlaceHolder?.let { defaultArtwork = it }
                            }
                        }
                    )
                    val mediaItem = MediaItem.fromUri(slideViewModel.videoFileUri()!!)
                    // Set the media item to be played.
                    exoPlayer.setMediaItem(mediaItem)
                    // Prepare the player.
                    exoPlayer.prepare()
                    // Start the playback.
                    exoPlayer.play()
                }
            }
        }
        if (slideIndex > 0) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .zIndex(1f),
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

        NumberPickerTextField(
            intLiveData = slideViewModel.duration,
            minimumValue = 3,
            maximumValue = 500,
            title = "Duration in seconds",
            previewView = { intLiveDataParam, showBottomSheet ->
                Box(Modifier.padding(top = 20.dp)) {
                    Column(
                        Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.alpha(
                                if (slideViewModel.showCountdown.value) {
                                    1f
                                } else {
                                    0.5f
                                }
                            )
                        ) {
                            val backgroundColor = MaterialTheme.colorScheme.background
                            val onBackgroundColor = MaterialTheme.colorScheme.onBackground
                            Text(
                                text = intLiveDataParam.value.toString(),
                                fontWeight = FontWeight.Bold,
                                fontSize = 32.sp,
                                modifier = Modifier
                                    .drawBehind {
                                        drawCircle(color = backgroundColor)
                                        drawCircle(
                                            color = onBackgroundColor,
                                            style = Stroke(width = 10f)
                                        )
                                    }
                                    .padding(16.dp))
                            Icon(
                                Icons.Outlined.Edit,
                                "Edit Icon",
                                modifier = Modifier
                                    .padding(top = 45.dp)
                                    .size(20.dp)
                            )
                        }
                    }

                }
            },
            extraControls = {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "Show countdown")
                        Switch(
                            checked = slideViewModel.showCountdown.value,
                            onCheckedChange = {
                                slideViewModel.updateShowCountdown(it)
                            },
                            modifier = Modifier.padding(start = 10.dp)
                        )
                    }
                }
            },
            formatter = { it.secondsToInterval() }
        ) {
            slideViewModel.updateDuration(it)
        }

        TextField(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter),
            value = slideViewModel.text,
            onValueChange = { newValue ->
                slideViewModel.updateText(newValue)
            },
            label = {
                Text(text = "Instructional text")
            }
        )


        ImagePickCaptureButton(
            onImageFilePicked = { uri ->
                Log.i("file", "got image $uri")
                slideViewModel.updateImage(uri)
                slideViewModel.mediaUriKey.value = uri.path ?: ""
            },
            onVideoFilePicked = { uri ->
                Log.i("file", "got video $uri")
                slideViewModel.updateVideo(uri)
                slideViewModel.mediaUriKey.value = uri.path ?: ""
            },
            imageVector = if (slideViewModel.hasVisualMedia()) {
                ImageVector.vectorResource(id = R.drawable.icon_image_edit)
            } else {
                ImageVector.vectorResource(id = R.drawable.icon_add_image)
            }
        )


    }

}
