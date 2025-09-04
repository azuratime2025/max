package com.example.crashcourse.utils

import kotlin.math.sqrt

/**
 * Computes the cosine distance between two embeddings.
 * Cosine distance = 1 - cosine similarity.
 * @param a first embedding vector
 * @param b second embedding vector
 * @return distance in range [0, 2]
 */
fun cosineDistance(a: FloatArray, b: FloatArray): Float {
    require(a.size == b.size) { "Embedding vectors must have the same length" }
    var dot = 0f
    var normA = 0f
    var normB = 0f
    for (i in a.indices) {
        dot += a[i] * b[i]
        normA += a[i] * a[i]
        normB += b[i] * b[i]
    }
    val denom = sqrt(normA) * sqrt(normB)
    if (denom == 0f) {
        // One of the vectors is zero-length; no meaningful similarity
        return 1f
    }
    val similarity = dot / denom
    // Clamp similarity to [-1, 1] to avoid NaN from numerical issues
    val sim = similarity.coerceIn(-1f, 1f)
    return 1f - sim
}
