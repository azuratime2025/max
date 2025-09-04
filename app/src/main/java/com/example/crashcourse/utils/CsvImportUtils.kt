package com.example.crashcourse.utils

import android.content.Context
import android.net.Uri
import android.util.Log
import java.io.BufferedReader
import java.io.InputStreamReader

object CsvImportUtils {
    private const val TAG = "CsvImportUtils"
    
    data class CsvStudentData(
        val studentId: String,
        val name: String,
        val className: String = "",
        val subClass: String = "",
        val grade: String = "",
        val subGrade: String = "",
        val program: String = "",
        val role: String = "",
        val photoUrl: String = ""
    )
    
    data class CsvParseResult(
        val students: List<CsvStudentData>,
        val errors: List<String>,
        val totalRows: Int,
        val validRows: Int
    )
    
    suspend fun parseCsvFile(context: Context, uri: Uri): CsvParseResult {
        val students = mutableListOf<CsvStudentData>()
        val errors = mutableListOf<String>()
        var totalRows = 0
        var validRows = 0
        
        try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                BufferedReader(InputStreamReader(inputStream)).use { reader ->
                    var line: String?
                    var lineNumber = 0
                    var headers: List<String>? = null
                    
                    while (reader.readLine().also { line = it } != null) {
                        lineNumber++
                        totalRows++
                        
                        val currentLine = line?.trim() ?: continue
                        if (currentLine.isEmpty()) {
                            totalRows-- // Don't count empty lines
                            continue
                        }
                        
                        val columns = parseCsvLine(currentLine)
                        
                        if (lineNumber == 1) {
                            headers = columns.map { it.trim().lowercase() }
                            totalRows-- // Don't count header as data row
                            continue
                        }

                        if (headers == null) {
                            errors.add("No headers found")
                            continue
                        }

                        if (columns.isEmpty()) {
                            totalRows-- // Don't count empty lines
                            continue
                        }
                        
                        try {
                            val student = parseStudentRow(headers, columns, lineNumber)
                            if (student != null) {
                                students.add(student)
                                validRows++
                            } else {
                                errors.add("Line $lineNumber: Failed to parse student data")
                            }
                        } catch (e: Exception) {
                            errors.add("Line $lineNumber: ${e.message ?: "Parsing error"}")
                        }
                    }
                }
            }
        } catch (e: Exception) {
            errors.add("Failed to read CSV file: ${e.message ?: "Unknown error"}")
        }
        
        return CsvParseResult(
            students = students,
            errors = errors,
            totalRows = totalRows,
            validRows = validRows
        )
    }
    
    private fun parseCsvLine(line: String): List<String> {
        val result = mutableListOf<String>()
        val current = StringBuilder()
        var inQuotes = false
        var i = 0
        
        while (i < line.length) {
            when (val char = line[i]) {
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
                        current.append(char)
                    } else {
                        result.add(current.toString().trim())
                        current.clear()
                    }
                }
                else -> current.append(char)
            }
            i++
        }
        
        // Add the last column
        result.add(current.toString().trim())
        return result
    }
    
    private fun parseStudentRow(
        headers: List<String>,
        columns: List<String>,
        lineNumber: Int
    ): CsvStudentData? {
        // Create header to index mapping
        val headerMap = headers.mapIndexed { index, header -> header to index }.toMap()
        
        // Helper function to get column value
        fun getValue(headerNames: List<String>): String {
            for (header in headerNames) {
                headerMap[header]?.takeIf { it < columns.size }?.let { 
                    return columns[it].trim() 
                }
            }
            return ""
        }
        
        // Extract fields
        val studentId = getValue(listOf("studentid", "student_id", "id", "student id", "student"))
        val name = getValue(listOf("name", "fullname", "student_name", "full name"))
        
        if (studentId.isEmpty() || name.isEmpty()) {
            return null
        }
        
        return CsvStudentData(
            studentId = studentId,
            name = name,
            className = getValue(listOf("class", "classname", "class_name", "class name")),
            subClass = getValue(listOf("subclass", "sub_class", "sub class")),
            grade = getValue(listOf("grade", "level")),
            subGrade = getValue(listOf("subgrade", "sub_grade", "sub grade")),
            program = getValue(listOf("program", "course")),
            role = getValue(listOf("role", "type", "position")).takeIf { it.isNotEmpty() } ?: "Student",
            photoUrl = getValue(listOf("photo", "photourl", "photo_url", "image", "photo url"))
        )
    }
    
    fun generateSampleCsv(): String {
        return """
            Student ID,Name,Class,Sub Class,Grade,Sub Grade,Program,Role,Photo URL
            STU001,John Doe,Class A,Sub A1,Grade 1,Sub 1A,Program X,Student,https://example.com/john.jpg
            STU002,Jane Smith,Class B,Sub B1,Grade 2,Sub 2A,Program Y,Student,https://example.com/jane.jpg
            TEA001,Mr. Johnson,,,,,,Teacher,https://example.com/teacher.jpg
        """.trimIndent()
    }
}