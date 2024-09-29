package com.catglo.openphysicaltherapy.EditExercise

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.catglo.openphysicaltherapy.Data.Exercise
import com.catglo.openphysicaltherapy.Data.ExerciseListItem
import com.catglo.openphysicaltherapy.Data.ExerciseRepository
import com.catglo.openphysicaltherapy.Data.ExerciseStep
import com.catglo.openphysicaltherapy.Data.InstructionalSlide
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

@HiltViewModel
open class EditExerciseViewModel @Inject constructor(private val repo: ExerciseRepository,
                                                     @ApplicationContext val context: Context,
                                                     private val state: SavedStateHandle
) : ViewModel() {

    private var exercise = Exercise("")
    init {
       state.get<String>("EditExercise")?.let {
           exercise = repo.getExercise(it) ?: Exercise("")
       }
    }

    var hasBeenEdited by mutableStateOf(false)
        private set

    private var _exerciseSteps = exercise.steps.toMutableStateList()
    fun getExerciseSteps():List<ExerciseStep>{
        return _exerciseSteps
    }
    fun addStep(){
        _exerciseSteps.add(ExerciseStep(1))
        exercise.steps = _exerciseSteps
        hasBeenEdited = true
    }

    fun removeStep(stepIndexToRemove: Int) {
        _exerciseSteps.removeAt(stepIndexToRemove)
        exercise.steps = _exerciseSteps
        hasBeenEdited = true
    }


    private val _name = MutableLiveData<String>(exercise.name)
    val name: LiveData<String> = _name
    fun updateName(newName:String){
        _name.value = newName
        exercise.name = newName
        hasBeenEdited = true
    }

    fun save(){
        repo.saveExercise(exercise)
    }

    fun load(fileName:String) {
        repo.getExercise(fileName)?.let {
            exercise = it
            _name.value = it.name
            _exerciseSteps.clear()
            exercise.steps.forEach { _exerciseSteps.add(it) }
        }
    }

    private var listOfExercises : List<ExerciseListItem>? = null
    fun isExerciseNameUnique(name:String):Boolean{
        if (listOfExercises == null){
            listOfExercises = ExerciseRepository(context).getExerciseList()
        }
        listOfExercises?.forEach {
            if (it.name == name) {
                return false
            }
        }
        return true
    }

    fun fileName(): String {
        return exercise.fileName
    }

    fun saveForPreview() {
        repo.saveExercise(exercise, true)
    }

    fun exportExercise() : File {
        return repo.exportExercise(exercise)
    }

    fun prettyFileName(): String {
        return exercise.prettyFileName()
    }

    fun getSlides(exerciseStepIndex:Int):List<InstructionalSlide>{
        return _exerciseSteps[exerciseStepIndex].slides
    }
    fun addSlide(exerciseStepIndex:Int){
        exercise.steps[exerciseStepIndex].slides.add(InstructionalSlide())
        hasBeenEdited = true
    }
    fun removeSlide(exerciseStepIndex:Int,slideIndex: Int) {
        exercise.steps[exerciseStepIndex].slides.removeAt(slideIndex)
        hasBeenEdited = true
    }
    fun updateNumberOfReps(exerciseStepIndex:Int,newNumberOfReps:Int){
        exercise.steps[exerciseStepIndex].numberOfReps = newNumberOfReps
        hasBeenEdited = true
    }

    fun numberOfReps(exerciseStepIndex: Int):MutableIntState{
        return mutableIntStateOf(exercise.steps[exerciseStepIndex].numberOfReps)
    }

    fun duration(slideIndex:Int,stepIndex:Int):MutableIntState{
        return mutableIntStateOf(exercise.steps[stepIndex].slides[slideIndex].duration)
    }

    fun updateDuration(slideIndex:Int,stepIndex:Int,newDuration:Int){
        exercise.steps[stepIndex].slides[slideIndex].duration = newDuration
        hasBeenEdited = true
    }

    private val textStateFlow = HashMap<String, MutableState<String>>()
    fun text(slideIndex:Int,stepIndex:Int):MutableState<String> {
        val key = "$slideIndex - $stepIndex"
        val result =
            if (textStateFlow.containsKey(key)){
                textStateFlow[key]!!
            }
            else {
                mutableStateOf(exercise.steps[stepIndex].slides[slideIndex].text)
            }
        textStateFlow[key] = result
        return result
    }

    fun updateText(slideIndex:Int,stepIndex:Int,newText:String){
        val key = "$slideIndex - $stepIndex"
        textStateFlow[key]?.value = newText
        exercise.steps[stepIndex].slides[slideIndex].text = newText
        hasBeenEdited = true
    }


    private val showCountdownStateFlow = HashMap<String, MutableState<Boolean>>()
    fun showCountdown(slideIndex:Int,stepIndex:Int):MutableState<Boolean> {
        val key = "$slideIndex - $stepIndex"
        val result =
            if (showCountdownStateFlow.containsKey(key)){
                showCountdownStateFlow[key]!!
            }
            else {
                mutableStateOf(exercise.steps[stepIndex].slides[slideIndex].countdown)
            }
        showCountdownStateFlow[key] = result
        return result
    }


    fun updateShowCountdown(slideIndex:Int,stepIndex:Int,newValue:Boolean){
        val key = "$slideIndex - $stepIndex"
        showCountdownStateFlow[key]?.value = newValue
        exercise.steps[stepIndex].slides[slideIndex].countdown = newValue
        hasBeenEdited = true
    }


    private val imageFileStateFlow = HashMap<String, MutableState<String>>()
    fun imageFile(slideIndex:Int,stepIndex:Int): MutableState<String> {
        val key = "$slideIndex - $stepIndex"
        if (imageFileStateFlow.containsKey(key)){
            return imageFileStateFlow[key]!!
        } else {
            imageFileStateFlow[key] = mutableStateOf(exercise.steps[stepIndex].slides[slideIndex].imageFileName ?: "")
            return imageFileStateFlow[key]!!
        }
    }

    private val videoFileStateFlow = HashMap<String, MutableState<String>>()
    fun videoFile(slideIndex:Int,stepIndex:Int): MutableState<String> {
        val key = "$slideIndex - $stepIndex"
        if (videoFileStateFlow.containsKey(key)){
            return videoFileStateFlow[key]!!
        } else {
            videoFileStateFlow[key] = mutableStateOf(exercise.steps[stepIndex].slides[slideIndex].videoFileName ?: "")
            return videoFileStateFlow[key]!!
        }
    }

    fun getImageFile(slideIndex:Int,stepIndex:Int):File? {
        return exercise.steps[stepIndex].slides[slideIndex].getImageFile(exercise.fileName, context)
    }

    val mediaUriKey = mutableStateOf("1")

    fun hasVisualMedia(slideIndex:Int,stepIndex:Int):Boolean{
        return (exercise.steps[stepIndex].slides[slideIndex].imageFileName != null
                || exercise.steps[stepIndex].slides[slideIndex].videoFileName != null)
    }

    private fun inputStreamToFile(uri: Uri, file: File):Boolean{
        val inputStream = context.contentResolver.openInputStream(uri)
        val output = FileOutputStream(file)
        val bytesCopied = inputStream?.copyTo(output, 4 * 1024)
        return ((bytesCopied ?: 0) > 0)
    }

    fun updateImage(slideIndex:Int,stepIndex:Int,uri: Uri) {
        //The idea here is to copy the new file to the exercise folder then we can just store the
        //file name as a string
        val imageFileName = "slide_image_${System.currentTimeMillis()}.jpg"
        val path = File(File(context.filesDir,
            "exercises"),
            exercise.fileName)
        path.mkdirs()
        val file = File(path, imageFileName)
        if (inputStreamToFile(uri, file)){
            val key = "$slideIndex - $stepIndex"
            exercise.steps[stepIndex].slides[slideIndex].imageFileName = imageFileName
            imageFileStateFlow[key]?.value = imageFileName
            exercise.steps[stepIndex].slides[slideIndex].videoFileName = null
            videoFileStateFlow.remove(key)
        }
        hasBeenEdited = true
    }

    fun updateVideo(slideIndex:Int,stepIndex:Int,uri: Uri) {
        val videoFileName = "slide_video_${System.currentTimeMillis()}"
        val path = File(File(context.filesDir,
            "exercises"),
            exercise.fileName)
        path.mkdirs()
        val file = File(path, videoFileName)
        if (inputStreamToFile(uri, file)){
            val key = "$slideIndex - $stepIndex"
            exercise.steps[stepIndex].slides[slideIndex].videoFileName = videoFileName
            videoFileStateFlow[key]?.value = videoFileName
            exercise.steps[stepIndex].slides[slideIndex].imageFileName = null
            imageFileStateFlow.remove(key)
        }
        hasBeenEdited = true
    }

    fun videoFileUri(slideIndex:Int,stepIndex:Int): Uri? {
        return exercise.steps[stepIndex].slides[slideIndex].videoFileUri(context, exercise.fileName)
    }
}

