package com.example.crashcourse.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ClassOptionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(option: ClassOption)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(options: List<ClassOption>)

    @Query("SELECT * FROM class_options ORDER BY displayOrder ASC")
    fun getAllOptions(): Flow<List<ClassOption>>

    @Query("SELECT * FROM class_options WHERE id = :id")
    suspend fun getOptionById(id: Int): ClassOption?

    @Update
    suspend fun update(option: ClassOption)

    @Delete
    suspend fun delete(option: ClassOption)

    @Query("SELECT MAX(id) FROM class_options")
    suspend fun getMaxId(): Int?
}

@Dao
interface SubClassOptionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(option: SubClassOption)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(options: List<SubClassOption>)

    @Query("SELECT * FROM subclass_options ORDER BY displayOrder ASC")
    fun getAllOptions(): Flow<List<SubClassOption>>

    @Query("SELECT * FROM subclass_options WHERE parentClassId = :parentClassId ORDER BY displayOrder ASC")
    fun getOptionsForClass(parentClassId: Int): Flow<List<SubClassOption>>

    @Update
    suspend fun update(option: SubClassOption)

    @Delete
    suspend fun delete(option: SubClassOption)

    @Query("SELECT MAX(id) FROM subclass_options")
    suspend fun getMaxId(): Int?
}

@Dao
interface GradeOptionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(option: GradeOption)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(options: List<GradeOption>)

    @Query("SELECT * FROM grade_options ORDER BY displayOrder ASC")
    fun getAllOptions(): Flow<List<GradeOption>>

    @Query("SELECT * FROM grade_options WHERE id = :id")
    suspend fun getOptionById(id: Int): GradeOption?

    @Update
    suspend fun update(option: GradeOption)

    @Delete
    suspend fun delete(option: GradeOption)

    @Query("SELECT MAX(id) FROM grade_options")
    suspend fun getMaxId(): Int?
}

@Dao
interface SubGradeOptionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(option: SubGradeOption)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(options: List<SubGradeOption>)

    @Query("SELECT * FROM subgrade_options ORDER BY displayOrder ASC")
    fun getAllOptions(): Flow<List<SubGradeOption>>

    @Query("SELECT * FROM subgrade_options WHERE parentGradeId = :parentGradeId ORDER BY displayOrder ASC")
    fun getOptionsForGrade(parentGradeId: Int): Flow<List<SubGradeOption>>

    @Query("SELECT * FROM subgrade_options WHERE id = :id")
    suspend fun getOptionById(id: Int): SubGradeOption?

    @Update
    suspend fun update(option: SubGradeOption)

    @Delete
    suspend fun delete(option: SubGradeOption)

    @Query("SELECT MAX(id) FROM subgrade_options")
    suspend fun getMaxId(): Int?
}

@Dao
interface ProgramOptionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(option: ProgramOption)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(options: List<ProgramOption>)

    @Query("SELECT * FROM program_options ORDER BY displayOrder ASC")
    fun getAllOptions(): Flow<List<ProgramOption>>

    @Query("SELECT * FROM program_options WHERE id = :id")
    suspend fun getOptionById(id: Int): ProgramOption?

    @Update
    suspend fun update(option: ProgramOption)

    @Delete
    suspend fun delete(option: ProgramOption)

    @Query("SELECT MAX(id) FROM program_options")
    suspend fun getMaxId(): Int?
}

@Dao
interface RoleOptionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(option: RoleOption)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(options: List<RoleOption>)

    @Query("SELECT * FROM role_options ORDER BY displayOrder ASC")
    fun getAllOptions(): Flow<List<RoleOption>>

    @Query("SELECT * FROM role_options WHERE id = :id")
    suspend fun getOptionById(id: Int): RoleOption?

    @Update
    suspend fun update(option: RoleOption)

    @Delete
    suspend fun delete(option: RoleOption)

    @Query("SELECT MAX(id) FROM role_options")
    suspend fun getMaxId(): Int?
}
