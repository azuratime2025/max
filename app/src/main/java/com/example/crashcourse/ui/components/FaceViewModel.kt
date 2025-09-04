package com.example.crashcourse.ui.components

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.crashcourse.db.*
import com.example.crashcourse.ml.FaceRecognitionHelper
import com.example.crashcourse.utils.cosineDistance
import com.example.crashcourse.utils.PhotoStorageUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.util.Log

class FaceViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getInstance(application)
    private val faceDao = database.faceDao()
    private val classOptionDao = database.classOptionDao()
    private val subClassOptionDao = database.subClassOptionDao()
    private val gradeOptionDao = database.gradeOptionDao()
    private val subGradeOptionDao = database.subGradeOptionDao()
    private val programOptionDao = database.programOptionDao()
    private val roleOptionDao = database.roleOptionDao()
    private val helper = FaceRecognitionHelper(application)

    companion object {
        // distance ≤ 0.3 for duplicate detection during registration
        private const val DUPLICATE_DISTANCE_THRESHOLD = 0.3f
        // distance < 0.8 for recognition during check-in (more lenient for testing)
        const val RECOGNITION_DISTANCE_THRESHOLD = 0.8f
    }

    /** Exposes a StateFlow of all FaceEntity in the DB */
    val faceList: StateFlow<List<FaceEntity>> =
        faceDao.getAllFacesFlow()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // Dropdown options as StateFlows
    val classOptions: StateFlow<List<ClassOption>> =
        classOptionDao.getAllOptions()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val gradeOptions: StateFlow<List<GradeOption>> =
        gradeOptionDao.getAllOptions()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val programOptions: StateFlow<List<ProgramOption>> =
        programOptionDao.getAllOptions()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val roleOptions: StateFlow<List<RoleOption>> =
        roleOptionDao.getAllOptions()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // Functions to get dependent dropdown options
    fun getSubClassOptions(parentClassId: Int): Flow<List<SubClassOption>> {
        return subClassOptionDao.getOptionsForClass(parentClassId)
    }

    fun getSubGradeOptions(parentGradeId: Int): Flow<List<SubGradeOption>> {
        return subGradeOptionDao.getOptionsForGrade(parentGradeId)
    }

    /** Populates initial dropdown options if they don't exist */
    fun populateInitialOptions() {
        viewModelScope.launch(Dispatchers.IO) {
            // Only add if no options exist
            if (classOptionDao.getAllOptions().stateIn(viewModelScope, SharingStarted.WhileSubscribed(100), emptyList()).value.isEmpty()) {
                // Add sample class options
                val classOptions = listOf(
                    ClassOption(id = 1, name = "Class A", displayOrder = 1),
                    ClassOption(id = 2, name = "Class B", displayOrder = 2),
                    ClassOption(id = 3, name = "Class C", displayOrder = 3)
                )
                classOptionDao.insertAll(classOptions)

                // Add sample subclass options
                val subClassOptions = listOf(
                    SubClassOption(id = 1, name = "Subclass A1", parentClassId = 1, displayOrder = 1),
                    SubClassOption(id = 2, name = "Subclass A2", parentClassId = 1, displayOrder = 2),
                    SubClassOption(id = 3, name = "Subclass B1", parentClassId = 2, displayOrder = 1),
                    SubClassOption(id = 4, name = "Subclass C1", parentClassId = 3, displayOrder = 1)
                )
                subClassOptionDao.insertAll(subClassOptions)

                // Add sample grade options
                val gradeOptions = listOf(
                    GradeOption(id = 1, name = "Grade 1", displayOrder = 1),
                    GradeOption(id = 2, name = "Grade 2", displayOrder = 2),
                    GradeOption(id = 3, name = "Grade 3", displayOrder = 3)
                )
                gradeOptionDao.insertAll(gradeOptions)

                // Add sample subgrade options
                val subGradeOptions = listOf(
                    SubGradeOption(id = 1, name = "Section A", parentGradeId = 1, displayOrder = 1),
                    SubGradeOption(id = 2, name = "Section B", parentGradeId = 1, displayOrder = 2),
                    SubGradeOption(id = 3, name = "Section A", parentGradeId = 2, displayOrder = 1)
                )
                subGradeOptionDao.insertAll(subGradeOptions)

                // Add sample program options
                val programOptions = listOf(
                    ProgramOption(id = 1, name = "Regular", displayOrder = 1),
                    ProgramOption(id = 2, name = "Special", displayOrder = 2),
                    ProgramOption(id = 3, name = "Advanced", displayOrder = 3)
                )
                programOptionDao.insertAll(programOptions)

                // Add sample role options
                val roleOptions = listOf(
                    RoleOption(id = 1, name = "Student", displayOrder = 1),
                    RoleOption(id = 2, name = "Teacher", displayOrder = 2),
                    RoleOption(id = 3, name = "Staff", displayOrder = 3),
                    RoleOption(id = 4, name = "Admin", displayOrder = 4)
                )
                roleOptionDao.insertAll(roleOptions)
            }
        }
    }

    /**
     * Registers a new face with student information, blocking duplicates.
     * onSuccess(): called if insert completes
     * onDuplicate(existingName): called if a similar face was found
     */
    fun registerFace(
        studentId: String,
        name: String,
        embedding: FloatArray,
        photoUrl: String? = null,
        className: String = "",
        subClass: String = "",
        grade: String = "",
        subGrade: String = "",
        program: String = "",
        role: String = "",
        onSuccess: () -> Unit,
        onDuplicate: (existingName: String) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            Log.d("FaceViewModel", "Registering face: $name ($studentId)")
            Log.d("FaceViewModel", "Embedding size: ${embedding.size}")
            Log.d("FaceViewModel", "First 5 embedding values: ${embedding.take(5).joinToString(", ")}")

            // Check if student ID already exists
            val existingFace = faceDao.getFaceByStudentId(studentId)
            if (existingFace != null) {
                Log.d("FaceViewModel", "Student ID already exists: $studentId")
                withContext(Dispatchers.Main) {
                    onDuplicate(existingFace.name)
                }
                return@launch
            }

            // Check for duplicate face using embedding
            val cached = FaceCache.load(getApplication())
            Log.d("FaceViewModel", "Loaded ${cached.size} faces from cache for duplicate check")
            var bestName: String? = null
            var bestDist = Float.MAX_VALUE
            for ((existingName, existingEmb) in cached) {
                val dist = cosineDistance(existingEmb, embedding)
                Log.d("FaceViewModel", "Distance to $existingName: $dist")
                if (dist < bestDist) {
                    bestDist = dist
                    bestName = existingName
                }
            }
            Log.d("FaceViewModel", "Best match: $bestName, distance: $bestDist, threshold: $DUPLICATE_DISTANCE_THRESHOLD")

            if (bestName != null && bestDist <= DUPLICATE_DISTANCE_THRESHOLD) {
                Log.d("FaceViewModel", "Duplicate face detected: $bestName")
                withContext(Dispatchers.Main) {
                    onDuplicate(bestName)
                }
                return@launch
            }

            // Insert new face
            val face = FaceEntity(
                studentId = studentId,
                name = name,
                photoUrl = photoUrl,
                embedding = embedding,
                className = className,
                subClass = subClass,
                grade = grade,
                subGrade = subGrade,
                program = program,
                role = role,
                timestamp = System.currentTimeMillis()
            )
            Log.d("FaceViewModel", "Inserting face into database: $name")
            faceDao.insert(face)
            Log.d("FaceViewModel", "Face inserted successfully, refreshing cache")
            FaceCache.refresh(getApplication())
            withContext(Dispatchers.Main) {
                onSuccess()
            }
        }
    }

    /** Deletes a face record */
    fun deleteFace(face: FaceEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            faceDao.delete(face)
            FaceCache.refresh(getApplication())
        }
    }

    /** Edits a face record */
    fun updateFace(face: FaceEntity, onComplete: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            faceDao.update(face)
            FaceCache.refresh(getApplication())
            withContext(Dispatchers.Main) {
                onComplete()
            }
        }
    }

    /** Updates a face record with new photo */
    fun updateFaceWithPhoto(
        face: FaceEntity,
        photoBitmap: Bitmap?,
        embedding: FloatArray,
        onComplete: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Save the new photo if provided
                val photoUrl = if (photoBitmap != null) {
                    PhotoStorageUtils.saveFacePhoto(getApplication(), photoBitmap, face.studentId)
                } else {
                    face.photoUrl // Keep existing photo URL
                }

                // Clean up old photos if we have a new one
                if (photoUrl != null && photoUrl != face.photoUrl) {
                    PhotoStorageUtils.cleanupOldPhotos(getApplication(), face.studentId, photoUrl)
                }

                // Update the face entity
                val updatedFace = face.copy(
                    photoUrl = photoUrl,
                    embedding = embedding
                )

                faceDao.update(updatedFace)
                FaceCache.refresh(getApplication())

                withContext(Dispatchers.Main) {
                    onComplete()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onError("Failed to update face: ${e.message}")
                }
            }
        }
    }

    /**
     * Simplified version of registerFace that maintains backward compatibility
     * with the existing UI
     */
    fun registerFace(
        name: String,
        embedding: FloatArray,
        onSuccess: () -> Unit,
        onDuplicate: (existingName: String) -> Unit
    ) {
        // Generate a random student ID for backward compatibility
        val studentId = "STU" + System.currentTimeMillis().toString()
        registerFace(
            studentId = studentId,
            name = name,
            embedding = embedding,
            onSuccess = onSuccess,
            onDuplicate = onDuplicate
        )
    }
}
