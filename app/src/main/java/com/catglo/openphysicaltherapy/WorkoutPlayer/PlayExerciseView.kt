package com.catglo.openphysicaltherapy.WorkoutPlayer

import androidx.annotation.OptIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.painterResource
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
import com.catglo.openphysicaltherapy.R
import com.catglo.openphysicaltherapy.Widgets.CircleSegmentCounter
import com.catglo.openphysicaltherapy.Widgets.CircleSegmentCounterDirection.*
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
        viewModel.textToSpeech(viewModel.instructionText)
    }

    LaunchedEffect(key1 = viewModel.countdownTimerValue.value) {
        while (viewModel.hasMoreSlides()) {
            delay(1000L)
            viewModel.onCountdownTick()
        }
    }

    if (viewModel.isDoneWithExercise) {
        onExerciseComplete()
    }
    else {
        Box(modifier = Modifier.fillMaxSize()) {
            Column {
                Spacer(modifier = Modifier.height(20.dp))
                Box(
                    Modifier
                        .padding(top = 20.dp)
                        .height(70.dp)) {
                    if (viewModel.getSlide().countdown) {
                        Box(
                            Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painterResource(R.drawable.stopwatch),
                                contentDescription = "",
                                contentScale = ContentScale.Inside,
                                modifier = Modifier.fillMaxSize()
                            )
                            if ((countdownValue.value ?: 0) <= 5) {
                                Box(modifier = Modifier.padding(top=13.dp)) {
                                    CircleSegmentCounter(
                                        numberOfArcSegments = 5,
                                        numberOfHighlightedSegments = countdownValue.value ?: 0,
                                        boxSize = 50.dp,
                                        strokeWidth = 10f,
                                        arcColorToDo = Color.Black,
                                        arcColorDone = Color.White,
                                        gap=5
                                    )
                                }
                            }
                            Text(
                                text = countdownValue.value?.toString() ?: "",
                                fontWeight = FontWeight.Bold,
                                fontSize = 27.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .padding(start = 16.dp, top = 25.dp, end = 16.dp, bottom = 16.dp)
                                    .width(50.dp))
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
            Box(modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 20.dp, bottom = 10.dp)) {
                CircleSegmentCounter(
                    numberOfArcSegments = viewModel.totalNumberOfReps,
                    numberOfHighlightedSegments = viewModel.repsCountdownValue,
                    boxSize = 70.dp,
                    direction = RIGHT)
                Text(text = "${viewModel.totalNumberOfReps-viewModel.repsCountdownValue+1}/${viewModel.totalNumberOfReps}",
                    modifier = Modifier.align(Alignment.Center),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold)
            }
        }
    }
}

