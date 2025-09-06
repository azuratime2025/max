package com.example.crashcourse.viewmodel

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.crashcourse.ui.components.FaceViewModel
import com.example.crashcourse.utils.BulkPhotoProcessor
import com.example.crashcourse.utils.CsvImportUtils
import com.example.crashcourse.utils.PhotoProcessingUtils
import com.example.crashcourse.utils.PhotoStorageUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.example.crashcourse.utils.ProcessResult

class BulkRegistrationViewModel : ViewModel() {
    private val _state = MutableStateFlow(ProcessingState())
    val state: StateFlow<ProcessingState> = _state.asStateFlow()

    private var photoSources = listOf<String>()
    private var lastStudents = mapOf<String, CsvImportUtils.CsvStudentData>()

    fun prepareProcessing(context: Context, uri: Uri) {
        viewModelScope.launch {
            try {
                val csvResult = CsvImportUtils.parseCsvFile(context, uri)
            // simpan ke cache untuk re-run
            lastStudents = csvResult.students.associateBy { it.studentId }
                photoSources = csvResult.students.map { it.photoUrl }
                
                val seconds = BulkPhotoProcessor.estimateProcessingTime(photoSources)
                val estimate = when {
                    seconds > 120 -> "${seconds / 60} minutes"
                    seconds > 60 -> "1 minute ${seconds % 60} seconds"
                    else -> "$seconds seconds"
                }
                
                _state.value = _state.value.copy(
                    estimatedTime = "Estimated time: $estimate"
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    estimatedTime = "Time estimate unavailable"
                )
            }
        }
    }

    fun processCsvFile(context: Context, uri: Uri, faceViewModel: FaceViewModel) {
        viewModelScope.launch {
            try {
                _state.value = ProcessingState(
                    isProcessing = true,
                    status = "Parsing CSV file...",
                    estimatedTime = state.value.estimatedTime
                )

                val csvResult = CsvImportUtils.parseCsvFile(context, uri)
                if (csvResult.students.isEmpty()) {
                    _state.value = _state.value.copy(
                        isProcessing = false,
                        status = "No valid students found in CSV",
                        errorCount = csvResult.errors.size
                    )
                    return@launch
                }

                val results = mutableListOf<ProcessResult>()
                var successCount = 0
                var duplicateCount = 0
                var errorCount = 0
                val totalStudents = csvResult.students.size

                csvResult.students.forEachIndexed { index, student ->
                    try {
                        val photoType = BulkPhotoProcessor.getPhotoSourceType(student.photoUrl)
                        _state.value = _state.value.copy(
                            progress = (index + 1).toFloat() / totalStudents,
                            status = "Processing ${index + 1}/$totalStudents: ${student.name}",
                            currentPhotoType = "Photo source: $photoType",
                            currentPhotoSize = ""
                        )

                        val result = processStudent(context, student, faceViewModel)
                        when {
                            result.status == "Registered" -> successCount++
                            result.status.startsWith("Duplicate") -> duplicateCount++
                            else -> errorCount++
                        }
                        results.add(result)
                        
                        _state.value = _state.value.copy(
                            currentPhotoSize = "Photo size: ${formatFileSize(result.photoSize)}"
                        )
                    } catch (e: Exception) {
                        errorCount++
                        results.add(
                            ProcessResult(
                                studentId = student.studentId,
                                name = student.name,
                                status = "Error",
                                error = e.message ?: "Unknown error"
                            )
                        )
                    }
                }

                _state.value = ProcessingState(
                    isProcessing = false,
                    results = results,
                    successCount = successCount,
                    duplicateCount = duplicateCount,
                    errorCount = errorCount,
                    status = "Processed $successCount students successfully"
                )
            } catch (e: Exception) {
                _state.value = ProcessingState(
                    isProcessing = false,
                    status = "Processing failed: ${e.message}",
                    errorCount = 1
                )
            }
        }
    }

