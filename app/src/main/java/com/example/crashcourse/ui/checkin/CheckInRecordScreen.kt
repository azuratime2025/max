package com.example.crashcourse.ui

import android.content.Context
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.ui.platform.LocalContext
import com.example.crashcourse.utils.showToast
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.SelectableDates
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import com.example.crashcourse.viewmodel.CheckInViewModel
import com.example.crashcourse.viewmodel.OptionsViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.crashcourse.db.*

private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckInRecordScreen(
    checkInViewModel: CheckInViewModel = viewModel(),
    optionsViewModel: OptionsViewModel = viewModel()
) {
    val context = LocalContext.current

    // Local filter states
    var nameFilter by remember { mutableStateOf("") }
    var startDate by remember { mutableStateOf<LocalDate?>(null) }
    var endDate by remember { mutableStateOf<LocalDate?>(null) }
    var showFilters by remember { mutableStateOf(false) }
    var selectedClass by remember { mutableStateOf<ClassOption?>(null) }
    var selectedSubClass by remember { mutableStateOf<SubClassOption?>(null) }
    var selectedGrade by remember { mutableStateOf<GradeOption?>(null) }
    var selectedSubGrade by remember { mutableStateOf<SubGradeOption?>(null) }
    var selectedProgram by remember { mutableStateOf<ProgramOption?>(null) }
    var selectedRole by remember { mutableStateOf<RoleOption?>(null) }

    // Collect options for dropdowns
    val classOptions by optionsViewModel.classOptions.collectAsStateWithLifecycle(initialValue = emptyList())
    val subClassOptions by optionsViewModel.subClassOptions.collectAsStateWithLifecycle(initialValue = emptyList())
    val gradeOptions by optionsViewModel.gradeOptions.collectAsStateWithLifecycle(initialValue = emptyList())
    val subGradeOptions by optionsViewModel.subGradeOptions.collectAsStateWithLifecycle(initialValue = emptyList())
    val programOptions by optionsViewModel.programOptions.collectAsStateWithLifecycle(initialValue = emptyList())
    val roleOptions by optionsViewModel.roleOptions.collectAsStateWithLifecycle(initialValue = emptyList())

    var isLoading by remember { mutableStateOf(false) }
    var records by remember { mutableStateOf(emptyList<CheckInRecord>()) }
    val scope = rememberCoroutineScope()

    // Add FaceViewModel for user filtering
    val faceViewModel: com.example.crashcourse.viewmodel.FaceViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    val allFaces by faceViewModel.faceList.collectAsStateWithLifecycle(emptyList())

    // Create a common search function that debounces the search
    val performSearch: () -> Unit = {
        println("Starting search...") // Debug log
        scope.launch {
            if (!isLoading) {
                isLoading = true
                try {
                    println("Search params: name=$nameFilter, start=$startDate, end=$endDate, class=${selectedClass?.id}, subClass=${selectedSubClass?.id}") // Debug log
                    // Search with all filters - use first() to get only the first emission
                    val flow = checkInViewModel.getFilteredCheckIns(
                        nameFilter = nameFilter,
                        startDate = startDate?.format(dateFormatter) ?: "",
                        endDate = endDate?.format(dateFormatter) ?: "",
                        classId = selectedClass?.id,
                        subClassId = selectedSubClass?.id,
                        gradeId = selectedGrade?.id,
                        subGradeId = selectedSubGrade?.id,
                        programId = selectedProgram?.id,
                        roleId = selectedRole?.id
                    )
                    println("Got flow from ViewModel") // Debug log
                    val result = flow.first() // Get only the first emission instead of collecting indefinitely
                    println("Got results from flow") // Debug log
                    records = result
                    println("Search results: ${result.size} records found") // Debug log
                } catch (e: Exception) {
                    println("Search error: ${e.message}")
                    e.printStackTrace() // Print full stack trace for debugging
                } finally {
                    println("Search completed, loading = false") // Debug log
                    isLoading = false
                }
            }
        }
    }

    // Make filters reactive with debounce
    LaunchedEffect(
        nameFilter,
        startDate,
        endDate,
        selectedClass,
        selectedSubClass,
        selectedGrade,
        selectedSubGrade,
        selectedProgram,
        selectedRole
    ) {
        try {
            // Small delay to avoid too frequent searches while typing
            kotlinx.coroutines.delay(500)
            println("Filter changed, triggering search") // Debug log
            performSearch()
        } catch (e: Exception) {
            println("Error in filter effect: ${e.message}")
            e.printStackTrace()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Check-in Records") },
                actions = {
                    IconButton(
                        onClick = {
                            val file = checkInViewModel.exportToPdf(records)
                            context.showToast("PDF exported to ${file.name}")
                        }
                    ) {
                        Icon(Icons.Default.PictureAsPdf, "Export to PDF")
                    }
                    IconButton(
                        onClick = {
                            val file = checkInViewModel.exportToCsv(records)
                            context.showToast("CSV file exported to ${file.name}")
                        }
                    ) {
                        Icon(Icons.Default.TableView, "Export to CSV")
                    }
                    IconButton(onClick = { showFilters = !showFilters }) {
                        Icon(
                            if (showFilters) Icons.Default.FilterList
                            else Icons.Default.FilterListOff,
                            "Toggle Filters"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // Search bar with only one input and a search button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = nameFilter,
                    onValueChange = { nameFilter = it },
                    label = { Text("Search by name") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    trailingIcon = if (nameFilter.isNotEmpty()) {
                        {
                            IconButton(onClick = { nameFilter = "" }) {
                                Icon(Icons.Default.Clear, "Clear search")
                            }
                        }
                    } else null
                )
                Button(
                    onClick = performSearch,
                    modifier = Modifier.height(56.dp),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            // Filters section
            if (showFilters) {
                FilterSection(
                    nameFilter = nameFilter,
                    onNameFilterChange = { value -> nameFilter = value },
                    onSearchClick = performSearch,
                    startDate = startDate,
                    onStartDateChange = { date -> startDate = date },
                    endDate = endDate,
                    onEndDateChange = { date -> endDate = date },
                    classOptions = classOptions,
                    selectedClass = selectedClass,
                    onClassSelected = { option -> selectedClass = option },
                    subClassOptions = subClassOptions.filter { it.parentClassId == selectedClass?.id },
                    selectedSubClass = selectedSubClass,
                    onSubClassSelected = { option -> selectedSubClass = option },
                    gradeOptions = gradeOptions,
                    selectedGrade = selectedGrade,
                    onGradeSelected = { option -> selectedGrade = option },
                    subGradeOptions = subGradeOptions.filter { it.parentGradeId == selectedGrade?.id },
                    selectedSubGrade = selectedSubGrade,
                    onSubGradeSelected = { option -> selectedSubGrade = option },
                    programOptions = programOptions,
                    selectedProgram = selectedProgram,
                    onProgramSelected = { option -> selectedProgram = option },
                    roleOptions = roleOptions,
                    selectedRole = selectedRole,
                    onRoleSelected = { option -> selectedRole = option }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Records list with loading state
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                } else {
                    // Filter out records for deleted users
                    val validNames = allFaces.map { it.name }.toSet()
                    val filteredRecords = records.filter { validNames.contains(it.name) }
                    if (filteredRecords.isEmpty()) {
                        Text(
                            text = "No records found",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(filteredRecords) { record ->
                                CheckInRecordCard(record = record)
                            }
                        }
                    }
                }
            }
        }
    // Initial search effect
    LaunchedEffect(Unit) {
        println("Initial search triggered") // Debug log
        kotlinx.coroutines.delay(500) // Give time for ViewModels to initialize
        performSearch()
    }
}} // End of CheckInRecordScreen

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun FilterSection(
    nameFilter: String,
    onNameFilterChange: (String) -> Unit,
    onSearchClick: () -> Unit,
    startDate: LocalDate?,
    onStartDateChange: (LocalDate?) -> Unit,
    endDate: LocalDate?,
    onEndDateChange: (LocalDate?) -> Unit,
    classOptions: List<ClassOption>,
    selectedClass: ClassOption?,
    onClassSelected: (ClassOption?) -> Unit,
    subClassOptions: List<SubClassOption>,
    selectedSubClass: SubClassOption?,
    onSubClassSelected: (SubClassOption?) -> Unit,
    gradeOptions: List<GradeOption>,
    selectedGrade: GradeOption?,
    onGradeSelected: (GradeOption?) -> Unit,
    subGradeOptions: List<SubGradeOption>,
    selectedSubGrade: SubGradeOption?,
    onSubGradeSelected: (SubGradeOption?) -> Unit,
    programOptions: List<ProgramOption>,
    selectedProgram: ProgramOption?,
    onProgramSelected: (ProgramOption?) -> Unit,
    roleOptions: List<RoleOption>,
    selectedRole: RoleOption?,
    onRoleSelected: (RoleOption?) -> Unit
) {
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

    val startDateState = rememberDatePickerState()
    val endDateState = rememberDatePickerState()

    if (showStartDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showStartDatePicker = false },
            confirmButton = {
                Button(onClick = {
                    startDateState.selectedDateMillis?.let { millis ->
                        val date = Instant.ofEpochMilli(millis)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()
                        onStartDateChange(date)
                    }
                    showStartDatePicker = false
                }) {
                    Text("OK")
                }
            }
        ) {
            DatePicker(
                state = startDateState,
                showModeToggle = false
            )
        }
    }

    if (showEndDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showEndDatePicker = false },
            confirmButton = {
                Button(onClick = {
                    endDateState.selectedDateMillis?.let { millis ->
                        val date = Instant.ofEpochMilli(millis)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()
                        // Only update if date is after start date
                        if (startDate == null || !date.isBefore(startDate)) {
                            onEndDateChange(date)
                        }
                    }
                    showEndDatePicker = false
                }) {
                    Text("OK")
                }
            }
        ) {
            DatePicker(
                state = endDateState,
                showModeToggle = false
            )
        }
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Name filter with search button
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = nameFilter,
                onValueChange = onNameFilterChange,
                label = { Text("Search by name") },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
            Button(
                onClick = onSearchClick,
                modifier = Modifier.align(Alignment.CenterVertically)
            ) {
                Icon(Icons.Default.Search, contentDescription = "Search")
            }
        }

        // Date filters
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedButton(
                onClick = { showStartDatePicker = true },
                modifier = Modifier
                    .weight(1f)
                    .combinedClickable(
                        onClick = { showStartDatePicker = true },
                        onLongClick = { onStartDateChange(null) }
                    )
            ) {
                Text(startDate?.format(dateFormatter) ?: "Start Date")
                if (startDate != null) {
                    Icon(
                        Icons.Default.Clear,
                        contentDescription = "Clear start date",
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
            OutlinedButton(
                onClick = { showEndDatePicker = true },
                modifier = Modifier
                    .weight(1f)
                    .combinedClickable(
                        onClick = { showEndDatePicker = true },
                        onLongClick = { onEndDateChange(null) }
                    )
            ) {
                Text(endDate?.format(dateFormatter) ?: "End Date")
                if (endDate != null) {
                    Icon(
                        Icons.Default.Clear,
                        contentDescription = "Clear end date",
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        }

        // Dropdown filters
        FilterDropdown(
            label = "Class",
            options = classOptions,
            selectedOption = selectedClass,
            onOptionSelected = onClassSelected,
            getOptionLabel = { it.name }
        )

        if (selectedClass != null) {
            FilterDropdown(
                label = "Sub Class",
                options = subClassOptions,
                selectedOption = selectedSubClass,
                onOptionSelected = onSubClassSelected,
                getOptionLabel = { it.name }
            )
        }

        FilterDropdown(
            label = "Grade",
            options = gradeOptions,
            selectedOption = selectedGrade,
            onOptionSelected = onGradeSelected,
            getOptionLabel = { it.name }
        )

        if (selectedGrade != null) {
            FilterDropdown(
                label = "Sub Grade",
                options = subGradeOptions,
                selectedOption = selectedSubGrade,
                onOptionSelected = onSubGradeSelected,
                getOptionLabel = { it.name }
            )
        }

        FilterDropdown(
            label = "Program",
            options = programOptions,
            selectedOption = selectedProgram,
            onOptionSelected = onProgramSelected,
            getOptionLabel = { it.name }
        )

        FilterDropdown(
            label = "Role",
            options = roleOptions,
            selectedOption = selectedRole,
            onOptionSelected = onRoleSelected,
            getOptionLabel = { it.name }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> FilterDropdown(
    label: String,
    options: List<T>,
    selectedOption: T?,
    onOptionSelected: (T?) -> Unit,
    getOptionLabel: (T) -> String
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = selectedOption?.let { getOptionLabel(it) } ?: "",
            onValueChange = {},
            label = { Text(label) },
            readOnly = true,
            trailingIcon = {
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(
                        if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = "Select $label"
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
            DropdownMenuItem(
                text = { Text("All") },
                onClick = {
                    onOptionSelected(null)
                    expanded = false
                }
            )
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(getOptionLabel(option)) },
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun CheckInRecordCard(
    record: CheckInRecord,
    optionsViewModel: OptionsViewModel = viewModel()
) {
    // Collect the current values of all option lists
    val classOptions by optionsViewModel.classOptions.collectAsStateWithLifecycle(initialValue = emptyList())
    val subClassOptions by optionsViewModel.subClassOptions.collectAsStateWithLifecycle(initialValue = emptyList())
    val gradeOptions by optionsViewModel.gradeOptions.collectAsStateWithLifecycle(initialValue = emptyList())
    val subGradeOptions by optionsViewModel.subGradeOptions.collectAsStateWithLifecycle(initialValue = emptyList())
    val programOptions by optionsViewModel.programOptions.collectAsStateWithLifecycle(initialValue = emptyList())
    val roleOptions by optionsViewModel.roleOptions.collectAsStateWithLifecycle(initialValue = emptyList())

    // Find display names from IDs
    val className = remember(record.classId, classOptions) {
        record.classId?.let { id -> classOptions.find { it.id == id }?.name }
    }
    val subClassName = remember(record.subClassId, subClassOptions) {
        record.subClassId?.let { id -> subClassOptions.find { it.id == id }?.name }
    }
    val gradeName = remember(record.gradeId, gradeOptions) {
        record.gradeId?.let { id -> gradeOptions.find { it.id == id }?.name }
    }
    val subGradeName = remember(record.subGradeId, subGradeOptions) {
        record.subGradeId?.let { id -> subGradeOptions.find { it.id == id }?.name }
    }
    val programName = remember(record.programId, programOptions) {
        record.programId?.let { id -> programOptions.find { it.id == id }?.name }
    }
    val roleName = remember(record.roleId, roleOptions) {
        record.roleId?.let { id -> roleOptions.find { it.id == id }?.name }
    }

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Primary Info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = record.name,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(record.timestamp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Student Details in a Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    // Class Information
                    className?.let {
                        Text(
                            text = "Class: $it",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                    subClassName?.let { sub ->
                        Text(
                            text = "Sub-Class: $sub",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    gradeName?.let {
                        Text(
                            text = "Grade: $it",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    // Additional Details
                    subGradeName?.let { sub ->
                        Text(
                            text = "Sub-Grade: $sub",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    programName?.let {
                        Text(
                            text = "Program: $it",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    roleName?.let {
                        Text(
                            text = "Role: $it",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}
