package com.catglo.openphysicaltherapy.Data

import android.net.Uri
import com.catglo.openphysicaltherapy.OpenPhysicalTherapyApplication
import java.io.File

data class InstructionalSlide(var text: String = "",
                              var duration:Int=3,
                              var countdown: Boolean = false,
                              var imageFileName: String? = null,
                              var audioFileName: String? = null,
                              var videoFileName: String? = null) {
    fun videoFileUri(application:OpenPhysicalTherapyApplication, exerciseFileName:String): Uri? {
        videoFileName?.let { videoFileName ->
            val file = File(
                File(
                    File(
                application.filesDir,
                "exercises"),
                exerciseFileName),
                videoFileName)
            return Uri.fromFile(file)
        }
        return null
    }
}

data class ExerciseStep(var numberOfReps: Int = 1,
                        var slides: MutableList<InstructionalSlide> = mutableListOf(
                            InstructionalSlide()
                        ))

data class Exercise(var name: String,
                    var steps: MutableList<ExerciseStep> = mutableListOf(ExerciseStep()),
                    var fileName: String = "exercise_"+System.currentTimeMillis().toString())

data class ExerciseListItem(var name: String, val fileName: String)

data class Workout(var name: String,
                   var exercises: List<ExerciseListItem>,
                   var fileName: String = "workout_"+System.currentTimeMillis().toString())

data class WorkoutListItem(var name: String, val fileName: String)
