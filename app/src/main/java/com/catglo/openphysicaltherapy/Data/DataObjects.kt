package com.catglo.openphysicaltherapy.Data

import android.content.Context
import android.net.Uri
import com.catglo.openphysicaltherapy.toValidFileName
import java.io.File

data class InstructionalSlide(var text: String = "",
                              var duration:Int=3,
                              var countdown: Boolean = true,
                              var imageFileName: String? = null,
                              var audioFileName: String? = null,
                              var videoFileName: String? = null,
                              var speakInstructions: Boolean = false,
                              var showTimer:Boolean = true
    ) {

    fun videoFileUri(application: Context, exerciseFileName:String): Uri? {
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

    fun getImageFile(exerciseFileName: String, application: Context): File? {
        imageFileName?.let { imageFileName ->
            return File(
                File(
                    File(
                        application.filesDir,
                        "exercises"
                    ),
                    exerciseFileName
                ),imageFileName)
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
{
    fun totalDuration():Int{
        var totalDuration = 0
        steps.forEach { step ->
            step.slides.forEach { slide ->
                totalDuration += slide.duration
            }
        }
        return totalDuration
    }

    fun totalNumberOfReps(): Any {
        var totalNumberOfReps = 0
        steps.forEach { step ->
            totalNumberOfReps += step.numberOfReps
        }
        return totalNumberOfReps
    }

    fun prettyFileName(): String {
        return name.toValidFileName()
    }
}



data class ExerciseNameConflict(val newExercise: Exercise, val existingNameConflictExercise: ExerciseListItem?)

data class ExerciseListItem(var name: String, var fileName: String)

data class Workout(var name: String,
                   var exercises: List<ExerciseListItem>,
                   var fileName: String = "workout_"+System.currentTimeMillis().toString())

data class WorkoutListItem(var name: String, val fileName: String)


data class WorkoutNameConflict(val newWorkout: Workout,
                               val existingNameConflictWorkout: WorkoutListItem?,
                               val exerciseConflicts: List<ExerciseNameConflict>)
