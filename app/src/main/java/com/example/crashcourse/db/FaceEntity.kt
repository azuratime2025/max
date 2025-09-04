package com.example.crashcourse.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "faces")
data class FaceEntity(
    @PrimaryKey val studentId: String, // Use student ID as unique key
    val name: String,
    val photoUrl: String? = null, // Optional, if you want to store image URL
    val embedding: FloatArray,

    // These link to dropdowns
    val className: String = "",
    val subClass: String = "",
    val grade: String = "",
    val subGrade: String = "",
    val program: String = "",
    val role: String = "",

    val timestamp: Long = System.currentTimeMillis()
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is FaceEntity) return false

        return studentId == other.studentId &&
                name == other.name &&
                photoUrl == other.photoUrl &&
                embedding.contentEquals(other.embedding) &&
                className == other.className &&
                subClass == other.subClass &&
                grade == other.grade &&
                subGrade == other.subGrade &&
                program == other.program &&
                role == other.role &&
                timestamp == other.timestamp
    }

    override fun hashCode(): Int {
        var result = studentId.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + (photoUrl?.hashCode() ?: 0)
        result = 31 * result + embedding.contentHashCode()
        result = 31 * result + className.hashCode()
        result = 31 * result + subClass.hashCode()
        result = 31 * result + grade.hashCode()
        result = 31 * result + subGrade.hashCode()
        result = 31 * result + program.hashCode()
        result = 31 * result + role.hashCode()
        result = 31 * result + timestamp.hashCode()
        return result
    }
}
