
package com.example.crashcourse.ui

import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.PermissionStatus

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PermissionsHandler(
    permissionState: PermissionState,
    onGranted: @Composable () -> Unit
) {
    when (permissionState.status) {
        is PermissionStatus.Granted -> onGranted()
        else -> {
            LaunchedEffect(Unit) {
                permissionState.launchPermissionRequest()
            }
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Camera permission is required",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}