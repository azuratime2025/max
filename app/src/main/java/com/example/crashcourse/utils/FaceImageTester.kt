package com.example.crashcourse.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import androidx.core.graphics.createBitmap
import android.util.Log
import java.io.File

/**
 * FaceImageTester - Utility for testing face image saving functionality
 * 
 * This class provides comprehensive testing for the face image saving system
 * including file creation, path validation, and storage verification.
 */
object FaceImageTester {
    
    private const val TAG = "FaceImageTester"
    
    /**
     * Run comprehensive tests for face image saving
     * 
     * @param context Android context
     * @return Test results summary
     */
    fun runAllTests(context: Context): String {
        val results = mutableListOf<String>()
        
        Log.d(TAG, "=== Starting Face Image Tests ===")
        results.add("=== Face Image Tests ===")
        
        // Test 1: Check faces folder
        val folderTest = testFacesFolder(context)
        results.add("1. Faces Folder: $folderTest")
        
        // Test 2: Test file creation
        val fileTest = testFileCreation(context)
        results.add("2. File Creation: $fileTest")
        
        // Test 3: Test image saving
        val imageTest = testImageSaving(context)
        results.add("3. Image Saving: $imageTest")
        
        // Test 4: Test specific file check
        val specificTest = testSpecificFile(context, "123456")
        results.add("4. Specific File (123456): $specificTest")
        
        // Test 5: List all face files
        val listTest = listAllFaceFiles(context)
        results.add("5. All Face Files: $listTest")
        
        Log.d(TAG, "=== Tests Complete ===")
        
        return results.joinToString("\n")
    }
    
    /**
     * Test faces folder creation and access
     */
    private fun testFacesFolder(context: Context): String {
        return try {
            val facesDir = File(context.filesDir, "faces")
            val exists = facesDir.exists()
            val canCreate = if (!exists) facesDir.mkdirs() else true
            val canWrite = facesDir.canWrite()
            val path = facesDir.absolutePath
            
            Log.d(TAG, "Faces folder - Exists: $exists, Created: $canCreate, Writable: $canWrite")
            Log.d(TAG, "Faces folder path: $path")
            
            "✅ EXISTS: $exists, WRITABLE: $canWrite, PATH: $path"
        } catch (e: Exception) {
            Log.e(TAG, "Faces folder test failed", e)
            "❌ ERROR: ${e.message}"
        }
    }
    
    /**
     * Test basic file creation
     */
    private fun testFileCreation(context: Context): String {
        return try {
            val testFile = File(context.filesDir, "faces/test_file.txt")
            testFile.parentFile?.mkdirs()
            
            val created = testFile.createNewFile()
            val exists = testFile.exists()
            val size = testFile.length()
            
            // Clean up
            testFile.delete()
            
            Log.d(TAG, "File creation - Created: $created, Exists: $exists, Size: $size")
            
            "✅ CREATED: $created, EXISTS: $exists"
        } catch (e: Exception) {
            Log.e(TAG, "File creation test failed", e)
            "❌ ERROR: ${e.message}"
        }
    }
    
    /**
     * Test actual image saving
     */
    private fun testImageSaving(context: Context): String {
        return try {
            // Create a test bitmap
            val testBitmap = createTestBitmap()
            val testStudentId = "TEST_${System.currentTimeMillis()}"
            
            // Save using our function
            val savedPath = FaceImageSaver.saveFaceImageWithStudentId(context, testBitmap, testStudentId)
            
            // Verify the file
            val savedFile = File(savedPath)
            val exists = savedFile.exists()
            val size = savedFile.length()
            
            Log.d(TAG, "Image saving - Path: $savedPath, Exists: $exists, Size: $size bytes")
            
            // Clean up test file
            savedFile.delete()
            
            "✅ SAVED: $savedPath, SIZE: $size bytes"
        } catch (e: Exception) {
            Log.e(TAG, "Image saving test failed", e)
            "❌ ERROR: ${e.message}"
        }
    }
    
