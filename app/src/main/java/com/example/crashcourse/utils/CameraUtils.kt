package com.example.crashcourse.utils

import androidx.camera.core.CameraSelector

fun getCameraSelector(useBackCamera: Boolean): CameraSelector {
    return if (useBackCamera) {
        CameraSelector.DEFAULT_BACK_CAMERA
    } else {
        CameraSelector.DEFAULT_FRONT_CAMERA
    }
}
