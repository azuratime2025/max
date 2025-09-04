package com.example.crashcourse.db

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity for class options dropdown
 */
@Entity(tableName = "class_options")
data class ClassOption(
    @PrimaryKey val id: Int = 0,
    val name: String,
    val displayOrder: Int = 0
)

/**
 * Entity for sub-class options dropdown
 */
@Entity(tableName = "subclass_options")
data class SubClassOption(
    @PrimaryKey val id: Int = 0,
    val name: String,
    val parentClassId: Int, // References ClassOption.id
    val displayOrder: Int = 0
)

/**
 * Entity for grade options dropdown
 */
@Entity(tableName = "grade_options")
data class GradeOption(
    @PrimaryKey val id: Int = 0,
    val name: String,
    val displayOrder: Int = 0
)

/**
 * Entity for sub-grade options dropdown
 */
@Entity(tableName = "subgrade_options")
data class SubGradeOption(
    @PrimaryKey val id: Int = 0,
    val name: String,
    val parentGradeId: Int, // References GradeOption.id
    val displayOrder: Int = 0
)

/**
 * Entity for program options dropdown
 */
@Entity(tableName = "program_options")
data class ProgramOption(
    @PrimaryKey val id: Int = 0,
    val name: String,
    val displayOrder: Int = 0
)

/**
 * Entity for role options dropdown
 */
@Entity(tableName = "role_options")
data class RoleOption(
    @PrimaryKey val id: Int = 0,
    val name: String,
    val displayOrder: Int = 0
)
