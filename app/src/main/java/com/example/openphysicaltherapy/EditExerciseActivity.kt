package com.example.openphysicaltherapy

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.openphysicaltherapy.ui.theme.OpenPhysicalTherapyTheme
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream


class ExerciseViewModel() : ViewModel() {
    var exercise = Exercise("")

    private var _exerciseSteps = exercise.steps.toMutableStateList()
    fun getExerciseSteps():List<ExerciseStep>{
        return _exerciseSteps
    }
    fun addStep(){
        _exerciseSteps.add(ExerciseStep(1))
        exercise.steps = _exerciseSteps
    }

    private val _name = MutableLiveData<String>("")
    val name: LiveData<String> = _name
    fun updateName(newName:String){
        _name.value = newName
        exercise.name = newName
    }

    fun save(context: Context){
        val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
        val jsonAdapter: JsonAdapter<Exercise> = moshi.adapter(Exercise::class.java)
        val json: String = jsonAdapter.toJson(exercise)
        val outputFile = file(context, name.value!!)
        if (!outputFile.exists()){
            ExerciseListItem.addExercise(name.value!!)
        }
        FileOutputStream(outputFile).use {
            it.write(json.toByteArray())
        }

    }

    fun load(context: Context, name:String) {
        val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
        val jsonAdapter: JsonAdapter<Exercise> = moshi.adapter(Exercise::class.java)
        val json = FileInputStream(file(context, name))
            .bufferedReader()
            .use {
                it.readText()
            }
        val exercise = jsonAdapter.fromJson(json)
        this._name.value = name
        exercise?.name = name
        exercise?.steps?.let {
            _exerciseSteps = it.toMutableStateList()
            this.exercise.steps = it
        }
    }

    companion object {

        private fun file(context: Context, name: String): File {
            val path = File(File(context.filesDir, "exercises"), name)
            if (!path.exists()) path.mkdirs()
            return File(path, "${name}-index.json")
        }

    }
}


class EditExerciseActivity() : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val exerciseToEdit = intent.getStringExtra("EditExercise")
        enableEdgeToEdge()
        setContent {
            CreateExerciseScreen(exerciseToEdit)
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun CreateExerciseScreen(exerciseToEdit: String?) {
        val exerciseViewModel by viewModels<ExerciseViewModel>()
        exerciseToEdit?.let {
            val context = LocalContext.current
            exerciseViewModel.load(context, it)
        }

        val exerciseSteps = exerciseViewModel.getExerciseSteps()
        val pagerState = rememberPagerState(pageCount = {exerciseSteps.size})
        val nameState by exerciseViewModel.name.observeAsState()

        val coroutineScope = rememberCoroutineScope()
        var isExerciseNameError by remember { mutableStateOf(false) }
        val showConfirmDiscardAlert = remember { mutableStateOf( false ) }

        if (showConfirmDiscardAlert.value){
            AlertDialog(
                onDismissRequest = { showConfirmDiscardAlert.value = false },
                dismissButton = { Button(onClick = { showConfirmDiscardAlert.value = false }) { Text(text = "Cancel") } },
                title={ Text("Discard Changes") },
                text={ Text("Are you sure you want to exit without saving?") },
                confirmButton = { Button(onClick = { finish() }) { Text(text = "Discard Changes") } }
            )
        }

        OpenPhysicalTherapyTheme {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text(getString(R.string.title_activity_create_exercise)) },
                        actions = {
                            IconButton(onClick = {
                                showConfirmDiscardAlert.value = true
                            }) {
                                Icon(Icons.Filled.Close, contentDescription = "Discard Changes Button")
                            }
                            IconButton(onClick = {
                                var isError = false
                                if ((exerciseViewModel.name.value?.length ?: 0) == 0){
                                    isExerciseNameError = true
                                    isError = true
                                }
                                if (!isError) {
                                    exerciseViewModel.save(this@EditExerciseActivity)
                                    finish()
                                }
                            }) {
                                Icon(Icons.Filled.Done, contentDescription = "Save Exercise Button")
                            }
                        },
                        colors = TopAppBarColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            scrolledContainerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                },
                content = { paddingValues ->
                    Column(modifier = Modifier.padding(paddingValues)) {
                        Spacer(modifier = Modifier.height(10.dp))
                        TextField(
                            modifier = Modifier.fillMaxWidth(),
                            value = nameState!!,
                            onValueChange = {
                                if (it.length == 0){
                                    isExerciseNameError = true
                                }else {
                                    isExerciseNameError = false
                                }
                                exerciseViewModel.updateName(it)
                            },
                            label = {
                                Text(text = stringResource(R.string.name_exercise))
                            },
                            maxLines = 1
                        )
                        if (isExerciseNameError){
                            Text(text = "Please enter a valid exercise name", color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Step ${pagerState.currentPage+1}",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            textAlign = TextAlign.Center
                        )
                        HorizontalDivider(thickness = 2.dp, color = MaterialTheme.colorScheme.onBackground, modifier = Modifier.padding(0.dp,0.dp,0.dp,8.dp))
                        HorizontalPager(state = pagerState ) { stepIndex ->
                            Column {
                                ExerciseStepScreen(exerciseSteps[stepIndex],stepIndex)
                            }
                        }

                    }
                },
                bottomBar = {
                    BottomAppBar {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            IconButton(onClick = {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(pagerState.currentPage - 1)
                                }
                            },
                                enabled = pagerState.currentPage>0) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous Step")
                            }
                            IconButton(onClick = {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                }
                            },
                                enabled = pagerState.currentPage<pagerState.pageCount-1) {
                                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next Step")
                            }
                            IconButton(onClick = {
                                exerciseViewModel.addStep()
                                Log.i("pager","pager state page count = ${pagerState.pageCount}")
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(pagerState.pageCount-1)
                                }
                            }) {
                                Icon(Icons.Filled.Add, contentDescription = "Add Step")
                            }
                        }
                    }

                }
            )
        }
    }
}

