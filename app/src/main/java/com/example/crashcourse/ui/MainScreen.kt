package com.example.crashcourse.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.crashcourse.navigation.Screen
import com.example.crashcourse.ui.add.AddUserScreen
import com.example.crashcourse.ui.add.BulkRegistrationScreen
import com.example.crashcourse.ui.edit.EditUserScreen
import com.example.crashcourse.ui.components.FaceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val navController = rememberNavController()
    var useBackCamera by remember { mutableStateOf(false) }

    // Create a shared ViewModel instance for all screens
    val sharedFaceViewModel: FaceViewModel = viewModel()

    Scaffold(
        bottomBar = { BottomNav(navController) }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screen.RegistrationMenu.route, // Set RegistrationMenu as the start destination
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(Screen.CheckIn.route) {
                CheckInScreen(useBackCamera = useBackCamera)
            }
            composable(Screen.RegistrationMenu.route) {
                RegistrationMenuScreen(
                    onNavigateToRegister = {
                        navController.navigate(Screen.Register.route)
                    },
                    onNavigateToBulkRegister = {
                        navController.navigate(Screen.BulkRegister.route)
                    },
                    onNavigateToAddUser = {
                        navController.navigate(Screen.AddUser.route)
                    }
                )
            }
            composable(Screen.Register.route) {
                RegisterFaceScreen(
                    useBackCamera = useBackCamera,
                    viewModel = sharedFaceViewModel
                )
            }
            composable(Screen.AddUser.route) {
                AddUserScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onUserAdded = { /* Refresh or update list if needed */ },
                    viewModel = sharedFaceViewModel
                )
            }
            composable(Screen.BulkRegister.route) {
                BulkRegistrationScreen(
                    faceViewModel = sharedFaceViewModel
                )
            }
            composable(Screen.FaceManualCapture.route) {
                FaceManualCaptureScreen(
                    onCaptured = { _ ->
                        // Handle captured bitmap - save to gallery, process, etc.
                        // For now, just navigate back
                        navController.popBackStack()
                    },
                    onDismiss = {
                        navController.popBackStack()
                    }
                )
            }
            composable(Screen.ManualRegistration.route) {
                // ManualRegistrationScreen has been removed as it is no longer needed
                Text("Manual Registration is currently unavailable.")
            }
            composable(Screen.Manage.route) {
                FaceListScreen(
                    viewModel = sharedFaceViewModel,
                    onEditUser = { user ->
                        navController.navigate(Screen.EditUser.createRoute(user.studentId))
                    }
                )
            }
            composable(Screen.EditUser.route) { backStackEntry ->
                val studentId = backStackEntry.arguments?.getString("studentId") ?: ""
                EditUserScreen(
                    studentId = studentId,
                    useBackCamera = useBackCamera,
                    onNavigateBack = { navController.popBackStack() },
                    onUserUpdated = { /* Refresh or update list if needed */ },
                    faceViewModel = sharedFaceViewModel
                )
            }
            composable(Screen.Options.route) {
                OptionsManagementScreen(
                    onNavigateToForm = { type ->
                        navController.navigate(Screen.OptionForm.createRoute(type))
                    }
                )
            }
            composable(
                route = Screen.OptionForm.route,
                arguments = listOf(
                    navArgument("type") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val type = backStackEntry.arguments?.getString("type") ?: return@composable
                OptionFormScreen(
                    type = type,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable(Screen.CheckInRecord.route) {
                CheckInRecordScreen()
            }
            composable(Screen.Debug.route) {
                DebugScreen()
            }
            composable(Screen.ExcelImageProcess.route) {
                ExcelImageProcessScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable(Screen.TestFaceImage.route) {
                TestFaceImageScreen()
            }
        }
    }
}

@Composable
fun BottomNav(navController: NavHostController) {
    val items = listOf(
        Screen.CheckIn  to "Check In",
        Screen.RegistrationMenu to "Register",
        Screen.Manage   to "Manage",
        Screen.Options  to "Options"
    )
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar {
        items.forEach { (screen, label) ->
            NavigationBarItem(
                selected = currentRoute == screen.route,
                onClick = {
                    navController.navigate(screen.route) {
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = {
                    Icon(
                        imageVector = when (screen) {
                            Screen.CheckIn  -> Icons.Default.Person
                            Screen.RegistrationMenu -> Icons.Default.PersonAdd
                            Screen.Register -> Icons.Default.PersonAdd
                            Screen.Manage   -> Icons.AutoMirrored.Filled.List
                            Screen.ExcelImageProcess -> Icons.Default.Upload
                            Screen.Options  -> Icons.Default.Settings
                            Screen.CheckInRecord -> Icons.Default.History
                            Screen.Debug -> Icons.Default.BugReport
                            Screen.TestFaceImage -> Icons.Default.Science
                            else -> Icons.Default.Person
                        },
                        contentDescription = label
                    )
                },
                label = { Text(label) }
            )
        }
    }
}
