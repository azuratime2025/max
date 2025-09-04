package com.example.crashcourse.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        ClassOption::class,
        SubClassOption::class,
        GradeOption::class,
        SubGradeOption::class,
        ProgramOption::class,
        RoleOption::class,
        FaceEntity::class,
        CheckInRecord::class
    ],
    version = 3,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun faceDao(): FaceDao
    abstract fun classOptionDao(): ClassOptionDao
    abstract fun subClassOptionDao(): SubClassOptionDao
    abstract fun gradeOptionDao(): GradeOptionDao
    abstract fun subGradeOptionDao(): SubGradeOptionDao
    abstract fun programOptionDao(): ProgramOptionDao
    abstract fun roleOptionDao(): RoleOptionDao
    abstract fun checkInRecordDao(): CheckInRecordDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "face_db"
                )
                .fallbackToDestructiveMigration() // For simplicity, recreate DB on schema change 
                .build().also { INSTANCE = it }
            }
    }
}
