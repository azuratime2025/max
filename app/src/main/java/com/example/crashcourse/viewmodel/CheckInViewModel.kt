package com.example.crashcourse.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.Flow
import com.example.crashcourse.db.AppDatabase
import com.example.crashcourse.db.CheckInRecord
import com.example.crashcourse.utils.ExportUtils
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class CheckInViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getInstance(application)
    private val checkInRecordDao = database.checkInRecordDao()
    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    
    fun exportToPdf(records: List<CheckInRecord>): File {
        return ExportUtils.exportToPdf(getApplication(), records)
    }
    
    fun exportToCsv(records: List<CheckInRecord>): File {
        return ExportUtils.exportToCsv(getApplication(), records)
    }

    fun getFilteredCheckIns(
        nameFilter: String = "",
        startDate: String = "",
        endDate: String = "",
        classId: Int? = null,
        subClassId: Int? = null,
        gradeId: Int? = null,
        subGradeId: Int? = null,
        programId: Int? = null,
        roleId: Int? = null
    ): Flow<List<CheckInRecord>> {
        val parsedStartDate = if (startDate.isNotBlank()) {
            LocalDateTime.parse("$startDate 00:00:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        } else null

        val parsedEndDate = if (endDate.isNotBlank()) {
            LocalDateTime.parse("$endDate 23:59:59", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        } else null

        return checkInRecordDao.getFilteredRecords(
            nameFilter = nameFilter,
            startDate = parsedStartDate,
            endDate = parsedEndDate,
            classId = classId,
            subClassId = subClassId,
            gradeId = gradeId,
            subGradeId = subGradeId,
            programId = programId,
            roleId = roleId
        )
    }
}
