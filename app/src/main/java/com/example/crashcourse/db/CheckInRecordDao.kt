package com.example.crashcourse.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

@Dao
interface CheckInRecordDao {
    @Query("SELECT * FROM check_in_records ORDER BY timestamp DESC")
    fun getAllRecords(): Flow<List<CheckInRecord>>

    @Query("""
        SELECT * FROM check_in_records 
        WHERE (:nameFilter = '' OR name LIKE '%' || :nameFilter || '%')
        AND (:startDate IS NULL OR timestamp >= :startDate)
        AND (:endDate IS NULL OR timestamp <= :endDate)
        AND (:classId IS NULL OR classId = :classId)
        AND (:subClassId IS NULL OR subClassId = :subClassId)
        AND (:gradeId IS NULL OR gradeId = :gradeId)
        AND (:subGradeId IS NULL OR subGradeId = :subGradeId)
        AND (:programId IS NULL OR programId = :programId)
        AND (:roleId IS NULL OR roleId = :roleId)
        ORDER BY timestamp DESC
    """)
    fun getFilteredRecords(
        nameFilter: String = "",
        startDate: LocalDateTime? = null,
        endDate: LocalDateTime? = null,
        classId: Int? = null,
        subClassId: Int? = null,
        gradeId: Int? = null,
        subGradeId: Int? = null,
        programId: Int? = null,
        roleId: Int? = null
    ): Flow<List<CheckInRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: CheckInRecord)

    @Update
    suspend fun update(record: CheckInRecord)

    @Delete
    suspend fun delete(record: CheckInRecord)
}