    private suspend fun processStudent(
        context: Context,
        student: CsvImportUtils.CsvStudentData,
        faceViewModel: com.example.crashcourse.ui.components.FaceViewModel
    ): ProcessResult {
        val photoResult = BulkPhotoProcessor.processPhotoSource(
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
        
        val embeddingResult = PhotoProcessingUtils.processBitmapForFaceEmbedding(context, bitmap)
            ?: return ProcessResult(
                studentId = student.studentId,
                name = student.name,
                status = "Error",
                error = "No face detected",
                photoSize = photoResult.originalSize
            )

        val (faceBitmap, embedding) = embeddingResult

        val photoPath = PhotoStorageUtils.saveFacePhoto(context, faceBitmap, student.studentId)
            ?: return ProcessResult(
                studentId = student.studentId,
                name = student.name,
                status = "Error",
                error = "Failed to save face photo",
                photoSize = photoResult.originalSize
            )

        try {
            faceViewModel.registerFace(
                studentId = student.studentId,
                name      = student.name,
                embedding = embedding,
                photoUrl  = photoPath,
                className = student.className,
                subClass  = student.subClass,
                grade     = student.grade,
                subGrade  = student.subGrade,
                program   = student.program,
                role      = student.role,
                onSuccess = { /* no-op */ },
                onDuplicate = { /* no-op: agregat status tetap dari VM */ }
            )
        } catch (e: Exception) {
            return ProcessResult(
                studentId = student.studentId,
                name = student.name,
                status = "Error",
                error = e.message ?: "Registration failed",
                photoSize = photoResult.originalSize
            )
        }

        return try {
            // In a real app, save to database here
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

    private fun formatFileSize(size: Long): String {
        return when {
            size == 0L -> "0 KB"
            size < 1024 -> "$size B"
            size < 1024 * 1024 -> "${size / 1024} KB"
            else -> String.format("%.1f MB", size / (1024.0 * 1024.0))
        }
    }

    fun resetState() {
        _state.value = ProcessingState()
    }

    /** Re-run hanya rows yang sebelumnya Error */
    fun rerunFailed(context: Context, faceViewModel: com.example.crashcourse.ui.components.FaceViewModel) {
        val current = state.value
        if (current.isProcessing) return
        val failedIds = current.results.filter { it.status == "Error" }.map { it.studentId }
        if (failedIds.isEmpty()) return

        viewModelScope.launch {
            try {
                _state.value = current.copy(
                    isProcessing = true,
                    status = "Re-running ${failedIds.size} failed rows..."
                )

                val newResults = current.results.toMutableList()
                var addSuccess = 0
                var addDuplicate = 0
                var addError = 0

                for (sid in failedIds) {
                    val student = lastStudents[sid]
                    if (student == null) {
                        val idx = newResults.indexOfFirst { it.studentId == sid }
                        if (idx >= 0) {
                            newResults[idx] = newResults[idx].copy(
                                status = "Error",
                                error = "Missing cached data"
                            )
                        }
                        addError++
                        continue
                    }

                    try {
                        val result = processStudent(context, student, faceViewModel)
                        val idx = newResults.indexOfFirst { it.studentId == sid }
                        if (idx >= 0) newResults[idx] = result else newResults.add(result)
                        when {
                            result.status == "Registered" -> addSuccess++
                            result.status.startsWith("Duplicate") -> addDuplicate++
                            else -> addError++
                        }
                    } catch (e: Exception) {
                        val idx = newResults.indexOfFirst { it.studentId == sid }
                        val fallback = com.example.crashcourse.utils.ProcessResult(
                            studentId = sid,
                            name = lastStudents[sid]?.name ?: sid,
                            status = "Error",
                            error = e.message ?: "Re-run failed"
                        )
                        if (idx >= 0) newResults[idx] = fallback else newResults.add(fallback)
                        addError++
                    }
                }

                val successCount = newResults.count { it.status == "Registered" }
                val duplicateCount = newResults.count { it.status.startsWith("Duplicate") }
                val errorCount = newResults.count { it.status == "Error" }

                _state.value = current.copy(
                    isProcessing = false,
                    results = newResults,
                    successCount = successCount,
                    duplicateCount = duplicateCount,
                    errorCount = errorCount,
                    status = "Re-run done: +$addSuccess ok, +$addDuplicate dup, +$addError err"
                )
            } catch (e: Exception) {
                _state.value = current.copy(
                    isProcessing = false,
                    status = "Re-run failed: ${e.message}"
                )
            }
        }
    }

}
