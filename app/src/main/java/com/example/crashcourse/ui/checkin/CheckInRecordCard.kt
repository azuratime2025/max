package com.example.crashcourse.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.crashcourse.viewmodel.OptionsViewModel
import com.example.crashcourse.db.*
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckInRecordCard(record: CheckInRecord) {
    val optionsViewModel: OptionsViewModel = viewModel()
    
    // Collect options from Flows
    val subClassOptions = optionsViewModel.subClassOptions.collectAsStateWithLifecycle(
        initialValue = emptyList<SubClassOption>()
    ).value
    val subGradeOptions = optionsViewModel.subGradeOptions.collectAsStateWithLifecycle(
        initialValue = emptyList<SubGradeOption>()
    ).value
    val programOptions = optionsViewModel.programOptions.collectAsStateWithLifecycle(
        initialValue = emptyList<ProgramOption>()
    ).value
    val roleOptions = optionsViewModel.roleOptions.collectAsStateWithLifecycle(
        initialValue = emptyList<RoleOption>()
    ).value
    
    // Derive option names from IDs
    val subClassName = remember(record.subClassId, subClassOptions) {
        subClassOptions.find { option -> option.id == record.subClassId }?.name
    }
    val subGradeName = remember(record.subGradeId, subGradeOptions) {
        subGradeOptions.find { option -> option.id == record.subGradeId }?.name
    }
    val programName = remember(record.programId, programOptions) {
        programOptions.find { option -> option.id == record.programId }?.name
    }
    val roleName = remember(record.roleId, roleOptions) {
        roleOptions.find { option -> option.id == record.roleId }?.name
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = record.name,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = record.timestamp.format(DateTimeFormatter.ofPattern("HH:mm:ss")),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Details
            Text(
                text = "Date: ${record.timestamp.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))}",
                style = MaterialTheme.typography.bodyMedium
            )
            
            // Primary details
            if (record.className != null) {
                Text(
                    text = "Class: ${record.className}",
                    style = MaterialTheme.typography.bodyMedium
                )
                if (subClassName != null) {
                    Text(
                        text = "Sub-class: $subClassName",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            
            if (record.gradeName != null) {
                Text(
                    text = "Grade: ${record.gradeName}",
                    style = MaterialTheme.typography.bodyMedium
                )
                if (subGradeName != null) {
                    Text(
                        text = "Sub-grade: $subGradeName",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            
            // Additional details
            if (programName != null) {
                Text(
                    text = "Program: $programName",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            if (roleName != null) {
                Text(
                    text = "Role: $roleName",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
