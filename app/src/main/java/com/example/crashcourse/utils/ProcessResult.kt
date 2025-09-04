package com.example.crashcourse.utils

data class ProcessResult(
    val studentId: String,
    val name: String,
    val status: String,
    val photoSize: Long = 0,
    val error: String? = null
)
