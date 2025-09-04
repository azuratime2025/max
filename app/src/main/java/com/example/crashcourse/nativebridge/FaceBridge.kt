package com.example.crashcourse.nativebridge

object FaceBridge {
    init {
        System.loadLibrary("azura_face_lib")
    }

    external fun initializeNative(modelPath: String): Boolean
    external fun recognizeFace(input: FloatArray): FloatArray
    external fun closeNative()
}