inline fun <VM : ViewModel> viewModelFactory(crossinline f: () -> VM) =
    object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(aClass: Class<T>):T = f() as T
    }

class ExerciseStepViewModel(private val exerciseStep:ExerciseStep) : ViewModel() {


    private var _slides = exerciseStep.slides.toMutableStateList()
    fun getSlides():List<InstructionalSlide>{
        return _slides
    }
    fun addSlide(){
        _slides.add(InstructionalSlide())
        exerciseStep.slides = _slides
    }
    fun removeSlide(slideIndex: Int) {
        _slides.removeAt(slideIndex)
        exerciseStep.slides = _slides
    }

    var numberOfReps = mutableIntStateOf(exerciseStep.numberOfReps)
        private set

    fun updateNumberOfReps(newNumberOfReps:Int){
        numberOfReps.value = newNumberOfReps
        exerciseStep.numberOfReps = newNumberOfReps
    }
}


@Composable
fun ExerciseStepScreen(step: ExerciseStep, stepIndex: Int) {

    val stepViewModel = viewModel<ExerciseStepViewModel>(
        key = stepIndex.toString(),
        factory = viewModelFactory { ExerciseStepViewModel(step) })

    val slides = stepViewModel.getSlides()

    LazyColumn{
        item{
            NumberPickerTextField(
                intLiveData = stepViewModel.numberOfReps,
                icon = ImageVector.vectorResource(R.drawable.icon_repeat),
                minimumValue = 1,
                maximumValue = 20,
                "Number of reps"
            ) {
                stepViewModel.updateNumberOfReps(it)
            }
        }
        items(slides.size) { slideIndex ->
            InstructionalSlideScreen(
                stepIndex = stepIndex,
                slideIndex = slideIndex,
                slide = slides[slideIndex],
                stepViewModel = stepViewModel
            )
        }
        item {
            Row(modifier = Modifier.fillMaxWidth()) {
                Spacer(modifier = Modifier.weight(1f))
                Button(onClick = {
                    //exercise.steps[page].addSlides()
                    stepViewModel.addSlide()
                }) {
                    Text("Add slide")
                }
            }
        }
    }
}

class InstructionalSlideViewModel(private val slide:InstructionalSlide) : ViewModel() {
    var duration = mutableIntStateOf(slide.duration)
        private set

    fun updateDuration(newDuration:Int){
        duration.value = newDuration
        slide.duration = newDuration
    }

    var text by mutableStateOf(slide.text)
        private set

    fun updateText(newText:String){
        text = newText
        slide.text = newText
    }

}

@Composable
fun InstructionalSlideScreen(
    slideIndex: Int,
    slide: InstructionalSlide,
    stepIndex: Int,
    stepViewModel: ExerciseStepViewModel
) {
    val slideViewModel = viewModel<InstructionalSlideViewModel>(
        key = "$stepIndex - $slideIndex",
        factory = viewModelFactory {
            InstructionalSlideViewModel(slide)
        })


    Box {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Slide ${slideIndex+1}")
        }
        if (slideIndex>0) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(),
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
    }
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Outlined.Info,
            contentDescription = "Information Icon",
            modifier = Modifier.padding(start = 10.dp, top=0.dp, end=10.dp, bottom = 0.dp))
        TextField(
            modifier = Modifier.fillMaxWidth(),
            value = slideViewModel.text,
            onValueChange = { newValue ->
                slideViewModel.updateText(newValue)
            },
            label = {
                Text(text = "Instructional text")
            }
        )
    }

    NumberPickerTextField(
        intLiveData = slideViewModel.duration,
        icon = ImageVector.vectorResource(R.drawable.icon_timer),
        minimumValue = 3,
        maximumValue = 500,
        title = "Duration in seconds"
    ) {
        slideViewModel.updateDuration(it)
    }
    Row {
        // TODO:
        //   Icon (image, video)
        //   Video/Audio+Image file picker
    }
}
