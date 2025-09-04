package com.example.crashcourse.utils

import android.content.Context
import android.os.Environment
import com.example.crashcourse.db.CheckInRecord
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.opencsv.CSVWriter
import java.io.File
import java.io.FileOutputStream
import java.time.format.DateTimeFormatter

object ExportUtils {
    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    val sharedDateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    fun exportToPdf(context: Context, records: List<CheckInRecord>): File {
        val fileName = "check_in_records_${System.currentTimeMillis()}.pdf"
        val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fileName)
        
        PdfWriter(file).use { writer ->
            val pdf = PdfDocument(writer)
            Document(pdf).use { document ->
                // Add title
                document.add(Paragraph("Check-in Records"))
                
                // Create table
                val table = Table(4) // 4 columns: Name, Timestamp, Class, Grade
                
                // Add headers
                table.addCell("Name")
                table.addCell("Timestamp")
                table.addCell("Class")
                table.addCell("Grade")
                
                // Add data
                records.forEach { record ->
                    table.addCell(record.name)
                    table.addCell(record.timestamp.format(dateFormatter))
                    table.addCell(record.className ?: "")
                    table.addCell(record.gradeName ?: "")
                }
                
                document.add(table)
            }
        }
        
        return file
    }

    fun exportToCsv(context: Context, records: List<CheckInRecord>): File {
        val fileName = "check_in_records_${System.currentTimeMillis()}.csv"
        val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fileName)
        
        CSVWriter(file.writer()).use { writer ->
            // Write header
            writer.writeNext(arrayOf("Name", "Timestamp", "Class", "Grade"))
            
            // Write data
            records.forEach { record ->
                writer.writeNext(arrayOf(
                    record.name,
                    record.timestamp.format(dateFormatter),
                    record.className ?: "",
                    record.gradeName ?: ""
                ))
            }
        }
        
        return file
    }
}
