package com.example.openphysicaltherapy.Data

data class InstructionalSlide(var text: String = "",
                              var duration:Int=3,
                              var countdown: Boolean = false)

data class ExerciseStep(var numberOfReps: Int = 1,
                        var slides: MutableList<InstructionalSlide> = mutableListOf(  InstructionalSlide() ))

data class Exercise(var name: String,
                    var steps: MutableList<ExerciseStep> = mutableListOf(ExerciseStep()))

data class ExerciseListItem(val name: String,
                            var workouts: MutableList<WorkoutListItem> = mutableListOf())

data class Workout(var name: String, var exercises: List<ExerciseListItem>)

data class WorkoutListItem(val name: String)
