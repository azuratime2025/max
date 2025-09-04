package com.example.crashcourse.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface FaceDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(face: FaceEntity)

    @Delete
    suspend fun delete(face: FaceEntity)

    @Update
    suspend fun update(face: FaceEntity)

    @Query("SELECT * FROM faces ORDER BY timestamp DESC")
    fun getAllFacesFlow(): Flow<List<FaceEntity>>

    @Query("SELECT * FROM faces")
    suspend fun getAllFaces(): List<FaceEntity>

    @Query("SELECT * FROM faces WHERE studentId = :studentId")
    suspend fun getFaceByStudentId(studentId: String): FaceEntity?

    @Query("SELECT * FROM faces WHERE className = :className")
    fun getFacesByClassName(className: String): Flow<List<FaceEntity>>

    @Query("SELECT * FROM faces WHERE grade = :grade")
    fun getFacesByGrade(grade: String): Flow<List<FaceEntity>>

    @Query("SELECT * FROM faces WHERE program = :program")
    fun getFacesByProgram(program: String): Flow<List<FaceEntity>>

    @Query("SELECT * FROM faces WHERE role = :role")
    fun getFacesByRole(role: String): Flow<List<FaceEntity>>
}
