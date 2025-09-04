package com.example.crashcourse.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import android.util.Log
import kotlinx.coroutines.launch
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.crashcourse.ui.components.FaceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExcelImageProcessScreen(
    viewModel: FaceViewModel = viewModel(),
    onNavigateBack: () -> Unit = {}
) {
    val context = LocalContext.current

    // Use our photo processing ViewModel (BulkRegistrationViewModel reference removed as per request)
    // val photoProcessViewModel: BulkRegistrationViewModel = viewModel()
    // References to photoProcessViewModel removed as per request
    // val isProcessing by photoProcessViewModel.isProcessing.collectAsState()
    // val processingProgress by photoProcessViewModel.processingProgress.collectAsState()
    // val processingStatus by photoProcessViewModel.processingStatus.collectAsState()
    // val processResults by photoProcessViewModel.processResults.collectAsState()
    val isProcessing = false
    val processingProgress = 0f
    val processingStatus = ""
    val processResults = null

    // Local state for file selection
    var selectedFileUri by remember { mutableStateOf<Uri?>(null) }
    var selectedFileName by remember { mutableStateOf<String?>(null) }
    var selectedFileType by remember { mutableStateOf<String?>(null) }
    
    // File picker launcher (following existing bulk registration pattern)
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { selectedUri ->
            val fileName = selectedUri.toString()
            val fileExtension = when {
                fileName.contains(".xlsx") -> "xlsx"
                fileName.contains(".xls") -> "xls"
                fileName.contains(".csv") -> "csv"
                else -> "unknown"
            }

            if (fileExtension != "unknown") {
                selectedFileUri = selectedUri
                selectedFileName = fileName.substringAfterLast("/")
                selectedFileType = fileExtension
                // Clear previous results when new file is selected - removed as per request
                // photoProcessViewModel.resetProcessing()
                Log.d("ExcelImageProcess", "File selected: $selectedFileName ($fileExtension)")
            } else {
                Log.w("ExcelImageProcess", "Unsupported file type selected")
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Excel Image Process") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.Description, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Section
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "📊 Bulk Registration System",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Import student data with photos from CSV or Excel files",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
            
            // File Selection Section
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "📁 File Selection",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // File selection button (like bulk registration)
                        OutlinedButton(
                            onClick = { filePickerLauncher.launch("*/*") },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isProcessing
                        ) {
                            Icon(Icons.Default.FolderOpen, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Browse Files")
                        }

                        Text(
                            text = "Supported: CSV (.csv), Excel (.xlsx, .xls)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        // Show selected file info (like bulk registration)
                        selectedFileName?.let { fileName ->
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = when (selectedFileType) {
                                            "csv" -> Icons.Default.Description
                                            else -> Icons.Default.TableChart
                                        },
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Selected: $fileName",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Text(
                                            text = "Type: ${selectedFileType?.uppercase()} file",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    IconButton(
                                        onClick = {
                                            selectedFileUri = null
                                            selectedFileName = null
                                            selectedFileType = null
                                        }
                                    ) {
                                        Icon(Icons.Default.Close, "Remove file")
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            // Processing Section
            if (selectedFileUri != null) {
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "⚙️ Processing",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            if (isProcessing) {
                                LinearProgressIndicator(
                                    progress = { processingProgress },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = processingStatus,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            } else {
                                Button(
                                    onClick = {
                                        // Processing functionality removed as per request
                                        Log.d("ExcelImageProcess", "Processing functionality is currently disabled.")
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.Image, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Start Processing")
                                }
                            }
                        }
                    }
                }
            }
            
            // Results Section - removed as per request due to unresolved references
            /*
            processResults?.let { results ->
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "📊 Results",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            // Success count
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("✅ Successfully registered:")
                                Text(
                                    text = "${results.successCount}",
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            
                            // Duplicate count
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("⚠️ Duplicates found:")
                                Text(
                                    text = "${results.duplicateCount}",
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                            
                            // Error count
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("❌ Errors:")
                                Text(
                                    text = "${results.errorCount}",
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }
            */
            
            // Instructions Section
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "📋 Instructions",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = "CSV Format: studentId,name,className,grade,photoUrl",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Excel Format: Columns with embedded images in photo column",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Photo URLs: Support http://, https://, and data: formats",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

// Helper function to get file name from URI
private fun getFileName(context: android.content.Context, uri: Uri): String {
    return try {
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
            cursor.moveToFirst()
            cursor.getString(nameIndex)
        } ?: "Unknown file"
    } catch (e: Exception) {
        "Unknown file"
    }
}
