package com.example.crashcourse.utils

import android.content.Context
import android.graphics.BitmapFactory
import com.example.crashcourse.utils.ProcessResult
import com.example.crashcourse.utils.CsvImportUtils.CsvStudentData
import com.example.crashcourse.viewmodel.FaceViewModel
import com.example.crashcourse.utils.PhotoProcessingUtils.processBitmapForFaceEmbedding
import com.example.crashcourse.utils.PhotoStorageUtils.saveFacePhoto
import com.example.crashcourse.utils.BulkPhotoProcessor.processPhotoSource


suspend fun processStudentAndRegister(
    context: Context,
    student: CsvStudentData
): ProcessResult {
    val photoResult = processPhotoSource(
        context = context,
        photoSource = student.photoUrl,
        studentId = student.studentId
    )

    if (!photoResult.success) {
        return ProcessResult(
            studentId = student.studentId,
            name = student.name,
            status = "Error",
            error = photoResult.error ?: "Photo processing failed",
            photoSize = photoResult.originalSize
        )
    }

    val bitmap = BitmapFactory.decodeFile(photoResult.localPhotoUrl)
        ?: return ProcessResult(
            studentId = student.studentId,
            name = student.name,
            status = "Error",
            error = "Failed to load processed photo",
            photoSize = photoResult.originalSize
        )

    val embeddingResult = processBitmapForFaceEmbedding(context, bitmap)
        ?: return ProcessResult(
            studentId = student.studentId,
            name = student.name,
            status = "Error",
            error = "No face detected",
            photoSize = photoResult.originalSize
        )

    val (faceBitmap, embedding) = embeddingResult

    val photoPath = saveFacePhoto(context, faceBitmap, student.studentId)
        ?: return ProcessResult(
            studentId = student.studentId,
            name = student.name,
            status = "Error",
            error = "Failed to save face photo",
            photoSize = photoResult.originalSize
        )

    return try {
        val viewModel = FaceViewModel(context.applicationContext as android.app.Application)

        // For now we call registerFace directly
        viewModel.registerFace(
            studentId = student.studentId,
            name = student.name,
            embedding = embedding,
            photoUrl = photoPath,
            className = student.className,
            subClass = student.subClass,
            grade = student.grade,
            subGrade = student.subGrade,
            program = student.program,
            role = student.role,
            onSuccess = {},
            onDuplicate = {}
        )

        ProcessResult(
            studentId = student.studentId,
            name = student.name,
            status = "Registered",
            photoSize = photoResult.processedSize
        )
    } catch (e: Exception) {
        ProcessResult(
            studentId = student.studentId,
            name = student.name,
            status = "Error",
            error = e.message ?: "Registration failed",
            photoSize = photoResult.originalSize
        )
    }
}
