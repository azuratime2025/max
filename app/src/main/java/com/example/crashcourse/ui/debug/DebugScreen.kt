package com.example.crashcourse.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.crashcourse.db.AppDatabase
import com.example.crashcourse.db.FaceCache
import com.example.crashcourse.db.FaceEntity
import com.example.crashcourse.viewmodel.FaceViewModel
import com.example.crashcourse.utils.cosineDistance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun DebugScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var faces by remember { mutableStateOf<List<FaceEntity>>(emptyList()) }
    var cacheData by remember { mutableStateOf<List<Pair<String, FloatArray>>>(emptyList()) }
    var loading by remember { mutableStateOf(false) }
    var debugInfo by remember { mutableStateOf("") }

    fun loadDebugData() {
        scope.launch {
            loading = true
            try {
                // Load from database
                val dbFaces = withContext(Dispatchers.IO) {
                    AppDatabase.getInstance(context).faceDao().getAllFaces()
                }
                faces = dbFaces

                // Load from cache
                val cacheList = FaceCache.load(context)
                cacheData = cacheList

                // Generate debug info
                val info = buildString {
                    appendLine("=== DATABASE FACES ===")
                    appendLine("Total faces in DB: ${dbFaces.size}")
                    dbFaces.forEachIndexed { index, face ->
                        appendLine("${index + 1}. ${face.name} (${face.studentId})")
                        appendLine("   Embedding size: ${face.embedding.size}")
                        appendLine("   First 5 values: ${face.embedding.take(5).joinToString(", ")}")
                        appendLine("   Photo URL: ${face.photoUrl ?: "None"}")

                        // Check if photo file exists
                        if (face.photoUrl != null) {
                            val file = java.io.File(face.photoUrl)
                            appendLine("   Photo exists: ${file.exists()}")
                            if (file.exists()) {
                                appendLine("   Photo size: ${file.length()} bytes")
                            }
                        }

                        appendLine("   Class: ${face.className}")
                        appendLine("   Grade: ${face.grade}")
                        appendLine("   Role: ${face.role}")
                        appendLine("   Timestamp: ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(java.util.Date(face.timestamp))}")
                        appendLine()
                    }

                    appendLine("=== CACHE DATA ===")
                    appendLine("Total faces in cache: ${cacheList.size}")
                    cacheList.forEachIndexed { index, (name, embedding) ->
                        appendLine("${index + 1}. $name")
                        appendLine("   Embedding size: ${embedding.size}")
                        appendLine("   First 5 values: ${embedding.take(5).joinToString(", ")}")
                        appendLine()
                    }

                    appendLine("=== EMBEDDING COMPARISON ===")
                    if (dbFaces.size >= 2) {
                        val face1 = dbFaces[0]
                        val face2 = dbFaces[1]
                        val distance = cosineDistance(face1.embedding, face2.embedding)
                        appendLine("Distance between ${face1.name} and ${face2.name}: $distance")
                        appendLine("Registration threshold: 0.2")
                        appendLine("Check-in threshold: 0.4")
                    }
                }
                debugInfo = info

            } catch (e: Exception) {
                debugInfo = "Error loading debug data: ${e.message}"
            } finally {
                loading = false
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Face Recognition Debug",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { loadDebugData() },
                enabled = !loading
            ) {
                if (loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Load Debug Data")
                }
            }

            Button(
                onClick = {
                    scope.launch {
                        FaceCache.clear()
                        loadDebugData()
                    }
                }
            ) {
                Text("Clear Cache & Reload")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                scope.launch {
                    // Test database insert
                    try {
                        val db = AppDatabase.getInstance(context)
                        val testFace = FaceEntity(
                            studentId = "TEST_${System.currentTimeMillis()}",
                            name = "Test User",
                            photoUrl = null,
                            embedding = FloatArray(512) { 0.1f },
                            className = "Test Class",
                            subClass = "Test SubClass",
                            grade = "Test Grade",
                            subGrade = "Test SubGrade",
                            program = "Test Program",
                            role = "Test Role",
                            timestamp = System.currentTimeMillis()
                        )
                        db.faceDao().insert(testFace)
                        debugInfo = "Test face inserted successfully!\nStudentId: ${testFace.studentId}\nName: ${testFace.name}"
                        loadDebugData() // Refresh the data
                    } catch (e: Exception) {
                        debugInfo = "Database test failed: ${e.message}"
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Test Database Insert")
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (debugInfo.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                LazyColumn(
                    modifier = Modifier.padding(16.dp)
                ) {
                    item {
                        Text(
                            text = debugInfo,
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Quick stats
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "${faces.size}",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text("DB Faces")
                }
            }

            Card {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "${cacheData.size}",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text("Cache Faces")
                }
            }
        }
    }
}
