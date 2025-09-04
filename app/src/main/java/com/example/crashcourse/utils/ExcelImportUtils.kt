package com.example.crashcourse.utils

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader

data class StudentData(
    val studentId: String,
    val name: String,
    val className: String = "",
    val subClass: String = "",
    val grade: String = "",
    val subGrade: String = "",
    val program: String = "",
    val role: String = ""
)

data class ImportResult(
    val success: Boolean,
    val data: List<StudentData> = emptyList(),
    val errors: List<String> = emptyList(),
    val totalRows: Int = 0,
    val validRows: Int = 0
)

object ExcelImportUtils {

    /**
     * Import student data from Excel file (not supported)
     */
    fun importFromExcel(context: Context, uri: Uri): ImportResult {
        return ImportResult(
            success = false,
            errors = listOf("Excel import is not supported. Please convert to CSV format.")
        )
    }

    /**
     * Import student data from CSV file with robust validation
     */
    fun importFromCsv(context: Context, uri: Uri): ImportResult {
        return try {
            // 1. Validate file type
            val mimeType = context.contentResolver.getType(uri)?.lowercase() ?: ""
            var fileName = ""
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (cursor.moveToFirst() && nameIndex != -1) {
                    fileName = cursor.getString(nameIndex)
                }
            }
            
            val isCsv = when {
                mimeType.contains("csv") -> true
                fileName.endsWith(".csv", ignoreCase = true) -> true
                uri.toString().lowercase().contains(".csv") -> true
                else -> false
            }
            
            if (!isCsv) {
                return ImportResult(
                    success = false, 
                    errors = listOf("Invalid file type. Detected: $mimeType/$fileName. Only CSV files are supported.")
                )
            }

            // 2. Open and parse file
            val inputStream: InputStream = context.contentResolver.openInputStream(uri)
                ?: return ImportResult(false, errors = listOf("Could not open file"))
            
            val reader = BufferedReader(InputStreamReader(inputStream))
            val lines = reader.readLines()
            reader.close()
            
            if (lines.isEmpty()) {
                return ImportResult(false, errors = listOf("CSV file is empty"))
            }
            
            // 3. Parse CSV with robust handling
            val students = mutableListOf<StudentData>()
            val errors = mutableListOf<String>()
            var totalRows = 0
            var validRows = 0
            
            // Parse header
            val header = lines[0].split(",").map { it.trim().lowercase() }
            val colMap = mutableMapOf<String, Int>()
            header.forEachIndexed { idx, col ->
                when {
                    col.contains("student") && col.contains("id") -> colMap["studentId"] = idx
                    col.contains("name") -> colMap["name"] = idx
                    col.contains("class") && !col.contains("sub") -> colMap["className"] = idx
                    col.contains("sub") && col.contains("class") -> colMap["subClass"] = idx
                    col == "grade" -> colMap["grade"] = idx
                    col.contains("sub") && col.contains("grade") -> colMap["subGrade"] = idx
                    col == "program" -> colMap["program"] = idx
                    col == "role" -> colMap["role"] = idx
                }
            }
            
            // Validate required columns
            if (!colMap.containsKey("studentId") || !colMap.containsKey("name")) {
                return ImportResult(
                    success = false,
                    errors = listOf(
                        "CSV file must have 'Student ID' and 'Name' columns",
                        "Detected columns: ${header.joinToString()}"
                    )
                )
            }
            
            // Process rows
            for (i in 1 until lines.size) {
                totalRows++
                val line = lines[i].trim()
                if (line.isEmpty()) continue
                
                try {
                    val row = parseCsvLine(line)
                    val studentId = colMap["studentId"]?.let { row.getOrNull(it)?.trim() } ?: ""
                    val name = colMap["name"]?.let { row.getOrNull(it)?.trim() } ?: ""
                    
                    if (studentId.isBlank() || name.isBlank()) {
                        errors.add("Row ${i + 1}: Student ID and Name are required")
                        continue
                    }
                    
                    students.add(
                        StudentData(
                            studentId = studentId,
                            name = name,
                            className = colMap["className"]?.let { row.getOrNull(it)?.trim() } ?: "",
                            subClass = colMap["subClass"]?.let { row.getOrNull(it)?.trim() } ?: "",
                            grade = colMap["grade"]?.let { row.getOrNull(it)?.trim() } ?: "",
                            subGrade = colMap["subGrade"]?.let { row.getOrNull(it)?.trim() } ?: "",
                            program = colMap["program"]?.let { row.getOrNull(it)?.trim() } ?: "",
                            role = colMap["role"]?.let { row.getOrNull(it)?.trim() } ?: "Student"
                        )
                    )
                    validRows++
                } catch (e: Exception) {
                    errors.add("Row ${i + 1}: ${e.message ?: "Invalid format"}")
                }
            }
            
            ImportResult(
                success = students.isNotEmpty(),
                data = students,
                errors = errors,
                totalRows = totalRows,
                validRows = validRows
            )
        } catch (e: Exception) {
            ImportResult(
                success = false, 
                errors = listOf("Failed to process CSV: ${e.message ?: "Unknown error"}")
            )
        }
    }

    /**
     * Robust CSV line parser that handles quoted fields
     */
    private fun parseCsvLine(line: String): List<String> {
        val result = mutableListOf<String>()
        val current = StringBuilder()
        var inQuotes = false
        var i = 0
        
        while (i < line.length) {
            when (val c = line[i]) {
                '"' -> {
                    if (inQuotes && i + 1 < line.length && line[i + 1] == '"') {
                        // Escaped quote
                        current.append('"')
                        i++ // Skip next quote
                    } else {
                        inQuotes = !inQuotes
                    }
                }
                ',' -> {
                    if (inQuotes) {
                        current.append(c)
                    } else {
                        result.add(current.toString().trim())
                        current.clear()
                    }
                }
                else -> current.append(c)
            }
            i++
        }
        result.add(current.toString().trim())
        return result
    }

    /**
     * Generate CSV template
     */
    fun generateSampleTemplate(context: Context): String {
        return """
            Student ID,Name,Class,Sub Class,Grade,Sub Grade,Program,Role
            STU001,John Doe,Class A,Sub A1,Grade 1,Sub 1A,Program X,Student
            STU002,Jane Smith,Class B,Sub B1,Grade 2,Sub 2A,Program Y,Student
            TEA001,Mr. Johnson,,,,,Teacher
        """.trimIndent()
    }
}