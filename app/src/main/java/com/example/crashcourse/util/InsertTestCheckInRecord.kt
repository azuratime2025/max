package com.example.crashcourse.util

import android.content.Context
import com.example.crashcourse.db.AppDatabase
import com.example.crashcourse.db.CheckInRecord
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDateTime

object InsertTestCheckInRecord {
    fun insert(context: Context) {
        val db = AppDatabase.getInstance(context)
        val dao = db.checkInRecordDao()
        CoroutineScope(Dispatchers.IO).launch {
            dao.insert(
                CheckInRecord(
                    name = "Test User",
                    timestamp = LocalDateTime.now(),
                    faceId = 1,
                    classId = null,
                    subClassId = null,
                    gradeId = null,
                    subGradeId = null,
                    programId = null,
                    roleId = null,
                    className = "Test Class",
                    gradeName = "Test Grade"
                )
            )
        }
    }
}
