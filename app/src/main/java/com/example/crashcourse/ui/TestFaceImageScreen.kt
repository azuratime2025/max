package com.example.crashcourse.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.example.crashcourse.utils.FaceImageTester
import kotlinx.coroutines.launch
import java.io.File

/**
 * TestFaceImageScreen - Screen for testing face image saving functionality
 * 
 * This screen provides a UI for testing all aspects of the face image saving system
 */

@Composable
fun TestFaceImageScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var testResults by remember { mutableStateOf("Click 'Run Tests' to start...") }
    var isRunning by remember { mutableStateOf(false) }
    var studentId by remember { mutableStateOf("123456") }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Face Image System Tester",
            style = MaterialTheme.typography.headlineMedium
        )
        
        // Quick test section
        Card {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Quick Test",
                    style = MaterialTheme.typography.titleMedium
                )
                
                OutlinedTextField(
                    value = studentId,
                    onValueChange = { studentId = it },
                    label = { Text("Student ID") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            scope.launch {
                                isRunning = true
                                val result = FaceImageTester.quickTest(context, studentId)
                                testResults = "Quick Test Result:\n$result"
                                isRunning = false
                            }
                        },
                        enabled = !isRunning,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Check File")
                    }
                    
                    Button(
                        onClick = {
                            scope.launch {
                                isRunning = true
                                val result = FaceImageTester.createTestFaceImage(context, studentId)
                                testResults = "Create Test Image Result:\n$result"
                                isRunning = false
                            }
                        },
                        enabled = !isRunning,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Create Test")
                    }
                }
            }
        }
        
        // Full test section
        Card {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Comprehensive Tests",
                    style = MaterialTheme.typography.titleMedium
                )
                
                Button(
                    onClick = {
                        scope.launch {
                            isRunning = true
                            testResults = "Running comprehensive tests...\n"
                            val results = FaceImageTester.runAllTests(context)
                            testResults = results
                            isRunning = false
                        }
                    },
                    enabled = !isRunning,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (isRunning) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(Modifier.width(8.dp))
                    }
                    Text("Run All Tests")
                }
            }
        }
        
        // Manual test section
        Card {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Manual Test (Your Code)",
                    style = MaterialTheme.typography.titleMedium
                )
                
                Button(
                    onClick = {
                        scope.launch {
                            isRunning = true
                            
                            // Your original test code
                            val testFile = File(context.filesDir, "faces/face_123456.jpg")
                            val exists = testFile.exists()
                            val path = testFile.absolutePath
                            val parentExists = testFile.parentFile?.exists() ?: false
                            
                            android.util.Log.d("Test", "File exists: $exists")
                            
                            testResults = """
                                Manual Test Results:
                                File exists: $exists
                                File path: $path
                                Parent folder exists: $parentExists
                                
                                File details:
                                - Name: ${testFile.name}
                                - Size: ${if (exists) testFile.length() else "N/A"} bytes
                                - Can read: ${if (exists) testFile.canRead() else "N/A"}
                                - Last modified: ${if (exists) java.util.Date(testFile.lastModified()) else "N/A"}
                            """.trimIndent()
                            
                            isRunning = false
                        }
                    },
                    enabled = !isRunning,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Run Manual Test")
                }
            }
        }
        
        // Results section
        Card {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Test Results",
                    style = MaterialTheme.typography.titleMedium
                )
                
                Spacer(Modifier.height(8.dp))
                
                Text(
                    text = testResults,
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 100.dp, max = 400.dp)
                )
            }
        }
        
        // Info section
        Card {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "System Info",
                    style = MaterialTheme.typography.titleMedium
                )
                
                Spacer(Modifier.height(8.dp))
                
                val filesDir = context.filesDir.absolutePath
                val facesDir = File(context.filesDir, "faces").absolutePath
                
                Text(
                    text = """
                        App Files Directory:
                        $filesDir
                        
                        Faces Directory:
                        $facesDir
                        
                        Expected file format:
                        face_{studentId}.jpg
                        
                        Example:
                        $facesDir/face_123456.jpg
                    """.trimIndent(),
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

/**
 * Usage in MainActivity or Navigation:
 * 
 * // Add to your navigation or test it directly
 * TestFaceImageScreen()
 */
