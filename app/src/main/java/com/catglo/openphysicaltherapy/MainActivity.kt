package com.catglo.openphysicaltherapy

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemColors
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.catglo.openphysicaltherapy.ExercisesList.ExerciseListViewModel
import com.catglo.openphysicaltherapy.ExercisesList.ExercisesListView

import com.catglo.openphysicaltherapy.Widgets.actionBarColors
import com.catglo.openphysicaltherapy.WorkoutList.WorkoutListViewModel
import com.catglo.openphysicaltherapy.WorkoutList.WorkoutsListView
import com.catglo.openphysicaltherapy.TodayTab.HomeScreen

import com.catglo.openphysicaltherapy.ui.theme.OpenPhysicalTherapyTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val exercises by viewModels<ExerciseListViewModel>()
    private val workouts by viewModels<WorkoutListViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

    }

    override fun onResume() {
        super.onResume()
        exercises.reload()

        setContent {
            val navController = rememberNavController()
            OpenPhysicalTherapyTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen(navController = navController)
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun MainScreen(
        navController: NavHostController
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Open Physical Therapy") },
                    colors = actionBarColors(),
                )
            },
            bottomBar = {
                BottomAppBar(
                    modifier = Modifier,
                    containerColor = MaterialTheme.colorScheme.background)
                {
                    BottomNavigationBar(navController = navController)
                }
            }
        ) { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding))
            {
                Tabs(navController = navController)
            }

        }

    }

    @Composable
    fun BottomNavigationBar(navController: NavController) {
        val items = listOf(
            NavigationItem.Today,
            NavigationItem.Exercises,
            NavigationItem.Workouts,
        )
        var selectedItem by remember { mutableIntStateOf(0) }
        var currentRoute by remember { mutableStateOf(NavigationItem.Today.route) }

        items.forEachIndexed { index, navigationItem ->
            if (navigationItem.route == currentRoute) {
                selectedItem = index
            }
        }

        NavigationBar(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ) {
            items.forEachIndexed { index, item ->
                NavigationBarItem(
                    alwaysShowLabel = true,
                    icon = { Icon(ImageVector.vectorResource(item.icon), contentDescription = item.title) },
                    label = { Text(item.title) },
                    selected = selectedItem == index,
                    colors = NavigationBarItemColors(
                        selectedIconColor = MaterialTheme.colorScheme.onSurface,
                        selectedTextColor = MaterialTheme.colorScheme.onSurface,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurface,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        disabledTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        selectedIndicatorColor = MaterialTheme.colorScheme.onSecondary
                    ),
                    onClick = {
                        selectedItem = index
                        currentRoute = item.route
                        navController.navigate(item.route) {
                            navController.graph.startDestinationRoute?.let { route ->
                                popUpTo(route) {
                                    saveState = true
                                }
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    }

    @Composable
    fun Tabs(navController: NavHostController) {
        NavHost(navController, startDestination = NavigationItem.Today.route) {
            composable(NavigationItem.Today.route) {
                HomeScreen()
            }
            composable(NavigationItem.Exercises.route) {
                ExercisesListView(exercises)
            }
            composable(NavigationItem.Workouts.route) {
                WorkoutsListView(workouts)
            }
        }
    }

}

//TODO: Need to add the option to add a continue screen, for like the exercisae intro

//TODO: There is a bug in the player where the first instruction is
//      played late, proboby from the time it takes to init text to speach

//TODO: We need some indication that its another reps

//TODO: Should make the whole image a click control to select a new image or video not just the icon

//TODO: Some global exercise settings. like auto say next rep, timer beep sounds, auto intro exerise screen

//TODO: Add a list of materials like a band or whatever else you need to the workout and each
//      exercise screen

//TODO: Add some way to schedule workouts, (need to figure out the UX for this)
//              Either integrate with google calendar, or just have a schedule option for each workout

//TODO: Add some simple help/tutorial system

//TODO: Make some options associated with various elements.
//      Use something like this to include them with the keyboard or picker
//      https://stackoverflow.com/questions/73331594/how-can-i-show-a-composable-on-top-of-the-visible-keyboard
//      This would include:
//          For the text an option to read it with text to speech or display it

//TODO: Would be good to have exercise body parts with icons


sealed class NavigationItem(var route: String, val icon: Int, var title: String) {
    data object Today : NavigationItem("Today", R.drawable.arm_flex_outline, "Today")
    data object Exercises : NavigationItem("Exercises", R.drawable.icon_stretch, "Exercises")
    data object Workouts : NavigationItem("Workouts", R.drawable.icon_workouts, "Workouts")
}


