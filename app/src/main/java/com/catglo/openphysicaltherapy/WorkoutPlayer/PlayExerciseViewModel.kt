package com.catglo.openphysicaltherapy.WorkoutPlayer

import android.content.Context
import android.net.Uri
import android.speech.tts.TextToSpeech
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.catglo.openphysicaltherapy.Data.Exercise
import com.catglo.openphysicaltherapy.Data.ExerciseRepository
import com.catglo.openphysicaltherapy.Data.ExerciseStep
import com.catglo.openphysicaltherapy.Data.InstructionalSlide
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class PlayExerciseViewModel @Inject constructor(private val repo: ExerciseRepository, @ApplicationContext val context: Context) : ViewModel() {
    val totalNumberOfReps: Int
        get() = exercise.numberOfReps

    private var exercise = Exercise("")

    private  var  textToSpeech: TextToSpeech? = null

    fun textToSpeech(text: String) {
        textToSpeech = TextToSpeech(
            context
        ) {
            if (it == TextToSpeech.SUCCESS) {
                textToSpeech?.let { txtToSpeech ->
                    txtToSpeech.language = Locale.US
                    txtToSpeech.setSpeechRate(1.0f)
                    txtToSpeech.speak(
                        text,
                        TextToSpeech.QUEUE_ADD,
                        null,
                        null
                    )
                }
            }

        }
    }

    val currentStep = mutableIntStateOf(0)
    val currentSlide = mutableIntStateOf(0)


    private var _exerciseSteps = exercise.steps.toMutableStateList()
    fun getExerciseSteps():List<ExerciseStep>{
        return _exerciseSteps
    }

    private var _countdownTimerValue = MutableLiveData<Int>(getSlideDuration())
    val countdownTimerValue : LiveData<Int> = _countdownTimerValue

    var instructionText by mutableStateOf(getSlide().text)

    private val _name = MutableLiveData<String>(exercise.name)
    val name: LiveData<String> = _name

    var isDoneWithExercise by mutableStateOf(false)
        private set

    fun load(fileName:String) {
        repo.getExercise(fileName)?.let {
            exercise = it
            _name.value = it.name
            _exerciseSteps = exercise.steps.toMutableStateList()
            _countdownTimerValue.value = getSlideDuration()
            slideSwitchCountdown = getSlide().duration
            instructionText = getSlide().text
            repsCountdownValue = exercise.numberOfReps
        }
    }

    fun getSlide(): InstructionalSlide {
        return _exerciseSteps[currentStep.intValue].slides[currentSlide.intValue]
    }

    fun getSlideDuration(): Int {
        var duration = 0
        _exerciseSteps.forEach { step ->
            step.slides.forEach {
                duration += it.duration
            }
        }
        return duration
    }

    fun getImageFile(): File? {
        return getSlide().getImageFile(exercise.fileName, context)
    }

    fun videoFileUri(): Uri? {
        return getSlide().videoFileUri(context, exercise.fileName)
    }

    private var slideSwitchCountdown = getSlide().duration
    var repsCountdownValue by mutableIntStateOf(exercise.numberOfReps)
        private set

    fun onCountdownTick() {
        _countdownTimerValue.value = _countdownTimerValue.value?.minus(1)
        slideSwitchCountdown--

        if ((_countdownTimerValue.value ?: 1) <= 0 && slideSwitchCountdown <= 0){
            repsCountdownValue--
            if (repsCountdownValue == 0) {
                isDoneWithExercise = true
            } else {
                currentStep.intValue = 0
                currentSlide.intValue = 0
                slideSwitchCountdown = getSlide().duration
                instructionText = getSlide().text
                _countdownTimerValue.value = getSlideDuration()
                textToSpeech(instructionText)

            }
        }
        else
        if ((_countdownTimerValue.value ?: 1) <= 0){
            currentStep.intValue++
            currentSlide.intValue = 0
            instructionText = getSlide().text
            slideSwitchCountdown = getSlide().duration
            textToSpeech(instructionText)
        }
        else if (slideSwitchCountdown <= 0){
            if (currentSlide.intValue < getExerciseSteps()[currentStep.intValue].slides.size-1) {
                currentSlide.intValue++
            }
            else if (currentStep.intValue < getExerciseSteps().size-1) {
                currentStep.intValue++
                currentSlide.intValue = 0
            }
            instructionText = getSlide().text
            slideSwitchCountdown = getSlide().duration
            textToSpeech(instructionText)
        }

    }

    fun hasMoreSlides(): Boolean {
        return repsCountdownValue>0 || slideSwitchCountdown>0
    }
}