    /**
     * Test specific file check (like your example)
     */
    private fun testSpecificFile(context: Context, studentId: String): String {
        return try {
            val testFile = File(context.filesDir, "faces/face_$studentId.jpg")
            val exists = testFile.exists()
            val path = testFile.absolutePath
            val parentExists = testFile.parentFile?.exists() ?: false
            
            Log.d(TAG, "Specific file test - StudentID: $studentId")
            Log.d(TAG, "File path: $path")
            Log.d(TAG, "File exists: $exists")
            Log.d(TAG, "Parent exists: $parentExists")
            
            if (!exists && parentExists) {
                // Try to create a test file
                val testBitmap = createTestBitmap()
                val savedPath = FaceImageSaver.saveFaceImageWithStudentId(context, testBitmap, studentId)
                val nowExists = File(savedPath).exists()
                
                "✅ CREATED TEST FILE: $nowExists, PATH: $savedPath"
            } else {
                "✅ EXISTS: $exists, PATH: $path"
            }
        } catch (e: Exception) {
            Log.e(TAG, "Specific file test failed", e)
            "❌ ERROR: ${e.message}"
        }
    }
    
    /**
     * List all face files in the directory
     */
    private fun listAllFaceFiles(context: Context): String {
        return try {
            val facesDir = File(context.filesDir, "faces")
            
            if (!facesDir.exists()) {
                return "❌ FACES FOLDER DOESN'T EXIST"
            }
            
            val files = facesDir.listFiles()
            val faceFiles = files?.filter { it.name.startsWith("face_") && it.name.endsWith(".jpg") }
            
            Log.d(TAG, "Found ${faceFiles?.size ?: 0} face files")
            faceFiles?.forEach { file ->
                Log.d(TAG, "Face file: ${file.name}, Size: ${file.length()} bytes")
            }
            
            val fileNames = faceFiles?.map { it.name }?.joinToString(", ") ?: "None"
            
            "✅ COUNT: ${faceFiles?.size ?: 0}, FILES: $fileNames"
        } catch (e: Exception) {
            Log.e(TAG, "List files test failed", e)
            "❌ ERROR: ${e.message}"
        }
    }
    
    /**
     * Create a test bitmap for testing
     */
    private fun createTestBitmap(): Bitmap {
        val bitmap = createBitmap(100, 100)
        val canvas = Canvas(bitmap)
        val paint = Paint().apply {
            color = Color.BLUE
            textSize = 20f
        }
        
        canvas.drawColor(Color.WHITE)
        canvas.drawText("TEST", 20f, 50f, paint)
        
        return bitmap
    }
    
    /**
     * Quick test function (like your example)
     */
    fun quickTest(context: Context, studentId: String = "123456"): String {
        val testFile = File(context.filesDir, "faces/face_$studentId.jpg")
        val exists = testFile.exists()
        val path = testFile.absolutePath
        val parentExists = testFile.parentFile?.exists() ?: false
        
        Log.d("Test", "File exists: $exists")
        Log.d("Test", "File path: $path")
        Log.d("Test", "Parent folder exists: $parentExists")
        
        return "File exists: $exists, Path: $path"
    }
    
    /**
     * Create test face image for a specific student ID
     */
    fun createTestFaceImage(context: Context, studentId: String): String {
        return try {
            val testBitmap = createTestBitmap()
            val savedPath = FaceImageSaver.saveFaceImageWithStudentId(context, testBitmap, studentId)
            
            Log.d(TAG, "Created test face image for $studentId at: $savedPath")
            
            "✅ Created: $savedPath"
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create test face image", e)
            "❌ Error: ${e.message}"
        }
    }
}

/**
 * Usage Examples:
 * 
 * // Quick test (like your example)
 * FaceImageTester.quickTest(context, "123456")
 * 
 * // Run all tests
 * val results = FaceImageTester.runAllTests(context)
 * Log.d("TestResults", results)
 * 
 * // Create a test image
 * FaceImageTester.createTestFaceImage(context, "123456")
 */
