package com.example.crashcourse.db

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.util.Log

/**
 * In‑memory cache for face embeddings to avoid repeated database reads.
 */
object FaceCache {
    private val cache = mutableListOf<Pair<String, FloatArray>>()

    /**
     * Loads all faces from the database on the IO dispatcher, caching them the first time.
     */
    suspend fun load(context: Context): List<Pair<String, FloatArray>> =
        withContext(Dispatchers.IO) {
            if (cache.isEmpty()) {
                Log.d("FaceCache", "Cache is empty, loading from database")
                // Retrieve all stored FaceEntity objects
                val faces = AppDatabase
                    .getInstance(context)
                    .faceDao()
                    .getAllFaces()

                Log.d("FaceCache", "Loaded ${faces.size} faces from database")
                faces.forEachIndexed { index, face ->
                    Log.d("FaceCache", "Face $index: ${face.name} (${face.studentId}), embedding size: ${face.embedding.size}")
                }

                // Map entities to name-embedding pairs and bulk-add to cache
                val pairs = faces.map { faceEntity: FaceEntity ->
                    faceEntity.name to faceEntity.embedding
                }

                cache.addAll(pairs)
                Log.d("FaceCache", "Added ${pairs.size} faces to cache")
            } else {
                Log.d("FaceCache", "Using cached data: ${cache.size} faces")
            }
            cache
        }

    /**
     * Loads all faces with their student IDs from the database.
     */
    suspend fun loadWithStudentIds(context: Context): List<Triple<String, String, FloatArray>> =
        withContext(Dispatchers.IO) {
            // Retrieve all stored FaceEntity objects
            val faces = AppDatabase
                .getInstance(context)
                .faceDao()
                .getAllFaces()

            // Map entities to studentId-name-embedding triples
            faces.map { faceEntity: FaceEntity ->
                Triple(faceEntity.studentId, faceEntity.name, faceEntity.embedding)
            }
        }

    /**
     * Clears the in-memory cache.
     */
    fun clear() {
        Log.d("FaceCache", "Clearing cache (had ${cache.size} faces)")
        cache.clear()
    }

    /**
     * Refreshes the cache by clearing and reloading from database.
     * Use this only when you know the database has been updated.
     */
    suspend fun refresh(context: Context): List<Pair<String, FloatArray>> {
        clear()
        return load(context)
    }
}
