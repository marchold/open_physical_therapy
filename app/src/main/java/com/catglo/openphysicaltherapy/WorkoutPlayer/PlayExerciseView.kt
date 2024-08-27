package com.catglo.openphysicaltherapy.WorkoutPlayer

import android.app.Activity
import androidx.annotation.OptIn
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import coil.request.ImageRequest
import kotlinx.coroutines.delay

@OptIn(UnstableApi::class)
@Composable
fun PlayExerciseView(exerciseToPlay: String,
                     onExerciseComplete: @Composable ()->Unit)
{
    val viewModel = hiltViewModel<PlayExerciseViewModel>(key = exerciseToPlay)
    val slideImage = viewModel.getImageFile()

    val countdownValue = viewModel.countdownTimerValue.observeAsState()

    LaunchedEffect(key1 = exerciseToPlay) {
        viewModel.load(exerciseToPlay)
    }

    LaunchedEffect(key1 = viewModel.countdownTimerValue.value) {
        while ((viewModel.countdownTimerValue.value ?: 0) > 0) {
            delay(1000L)
            viewModel.onCountdownTick()
        }
    }

    if (viewModel.isDoneWithExercise) {
        onExerciseComplete()
    }
    else {

        Column {
            Spacer(modifier = Modifier.height(20.dp))
            Box(Modifier.padding(top = 20.dp).height(60.dp)) {
                if (viewModel.getSlide().countdown) {
                    Column(
                        Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        val backgroundColor = MaterialTheme.colorScheme.background
                        val onBackgroundColor = MaterialTheme.colorScheme.onBackground
                        Text(
                            text = countdownValue.value?.toString() ?: "",
                            fontWeight = FontWeight.Bold,
                            fontSize = 27.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .drawBehind {
                                    drawCircle(color = backgroundColor)
                                    drawCircle(
                                        color = onBackgroundColor,
                                        style = Stroke(width = 10f)
                                    )
                                }
                                .padding(16.dp).width(50.dp))
                    }
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
            Box {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current).data(slideImage).build(),
                    modifier = Modifier
                        .fillMaxWidth(),
                    contentDescription = null,
                )

                viewModel.videoFileUri()?.path?.let { path ->
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
                    val mediaItem = MediaItem.fromUri(path)
                    // Set the media item to be played.
                    exoPlayer.setMediaItem(mediaItem)
                    // Prepare the player.
                    exoPlayer.prepare()
                    // Start the playback.
                    exoPlayer.play()
                }
            }
            Text(
                viewModel.instructionText,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(top = 20.dp),
                fontSize = 20.sp,
            )
        }
    }
}