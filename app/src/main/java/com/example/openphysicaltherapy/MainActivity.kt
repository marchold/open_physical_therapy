package com.example.openphysicaltherapy

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Create
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemColors
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.openphysicaltherapy.ui.theme.OpenPhysicalTherapyTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
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

    @Composable
    fun MainScreen(
        navController: NavHostController
    ) {
        Scaffold(
            bottomBar = {
                BottomAppBar(
                    modifier = Modifier,
                    containerColor = MaterialTheme.colorScheme.background)
                {
                    BottomNavigationBar(navController = navController)
                }
            }
        ) { innerPadding ->
            Box(modifier = Modifier.padding(
                PaddingValues(
                    0.dp,
                    0.dp,
                    0.dp,
                    innerPadding.calculateBottomPadding())
                )
            ) {
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
            containerColor = MaterialTheme.colorScheme.background
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
                ExercisesScreen()
            }
            composable(NavigationItem.Workouts.route) {
                WorkoutsScreen()
            }
        }
    }

    @Composable
    fun ExercisesScreen() {
        Scaffold(
            floatingActionButton = {
                MultiFloatingActionButton(
                    modifier = Modifier,
                    items = listOf(
                        FloatingButtonItem(Icons.Filled.Create, "Create Exercise", onClick = {
                            startActivity(Intent(this, CreateExerciseActivity::class.java))
                        }),
                        FloatingButtonItem(ImageVector.vectorResource(R.drawable.icon_import), "Import Exercise File", onClick = {

                        })
                    ),
                    icon = Icons.Filled.Add,
                )
            },
        ){ innerPadding->
            Column(
                modifier = Modifier.padding(innerPadding)
            ) {
                Text("List goes here")
            }
        }

    }
}

@Composable
fun CenterText(text: String) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = text, fontSize = 32.sp)
    }
}

@Composable
fun HomeScreen() {
    CenterText(text = "Today")
}



@Composable
fun WorkoutsScreen() {
    CenterText(text = "Workouts")
}

sealed class NavigationItem(var route: String, val icon: Int, var title: String) {
    data object Today : NavigationItem("Today", R.drawable.arm_flex_outline, "Today")
    data object Exercises : NavigationItem("Exercises", R.drawable.icon_stretch, "Exercises")
    data object Workouts : NavigationItem("Workouts", R.drawable.icon_workouts, "Workouts")
}


