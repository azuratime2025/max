package com.example.crashcourse.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.crashcourse.db.*
import com.example.crashcourse.viewmodel.OptionsViewModel

// Import helper functions
import com.example.crashcourse.ui.OptionsHelpers.getName
import com.example.crashcourse.ui.OptionsHelpers.getOrder
import com.example.crashcourse.ui.OptionsHelpers.getParentId
import com.example.crashcourse.ui.OptionsHelpers.setName
import com.example.crashcourse.ui.OptionsHelpers.setOrder
import com.example.crashcourse.ui.OptionsHelpers.setParentId
import com.example.crashcourse.ui.OptionsHelpers.getId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OptionsManagementScreen(
    viewModel: OptionsViewModel = viewModel(),
    onNavigateToForm: (type: String) -> Unit
) {
    var selectedOptionType by remember { mutableStateOf("Class") }
    var expanded by remember { mutableStateOf(false) }
    val optionTypes = listOf("Class", "SubClass", "Grade", "SubGrade", "Program", "Role")

    // Collect lists
    val classOptions by viewModel.classOptions.collectAsStateWithLifecycle(emptyList())
    val subClassOptions by viewModel.subClassOptions.collectAsStateWithLifecycle(emptyList())
    val gradeOptions by viewModel.gradeOptions.collectAsStateWithLifecycle(emptyList())
    val subGradeOptions by viewModel.subGradeOptions.collectAsStateWithLifecycle(emptyList())
    val programOptions by viewModel.programOptions.collectAsStateWithLifecycle(emptyList())
    val roleOptions by viewModel.roleOptions.collectAsStateWithLifecycle(emptyList())

    val options = when (selectedOptionType) {
        "Class" -> classOptions
        "SubClass" -> subClassOptions
        "Grade" -> gradeOptions
        "SubGrade" -> subGradeOptions
        "Program" -> programOptions
        "Role" -> roleOptions
        else -> emptyList()
    }
    
    val parentOptions = when (selectedOptionType) {
        "SubClass" -> classOptions
        "SubGrade" -> gradeOptions
        else -> emptyList()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manage Options") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // Type selector dropdown
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = selectedOptionType,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Select Option Type") },
                    trailingIcon = {
                        IconButton(onClick = { expanded = !expanded }) {
                            Icon(
                                imageVector = Icons.Filled.ArrowDropDown,
                                contentDescription = "Select option type"
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    optionTypes.forEach { type ->
                        DropdownMenuItem(
                            text = { Text(type) },
                            onClick = {
                                selectedOptionType = type
                                expanded = false
                                onNavigateToForm(type)
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Inline editable list
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(options) { option ->
                    ExpandableOptionCard(
                        option = option,
                        parentOptions = parentOptions,
                        onSave = { updated ->
                            val parentId = when (selectedOptionType) {
                                "SubClass" -> getParentId(updated) ?: 1
                                "SubGrade" -> getParentId(updated) ?: 1
                                else -> null
                            }
                            viewModel.updateOption(
                                selectedOptionType,
                                updated,
                                getName(updated),
                                getOrder(updated),
                                parentId
                            )
                        },
                        onDelete = { viewModel.deleteOption(selectedOptionType, it) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpandableOptionCard(
    option: Any,
    parentOptions: List<Any>,
    onSave: (Any) -> Unit,
    onDelete: (Any) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var dropdownExpanded by remember { mutableStateOf(false) }

    // States for fields
    var name by remember { mutableStateOf(getName(option)) }
    var order by remember { mutableStateOf(getOrder(option).toString()) }
    var parentId by remember { mutableStateOf(getParentId(option) ?: 1) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = { expanded = !expanded }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(getName(option), style = MaterialTheme.typography.titleMedium)
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(
                        imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (expanded) "Collapse" else "Expand"
                    )
                }
            }

            if (expanded) {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = { new -> 
                        name = new
                        onSave(setName(option, new))
                    },
                    label = { Text("Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = order,
                    onValueChange = { new -> 
                        order = new
                        onSave(setOrder(option, new.toIntOrNull() ?: 0))
                    },
                    label = { Text("Order") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (parentOptions.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = parentOptions.find { getId(it) == parentId }?.let { getName(it) } ?: "",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Parent") },
                            trailingIcon = {
                                IconButton(onClick = { dropdownExpanded = !dropdownExpanded }) {
                                    Icon(Icons.Default.ArrowDropDown, "Select parent")
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )

                        DropdownMenu(
                            expanded = dropdownExpanded,
                            onDismissRequest = { dropdownExpanded = false },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            parentOptions.forEach { parent ->
                                DropdownMenuItem(
                                    text = { Text(getName(parent)) },
                                    onClick = {
                                        parentId = getId(parent)
                                        onSave(setParentId(option, parentId))
                                        dropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { onDelete(option) },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete")
                    Spacer(Modifier.width(4.dp))
                    Text("Delete")
                }
            }
        }
    }
}
