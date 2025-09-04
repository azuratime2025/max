package com.example.crashcourse.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "check_in_records")
data class CheckInRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val timestamp: LocalDateTime,
    val faceId: Int,
    val classId: Int?,
    val subClassId: Int?,
    val gradeId: Int?,
    val subGradeId: Int?,
    val programId: Int?,
    val roleId: Int?,
    val className: String? = null,
    val gradeName: String? = null
)
