package com.example.crashcourse.ui.add

import android.graphics.Bitmap
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.crashcourse.ui.components.FaceViewModel
import com.example.crashcourse.utils.PhotoProcessingUtils
import com.example.crashcourse.utils.PhotoStorageUtils
import com.example.crashcourse.viewmodel.BulkRegistrationViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BulkRegistrationScreen(
    faceViewModel: FaceViewModel,
    bulkViewModel: BulkRegistrationViewModel = viewModel()
) {
    val context = LocalContext.current
    val bulkState by bulkViewModel.state.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    // Single registration state
    var name by remember { mutableStateOf("") }
    var studentId by remember { mutableStateOf("") }
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
    var embedding by remember { mutableStateOf<FloatArray?>(null) }
    var feedback by remember { mutableStateOf<String?>(null) }
    var isProcessing by remember { mutableStateOf(false) }

    // Batch registration state
    var fileUri by remember { mutableStateOf<Uri?>(null) }
    var fileName by remember { mutableStateOf<String?>(null) }

    // Photo selection launcher
    val photoLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            val bmp = PhotoStorageUtils.loadBitmapFromUri(context, it)
            if (bmp != null) {
                coroutineScope.launch {
                    val result = PhotoProcessingUtils.processBitmapForFaceEmbedding(context, bmp)
                    if (result != null) {
                        val (faceBitmap, emb) = result
                        bitmap = faceBitmap
                        embedding = emb
                        feedback = null
                    } else {
                        bitmap = bmp
                        embedding = null
                        feedback = "No face detected. Please try another photo."
                    }
                }
            }
        }
    }

    // File selection launcher
    val fileLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            val contentResolver = context.contentResolver
            val mimeType = contentResolver.getType(it)?.lowercase() ?: ""
            
            var fileName: String? = null
            contentResolver.query(it, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (cursor.moveToFirst() && nameIndex != -1) {
                    fileName = cursor.getString(nameIndex)
                }
            }
            
            val isCsv = when {
                mimeType.contains("csv") -> true
                fileName?.endsWith(".csv", ignoreCase = true) == true -> true
                else -> false
            }
            
            if (isCsv) {
                fileUri = it
                fileName = fileName ?: "selected_file.csv"
                bulkViewModel.resetState()
                bulkViewModel.prepareProcessing(context, it)
            } else {
                feedback = "Unsupported file type. Only CSV files are accepted"
            }
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Bulk Registration") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Single Registration Section
            Text("Single Registration", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(16.dp))
            
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            
            OutlinedTextField(
                value = studentId,
                onValueChange = { studentId = it },
                label = { Text("ID") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))
            
            Button(
                onClick = { photoLauncher.launch("image/*") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Image, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Select Photo")
            }
            Spacer(Modifier.height(12.dp))
            
            bitmap?.let {
                Image(
                    bitmap = it.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier
                        .size(180.dp)
                        .align(Alignment.CenterHorizontally)
                )
                Spacer(Modifier.height(8.dp))
            }
            
            feedback?.let {
                Text(it, color = Color.Red, modifier = Modifier.padding(vertical = 8.dp))
            }
            
            Button(
                onClick = {
                    isProcessing = true
                    if (bitmap != null && embedding != null && name.isNotBlank() && studentId.isNotBlank()) {
                        val photoPath = PhotoStorageUtils.saveFacePhoto(context, bitmap!!, studentId)
                        if (photoPath != null) {
                            faceViewModel.registerFace(
                                studentId = studentId,
                                name = name,
                                embedding = embedding!!,
                                photoUrl = photoPath,
                                onSuccess = { 
                                    feedback = "Registration successful!"
                                    isProcessing = false 
                                },
                                onDuplicate = { 
                                    feedback = "User already registered!"
                                    isProcessing = false 
                                }
                            )
                        } else {
                            feedback = "Failed to save photo."
                            isProcessing = false
                        }
                    } else {
                        feedback = "Please fill all fields and select a valid photo."
                        isProcessing = false
                    }
                },
                enabled = !isProcessing && bitmap != null && embedding != null && 
                          name.isNotBlank() && studentId.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {
                Text("Register Student")
            }
            
            // Divider
            Spacer(modifier = Modifier.height(24.dp))
            Divider()
            Spacer(modifier = Modifier.height(16.dp))
            
            // Batch Registration Section
            Text("Batch Registration", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(12.dp))
            
            OutlinedButton(
                onClick = { fileLauncher.launch("*/*") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !bulkState.isProcessing
            ) {
                Icon(Icons.Default.FolderOpen, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Select CSV File")
            }
            
            fileName?.let { name ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Description, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = name.take(40).let { 
                                if (name.length > 40) "$it..." else it 
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = {
                                fileUri = null
                                fileName = null
                                bulkViewModel.resetState()
                            }
                        ) {
                            Icon(Icons.Default.Close, "Remove file")
                        }
                    }
                }
            }
            
            // Processing info and estimates
            if (fileUri != null) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    if (bulkState.estimatedTime.isNotEmpty()) {
                        Text(
                            text = bulkState.estimatedTime,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    
                    if (bulkState.currentPhotoType.isNotEmpty()) {
                        Text(
                            text = bulkState.currentPhotoType,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    
                    if (bulkState.currentPhotoSize.isNotEmpty()) {
                        Text(
                            text = bulkState.currentPhotoSize,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
            
            // Processing controls
            if (fileUri != null && !bulkState.isProcessing) {
                Button(
                    onClick = { bulkViewModel.processCsvFile(context, fileUri!!, faceViewModel) }, 
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.CloudUpload, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Start Batch Processing")
                }
            }
            
            // Processing status
            if (bulkState.isProcessing) {
                Spacer(modifier = Modifier.height(16.dp))
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        LinearProgressIndicator(
                            progress = bulkState.progress,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = bulkState.status,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
            
            // Results summary
            if (bulkState.results.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Processing Results", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("✅ Successful:")
                            Text("${bulkState.successCount}", fontWeight = FontWeight.Bold)
                        }
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("⚠️ Duplicates:")
                            Text("${bulkState.duplicateCount}", fontWeight = FontWeight.Bold)
                        }
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("❌ Errors:")
                            Text("${bulkState.errorCount}", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            
            // Detailed results
            if (bulkState.results.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text("Details", style = MaterialTheme.typography.titleMedium)
                
                LazyColumn(
                    modifier = Modifier
                        .heightIn(max = 300.dp)
                        .fillMaxWidth()
                ) {
                    items(bulkState.results) { result ->
                        val color = when {
                            result.status == "Registered" -> Color.Green
                            result.status.startsWith("Duplicate") -> Color(0xFFFFA500)
                            else -> Color.Red
                        }
                        
                        val icon = when {
                            result.status == "Registered" -> Icons.Default.Check
                            result.status.startsWith("Duplicate") -> Icons.Default.Warning
                            else -> Icons.Default.Error
                        }
                        
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = color.copy(alpha = 0.1f)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(icon, contentDescription = null, tint = color)
                                Spacer(Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "${result.studentId}: ${result.name}",
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = result.status + (result.error?.let { " - $it" } ?: ""),
                                        color = color
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}