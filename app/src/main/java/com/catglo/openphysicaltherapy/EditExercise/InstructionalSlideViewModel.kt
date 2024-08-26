package com.catglo.openphysicaltherapy.EditExercise

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.catglo.openphysicaltherapy.Data.InstructionalSlide
import com.catglo.openphysicaltherapy.OpenPhysicalTherapyApplication
import java.io.File
import java.io.FileOutputStream

class InstructionalSlideViewModel(private val exerciseFileName:String, private val slide: InstructionalSlide, val application: OpenPhysicalTherapyApplication) : ViewModel() {
    var duration = mutableIntStateOf(slide.duration)
        private set

    fun updateDuration(newDuration:Int){
        duration.intValue = newDuration
        slide.duration = newDuration
    }

    var text by mutableStateOf(slide.text)
        private set

    fun updateText(newText:String){
        text = newText
        slide.text = newText
    }

    var showCountdown = mutableStateOf(slide.countdown)
        private set

    fun updateShowCountdown(newValue:Boolean){
        showCountdown.value = newValue
        slide.countdown = newValue
    }

    var imageFile by mutableStateOf(slide.imageFileName)
        private set
//
//    fun updateImageFile(newFile:String){
//        imageFile = newFile
//        slide.imageFileName = newFile
//    }
//
//    var audioFile by mutableStateOf(slide.audioFileName)
//        private set
//
//    fun updateAudioFile(newFile:String){
//        audioFile = newFile
//        slide.audioFileName = newFile
//    }
//
    var videoFile by mutableStateOf(slide.videoFileName)
        private set
//
//    fun updateVideoFile(newFile:String){
//        videoFile = newFile
//        slide.videoFileName = newFile
//    }

    fun getImageFile():File? {
        return slide.getImageFile(exerciseFileName, application)
    }

    fun hasVisualMedia():Boolean{
        return (slide.imageFileName != null || slide.videoFileName != null)
    }

    private fun inputStreamToFile(uri: Uri, file: File):Boolean{
        val inputStream = application.contentResolver.openInputStream(uri)
        val output = FileOutputStream(file)
        val bytesCopied = inputStream?.copyTo(output, 4 * 1024)
        return ((bytesCopied ?: 0) > 0)
    }

    fun updateImage(uri: Uri) {
        //The idea here is to copy the new file to the exercise folder then we can just store the
        //file name as a string
        val imageFileName = "slide_image_${System.currentTimeMillis()}.jpg"
        val path = File(File(application.filesDir,
            "exercises"),
            exerciseFileName)
        path.mkdirs()
        val file = File(path, imageFileName)
        if (inputStreamToFile(uri, file)){
            slide.imageFileName = imageFileName
            imageFile = imageFileName
            slide.videoFileName = null
            videoFile = null
        }
    }

    fun updateVideo(uri: Uri) {
        val videoFileName = "slide_video_${System.currentTimeMillis()}"
        val path = File(File(application.filesDir,
            "exercises"),
            exerciseFileName)
        path.mkdirs()
        val file = File(path, videoFileName)
        if (inputStreamToFile(uri, file)){
            slide.videoFileName = videoFileName
            videoFile = videoFileName
            slide.imageFileName = null
            imageFile = null
        }
    }

    fun videoFileUri(): Uri? {
        return slide.videoFileUri(application, exerciseFileName)

    }

}