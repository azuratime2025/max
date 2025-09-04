package com.example.crashcourse.ui

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.crashcourse.db.ClassOption
import com.example.crashcourse.db.GradeOption
import com.example.crashcourse.db.ProgramOption
import com.example.crashcourse.db.RoleOption
import com.example.crashcourse.db.SubClassOption
import com.example.crashcourse.db.SubGradeOption
import kotlinx.coroutines.launch

@Composable
fun RegistrationForm(
    name: String,
    onNameChange: (String) -> Unit,
    studentId: String,
    onStudentIdChange: (String) -> Unit,
    showAdvancedOptions: Boolean,
    onShowAdvancedOptionsChange: (Boolean) -> Unit,
    className: String,
    onClassNameChange: (String) -> Unit,
    subClass: String,
    onSubClassChange: (String) -> Unit,
    grade: String,
    onGradeChange: (String) -> Unit,
    subGrade: String,
    onSubGradeChange: (String) -> Unit,
    program: String,
    onProgramChange: (String) -> Unit,
    role: String,
    onRoleChange: (String) -> Unit,
    classExpanded: Boolean,
    onClassExpandedChange: (Boolean) -> Unit,
    subClassExpanded: Boolean,
    onSubClassExpandedChange: (Boolean) -> Unit,
    gradeExpanded: Boolean,
    onGradeExpandedChange: (Boolean) -> Unit,
    subGradeExpanded: Boolean,
    onSubGradeExpandedChange: (Boolean) -> Unit,
    programExpanded: Boolean,
    onProgramExpandedChange: (Boolean) -> Unit,
    roleExpanded: Boolean,
    onRoleExpandedChange: (Boolean) -> Unit,
    selectedClassId: Int?,
    onSelectedClassIdChange: (Int?) -> Unit,
    selectedSubClassId: Int?,
    onSelectedSubClassIdChange: (Int?) -> Unit,
    selectedGradeId: Int?,
    onSelectedGradeIdChange: (Int?) -> Unit,
    selectedSubGradeId: Int?,
    onSelectedSubGradeIdChange: (Int?) -> Unit,
    selectedProgramId: Int?,
    onSelectedProgramIdChange: (Int?) -> Unit,
    selectedRoleId: Int?,
    onSelectedRoleIdChange: (Int?) -> Unit,
    classOptions: List<ClassOption>,
    subClassOptions: List<SubClassOption>,
    gradeOptions: List<GradeOption>,
    subGradeOptions: List<SubGradeOption>,
    programOptions: List<ProgramOption>,
    roleOptions: List<RoleOption>,
    snackbarHostState: SnackbarHostState,
    capturedBitmap: Bitmap?,
    embedding: FloatArray?,
    isSubmitting: Boolean,
    onCapturePhoto: () -> Unit,
    onSubmit: () -> Unit
) {
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Required fields
        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            label = { Text("Name *") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = studentId,
            onValueChange = onStudentIdChange,
            label = { Text("Student ID") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            placeholder = { Text("Auto-generated if empty") }
        )

        // Face capture section
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Face Photo",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                if (capturedBitmap != null) {
                    // Show captured photo
                    Image(
                        bitmap = capturedBitmap.asImageBitmap(),
                        contentDescription = "Captured face",
                        modifier = Modifier
                            .size(120.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "✅ Face captured successfully!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Retake button
                    OutlinedButton(
                        onClick = onCapturePhoto,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Retake Photo")
                    }
                } else {
                    // Capture button
                    Button(
                        onClick = onCapturePhoto,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("📷 Capture Face Photo")
                    }
                    
                    Text(
                        text = "Take a photo with green face detection boxes",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }

        // Toggle for advanced options
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Additional Information",
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(Modifier.weight(1f))

            Switch(
                checked = showAdvancedOptions,
                onCheckedChange = onShowAdvancedOptionsChange
            )
        }

        // Advanced options
        if (showAdvancedOptions) {
            AdvancedOptionsSection(
                className = className,
                onClassNameChange = onClassNameChange,
                subClass = subClass,
                onSubClassChange = onSubClassChange,
                grade = grade,
                onGradeChange = onGradeChange,
                subGrade = subGrade,
                onSubGradeChange = onSubGradeChange,
                program = program,
                onProgramChange = onProgramChange,
                role = role,
                onRoleChange = onRoleChange,
                classExpanded = classExpanded,
                onClassExpandedChange = onClassExpandedChange,
                subClassExpanded = subClassExpanded,
                onSubClassExpandedChange = onSubClassExpandedChange,
                gradeExpanded = gradeExpanded,
                onGradeExpandedChange = onGradeExpandedChange,
                subGradeExpanded = subGradeExpanded,
                onSubGradeExpandedChange = onSubGradeExpandedChange,
                programExpanded = programExpanded,
                onProgramExpandedChange = onProgramExpandedChange,
                roleExpanded = roleExpanded,
                onRoleExpandedChange = onRoleExpandedChange,
                selectedClassId = selectedClassId,
                onSelectedClassIdChange = onSelectedClassIdChange,
                selectedSubClassId = selectedSubClassId,
                onSelectedSubClassIdChange = onSelectedSubClassIdChange,
                selectedGradeId = selectedGradeId,
                onSelectedGradeIdChange = onSelectedGradeIdChange,
                selectedSubGradeId = selectedSubGradeId,
                onSelectedSubGradeIdChange = onSelectedSubGradeIdChange,
                selectedProgramId = selectedProgramId,
                onSelectedProgramIdChange = onSelectedProgramIdChange,
                selectedRoleId = selectedRoleId,
                onSelectedRoleIdChange = onSelectedRoleIdChange,
                classOptions = classOptions,
                subClassOptions = subClassOptions,
                gradeOptions = gradeOptions,
                subGradeOptions = subGradeOptions,
                programOptions = programOptions,
                roleOptions = roleOptions,
                snackbarHostState = snackbarHostState
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Submit button
        Button(
            onClick = onSubmit,
            enabled = name.isNotBlank() && embedding != null && capturedBitmap != null && !isSubmitting,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            if (isSubmitting) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Registering...")
            } else {
                Icon(
                    imageVector = Icons.Default.PersonAdd,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (embedding != null && capturedBitmap != null) "✅ Register User" else "❌ Capture Face First",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }

        // Helper text
        if (embedding == null || capturedBitmap == null) {
            Text(
                text = "Please capture a face photo before registering",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun AdvancedOptionsSection(
    className: String,
    onClassNameChange: (String) -> Unit,
    subClass: String,
    onSubClassChange: (String) -> Unit,
    grade: String,
    onGradeChange: (String) -> Unit,
    subGrade: String,
    onSubGradeChange: (String) -> Unit,
    program: String,
    onProgramChange: (String) -> Unit,
    role: String,
    onRoleChange: (String) -> Unit,
    classExpanded: Boolean,
    onClassExpandedChange: (Boolean) -> Unit,
    subClassExpanded: Boolean,
    onSubClassExpandedChange: (Boolean) -> Unit,
    gradeExpanded: Boolean,
    onGradeExpandedChange: (Boolean) -> Unit,
    subGradeExpanded: Boolean,
    onSubGradeExpandedChange: (Boolean) -> Unit,
    programExpanded: Boolean,
    onProgramExpandedChange: (Boolean) -> Unit,
    roleExpanded: Boolean,
    onRoleExpandedChange: (Boolean) -> Unit,
    selectedClassId: Int?,
    onSelectedClassIdChange: (Int?) -> Unit,
    selectedSubClassId: Int?,
    onSelectedSubClassIdChange: (Int?) -> Unit,
    selectedGradeId: Int?,
    onSelectedGradeIdChange: (Int?) -> Unit,
    selectedSubGradeId: Int?,
    onSelectedSubGradeIdChange: (Int?) -> Unit,
    selectedProgramId: Int?,
    onSelectedProgramIdChange: (Int?) -> Unit,
    selectedRoleId: Int?,
    onSelectedRoleIdChange: (Int?) -> Unit,
    classOptions: List<ClassOption>,
    subClassOptions: List<SubClassOption>,
    gradeOptions: List<GradeOption>,
    subGradeOptions: List<SubGradeOption>,
    programOptions: List<ProgramOption>,
    roleOptions: List<RoleOption>,
    snackbarHostState: SnackbarHostState
) {
    val scope = rememberCoroutineScope()

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Class dropdown
        OutlinedTextField(
            value = className,
            onValueChange = { /* Read-only field */ },
            label = { Text("Class") },
            modifier = Modifier.fillMaxWidth(),
            readOnly = true,
            trailingIcon = {
                Row {
                    IconButton(onClick = { onClassExpandedChange(true) }) {
                        Icon(
                            imageVector = Icons.Filled.ArrowDropDown,
                            contentDescription = "Dropdown"
                        )
                    }
                    IconButton(onClick = {
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                message = "Class data has been updated!",
                                duration = SnackbarDuration.Short
                            )
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = "Update Class"
                        )
                    }
                }
            }
        )
        DropdownMenu(
            expanded = classExpanded,
            onDismissRequest = { onClassExpandedChange(false) },
            modifier = Modifier.fillMaxWidth(0.9f)
        ) {
            classOptions.forEach { option: ClassOption ->
                DropdownMenuItem(
                    text = { Text(option.name) },
                    onClick = {
                        onSelectedClassIdChange(option.id)
                        onClassNameChange(option.name)
                        onClassExpandedChange(false)

                        // Reset dependent fields
                        onSelectedSubClassIdChange(null)
                        onSubClassChange("")
                    }
                )
            }
        }

        // Sub-Class dropdown
        OutlinedTextField(
            value = subClass,
            onValueChange = { /* Read-only field */ },
            label = { Text("Sub-Class") },
            modifier = Modifier.fillMaxWidth(),
            readOnly = true,
            enabled = selectedClassId != null,
            trailingIcon = {
                Row {
                    IconButton(onClick = { if (selectedClassId != null) onSubClassExpandedChange(true) }) {
                        Icon(
                            imageVector = Icons.Filled.ArrowDropDown,
                            contentDescription = "Dropdown"
                        )
                    }
                    IconButton(onClick = {
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                message = "Sub-Class data has been updated!",
                                duration = SnackbarDuration.Short
                            )
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = "Update Sub-Class"
                        )
                    }
                }
            }
        )
        DropdownMenu(
            expanded = subClassExpanded,
            onDismissRequest = { onSubClassExpandedChange(false) },
            modifier = Modifier.fillMaxWidth(0.9f)
        ) {
            subClassOptions.forEach { option: SubClassOption ->
                DropdownMenuItem(
                    text = { Text(option.name) },
                    onClick = {
                        onSelectedSubClassIdChange(option.id)
                        onSubClassChange(option.name)
                        onSubClassExpandedChange(false)
                    }
                )
            }
        }

        // Grade dropdown
        OutlinedTextField(
            value = grade,
            onValueChange = { /* Read-only field */ },
            label = { Text("Grade") },
            modifier = Modifier.fillMaxWidth(),
            readOnly = true,
            trailingIcon = {
                Row {
                    IconButton(onClick = { onGradeExpandedChange(true) }) {
                        Icon(
                            imageVector = Icons.Filled.ArrowDropDown,
                            contentDescription = "Dropdown"
                        )
                    }
                    IconButton(onClick = {
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                message = "Grade data has been updated!",
                                duration = SnackbarDuration.Short
                            )
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = "Update Grade"
                        )
                    }
                }
            }
        )
        DropdownMenu(
            expanded = gradeExpanded,
            onDismissRequest = { onGradeExpandedChange(false) },
            modifier = Modifier.fillMaxWidth(0.9f)
        ) {
            gradeOptions.forEach { option: GradeOption ->
                DropdownMenuItem(
                    text = { Text(option.name) },
                    onClick = {
                        onSelectedGradeIdChange(option.id)
                        onGradeChange(option.name)
                        onGradeExpandedChange(false)

                        // Reset dependent fields
                        onSelectedSubGradeIdChange(null)
                        onSubGradeChange("")
                    }
                )
            }
        }

        // Sub-Grade dropdown
        OutlinedTextField(
            value = subGrade,
            onValueChange = { /* Read-only field */ },
            label = { Text("Sub-Grade") },
            modifier = Modifier.fillMaxWidth(),
            readOnly = true,
            enabled = selectedGradeId != null,
            trailingIcon = {
                Row {
                    IconButton(onClick = { if (selectedGradeId != null) onSubGradeExpandedChange(true) }) {
                        Icon(
                            imageVector = Icons.Filled.ArrowDropDown,
                            contentDescription = "Dropdown"
                        )
                    }
                    IconButton(onClick = {
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                message = "Sub-Grade data has been updated!",
                                duration = SnackbarDuration.Short
                            )
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = "Update Sub-Grade"
                        )
                    }
                }
            }
        )
        DropdownMenu(
            expanded = subGradeExpanded,
            onDismissRequest = { onSubGradeExpandedChange(false) },
            modifier = Modifier.fillMaxWidth(0.9f)
        ) {
            subGradeOptions.forEach { option: SubGradeOption ->
                DropdownMenuItem(
                    text = { Text(option.name) },
                    onClick = {
                        onSelectedSubGradeIdChange(option.id)
                        onSubGradeChange(option.name)
                        onSubGradeExpandedChange(false)
                    }
                )
            }
        }

        // Program dropdown
        OutlinedTextField(
            value = program,
            onValueChange = { /* Read-only field */ },
            label = { Text("Program") },
            modifier = Modifier.fillMaxWidth(),
            readOnly = true,
            trailingIcon = {
                Row {
                    IconButton(onClick = { onProgramExpandedChange(true) }) {
                        Icon(
                            imageVector = Icons.Filled.ArrowDropDown,
                            contentDescription = "Dropdown"
                        )
                    }
                    IconButton(onClick = {
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                message = "Program data has been updated!",
                                duration = SnackbarDuration.Short
                            )
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = "Update Program"
                        )
                    }
                }
            }
        )
        DropdownMenu(
            expanded = programExpanded,
            onDismissRequest = { onProgramExpandedChange(false) },
            modifier = Modifier.fillMaxWidth(0.9f)
        ) {
            programOptions.forEach { option: ProgramOption ->
                DropdownMenuItem(
                    text = { Text(option.name) },
                    onClick = {
                        onSelectedProgramIdChange(option.id)
                        onProgramChange(option.name)
                        onProgramExpandedChange(false)
                    }
                )
            }
        }

        // Role dropdown
        OutlinedTextField(
            value = role,
            onValueChange = { /* Read-only field */ },
            label = { Text("Role") },
            modifier = Modifier.fillMaxWidth(),
            readOnly = true,
            trailingIcon = {
                Row {
                    IconButton(onClick = { onRoleExpandedChange(true) }) {
                        Icon(
                            imageVector = Icons.Filled.ArrowDropDown,
                            contentDescription = "Dropdown"
                        )
                    }
                    IconButton(onClick = {
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                message = "Role data has been updated!",
                                duration = SnackbarDuration.Short
                            )
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = "Update Role"
                        )
                    }
                }
            }
        )
        DropdownMenu(
            expanded = roleExpanded,
            onDismissRequest = { onRoleExpandedChange(false) },
            modifier = Modifier.fillMaxWidth(0.9f)
        ) {
            roleOptions.forEach { option: RoleOption ->
                DropdownMenuItem(
                    text = { Text(option.name) },
                    onClick = {
                        onSelectedRoleIdChange(option.id)
                        onRoleChange(option.name)
                        onRoleExpandedChange(false)
                    }
                )
            }
        }
    }
}
