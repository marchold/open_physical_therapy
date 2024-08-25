package com.catglo.openphysicaltherapy.WorkoutPlayer

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.OptIn
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.catglo.openphysicaltherapy.EditExercise.EditExerciseViewModel
import com.catglo.openphysicaltherapy.EditExercise.InstructionalSlideViewModel
import com.catglo.openphysicaltherapy.OpenPhysicalTherapyApplication
import com.catglo.openphysicaltherapy.ui.theme.OpenPhysicalTherapyTheme
import com.catglo.openphysicaltherapy.viewModelFactory
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay

@AndroidEntryPoint
class ExercisePreviewActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val exerciseToPlay = intent.getStringExtra("Exercise")
        if (exerciseToPlay == null){
            finish()
        } else {
            enableEdgeToEdge()
            setContent {
                OpenPhysicalTherapyTheme {
                    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                        Box(modifier = Modifier.padding(innerPadding)) {
                            PlayExerciseView(exerciseToPlay)
                        }
                    }
                }
            }
        }
    }
}


@OptIn(UnstableApi::class)
@Composable
fun PlayExerciseView(exerciseToPlay: String) {
    val exerciseViewModel = hiltViewModel<EditExerciseViewModel>()
    exerciseViewModel.load(exerciseToPlay)

    var currentSlide by remember { mutableStateOf(0) }
    var currentStep by remember { mutableStateOf(0) }

    var slide = exerciseViewModel.getExerciseSteps()[currentStep].slides[currentSlide]
    val application = LocalContext.current.applicationContext as OpenPhysicalTherapyApplication
    val slideViewModel = viewModel<InstructionalSlideViewModel>(
        key = "$currentStep - $currentSlide",
        factory = viewModelFactory {
            InstructionalSlideViewModel(exerciseToPlay,slide,application)
        })
    val slideImage = slideViewModel.getImageFile()

    var showGoodWorkScreen by remember { mutableStateOf(false) }

    var countdownTimerValue by remember { mutableStateOf(slide.duration) }

    LaunchedEffect(key1 = countdownTimerValue) {
        while (countdownTimerValue > 0) {
            delay(1000L)
            countdownTimerValue--
            if (countdownTimerValue==0){
                if (currentSlide < exerciseViewModel.getExerciseSteps()[currentStep].slides.size-1) {
                    currentSlide++
                    slide = exerciseViewModel.getExerciseSteps()[currentStep].slides[currentSlide]
                    countdownTimerValue = slide.duration
                } else {
                    if (currentStep < exerciseViewModel.getExerciseSteps().size-1) {
                        currentStep++
                        currentSlide = 0
                        slide = exerciseViewModel.getExerciseSteps()[currentStep].slides[currentSlide]
                        countdownTimerValue = slide.duration
                    }
                    else {
                        //Show good work screen and an exit button
                        showGoodWorkScreen = true
                    }
                }
            }
        }
    }

    if (showGoodWorkScreen) {
        val context = LocalContext.current
        Button(onClick = { (context as? Activity)?.finish() }) {
            Text(text = "Good Work")
        }
    }
    else {

        Column {
            Spacer(modifier = Modifier.height(20.dp))
            Box(Modifier.padding(top = 20.dp)) {
                Column(
                    Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val backgroundColor = MaterialTheme.colorScheme.background
                    val onBackgroundColor = MaterialTheme.colorScheme.onBackground
                    Text(
                        text = countdownTimerValue.toString(),
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
    }
}