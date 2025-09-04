package com.example.crashcourse.navigation

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.example.crashcourse.ui.OptionsManagementScreen

sealed class Screen(val route: String) {
    object CheckIn  : Screen("check_in")
    object RegistrationMenu : Screen("registration_menu")
    object Register : Screen("register")
    object AddUser : Screen("add_user")
    object BulkRegister : Screen("bulk_register") // Bulk registration screen
    object ExcelImageProcess : Screen("excel_image_process")  // ✅ Excel Image Process screen
    object FaceManualCapture : Screen("face_manual_capture")  // ✅ New manual capture screen
    object ManualRegistration : Screen("manual_registration")  // ✅ New manual registration screen
    object Manage   : Screen("manage_faces")
    object EditUser : Screen("edit_user/{studentId}") {
        fun createRoute(studentId: String) = "edit_user/$studentId"
    }
    object Options  : Screen("options_management")
    object OptionForm : Screen("option_form/{type}") {
        fun createRoute(type: String) = "option_form/$type"
    }
    object CheckInRecord : Screen("checkin_record")
    object Debug : Screen("debug")
    object TestFaceImage : Screen("test_face_image")
}

/**
 * Extension function to add the options management screen to the navigation graph
 */
fun NavGraphBuilder.addOptionsManagementScreen(navController: NavController) {
    composable(Screen.Options.route) {
        OptionsManagementScreen(
            onNavigateToForm = { type ->
                navController.navigate(Screen.OptionForm.createRoute(type))
            }
        )
    }
    // Add composable for OptionFormScreen with type argument
    composable(Screen.OptionForm.route) { backStackEntry ->
        val type = backStackEntry.arguments?.getString("type") ?: ""
        com.example.crashcourse.ui.OptionFormScreen(
            type = type,
            onNavigateBack = { navController.popBackStack() }
        )
    }
}

/**
 * Extension function to add the test face image screen to the navigation graph
 */
fun NavGraphBuilder.addTestFaceImageScreen() {
    composable(Screen.TestFaceImage.route) {
        com.example.crashcourse.ui.TestFaceImageScreen()
    }
}

/**
 * Composable function for the options management navigation button
 */
@Composable
fun OptionsManagementButton(navController: NavController) {
    Button(
        onClick = { navController.navigate(Screen.Options.route) },
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("Manage Options")
    }
}

/**
 * Composable function for the test face image navigation button
 */
@Composable
fun TestFaceImageButton(navController: NavController) {
    Button(
        onClick = { navController.navigate(Screen.TestFaceImage.route) },
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("🧪 Test Face Images")
    }
}
