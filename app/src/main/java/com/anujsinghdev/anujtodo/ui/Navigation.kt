package com.anujsinghdev.anujtodo.ui

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.anujsinghdev.anujtodo.ui.login.LoginScreen
import com.anujsinghdev.anujtodo.ui.list_detail.ListDetailScreen
import com.anujsinghdev.anujtodo.ui.todo_list.TodoListScreen
import com.anujsinghdev.anujtodo.ui.util.Screen
import com.anujsinghdev.anujtodo.ui.my_day.MyDayScreen
import com.anujsinghdev.anujtodo.ui.pomodoro.PomodoroScreen
import com.anujsinghdev.anujtodo.ui.stats.StatsScreen // Import your StatsScreen

@Composable
fun Navigation(
    navController: NavHostController,
    startDestination: String
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        // Optional: Set a default background color for the transition area
        // modifier = Modifier.background(Color.Black)
    ) {

        // --- 0. Login (No animation needed, usually usually separate) ---
        composable(route = Screen.LoginScreen.route) {
            LoginScreen(navController = navController)
        }

        // --- MAIN TABS: Apply Smooth Transitions Here ---

        // 1. HOME SCREEN
        composable(
            route = Screen.TodoListScreen.route,
            enterTransition = {
                fadeIn(animationSpec = tween(400, easing = FastOutSlowInEasing)) +
                        scaleIn(initialScale = 0.95f, animationSpec = tween(400, easing = FastOutSlowInEasing))
            },
            exitTransition = {
                fadeOut(animationSpec = tween(300, easing = FastOutSlowInEasing))
            }
        ) {
            TodoListScreen(navController = navController)
        }

        // 2. FOCUS (POMODORO) SCREEN
        composable(
            route = Screen.PomodoroScreen.route,
            enterTransition = {
                fadeIn(animationSpec = tween(400, easing = FastOutSlowInEasing)) +
                        scaleIn(initialScale = 0.95f, animationSpec = tween(400, easing = FastOutSlowInEasing))
            },
            exitTransition = {
                fadeOut(animationSpec = tween(300, easing = FastOutSlowInEasing))
            }
        ) {
            PomodoroScreen(navController = navController)
        }

        // 3. STATS SCREEN
        composable(
            route = Screen.StatsScreen.route,
            enterTransition = {
                fadeIn(animationSpec = tween(400, easing = FastOutSlowInEasing)) +
                        scaleIn(initialScale = 0.95f, animationSpec = tween(400, easing = FastOutSlowInEasing))
            },
            exitTransition = {
                fadeOut(animationSpec = tween(300, easing = FastOutSlowInEasing))
            }
        ) {
            StatsScreen(navController = navController)
        }

        // --- SUB SCREENS (Default or Slide Animations) ---

        composable(route = Screen.ArchiveScreen.route) {
            com.anujsinghdev.anujtodo.ui.archive.ArchiveScreen(navController = navController)
        }

        composable(route = Screen.MyDayScreen.route) {
            MyDayScreen(navController = navController)
        }

        composable(
            route = Screen.ListDetailScreen.route,
            arguments = listOf(
                navArgument("listId") { type = NavType.LongType },
                navArgument("listName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val listId = backStackEntry.arguments?.getLong("listId") ?: 0L
            val listName = backStackEntry.arguments?.getString("listName") ?: "List"

            ListDetailScreen(
                navController = navController,
                listId = listId,
                listName = listName
            )
        }
    }
}