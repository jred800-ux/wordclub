package com.jred.WordClub_App.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.jred.WordClub_App.ui.screens.*
import com.jred.WordClub_App.viewmodel.AuthViewModel

sealed class Screen(val route: String, val label: String, val icon: ImageVector, val selectedIcon: ImageVector) {
    object Home : Screen("home", "控制台", Icons.Outlined.Home, Icons.Filled.Home)
    object BookLibrary : Screen("library", "词库", Icons.Outlined.MenuBook, Icons.Filled.MenuBook)
    object Summary : Screen("summary", "统计", Icons.Outlined.BarChart, Icons.Filled.BarChart)
}

val bottomNavItems = listOf(Screen.Home, Screen.BookLibrary, Screen.Summary)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WordClubNavGraph(authViewModel: AuthViewModel) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val uiState by authViewModel.uiState.collectAsState()

    // Determine start destination based on auth state
    val startDestination = if (uiState.isLoggedIn) Screen.Home.route else "login"

    // Auth-related routes that should not show bottom bar
    val isAuthRoute = currentDestination?.route in listOf("login", "register")

    val showBottomBar = !isAuthRoute && bottomNavItems.any { screen ->
        currentDestination?.hierarchy?.any { it.route == screen.route } == true
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomNavItems.forEach { screen ->
                        val selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
                        NavigationBarItem(
                            icon = {
                                Icon(
                                    if (selected) screen.selectedIcon else screen.icon,
                                    contentDescription = screen.label
                                )
                            },
                            label = { Text(screen.label) },
                            selected = selected,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding)
        ) {
            // Auth routes (no bottom bar)
            composable("login") {
                LoginScreen(
                    authViewModel = authViewModel,
                    onLoginSuccess = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo("login") { inclusive = true }
                        }
                    },
                    onNavigateToRegister = {
                        navController.navigate("register")
                    }
                )
            }
            composable("register") {
                RegisterScreen(
                    authViewModel = authViewModel,
                    onRegisterSuccess = {
                        // handled internally in the screen
                    },
                    onNavigateToLogin = {
                        navController.popBackStack()
                    }
                )
            }

            // Main routes
            composable(Screen.Home.route) { HomeScreen(navController) }
            composable(Screen.BookLibrary.route) { BookLibraryScreen(navController) }
            composable(Screen.Summary.route) { StudySummaryScreen() }
            composable("learn/first-sight") { FirstSightScreen(navController) }
            composable("learn/spelling") { SpellingScreen(navController) }
            composable("word/{wordId}") { backStackEntry ->
                val wordId = backStackEntry.arguments?.getString("wordId") ?: "0"
                WordDetailScreen(wordId = wordId, navController = navController)
            }
        }
    }
}
