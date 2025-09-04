package com.example.crashcourse.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import com.example.crashcourse.R

@Composable
fun CameraSwitchButton(
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Image(
        painter = painterResource(id = R.drawable.ic_switch_camera),
        contentDescription = "Switch Camera",
        modifier = modifier.clickable { onToggle() }
    )
}
