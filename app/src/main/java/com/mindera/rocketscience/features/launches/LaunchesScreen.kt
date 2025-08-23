package com.mindera.rocketscience.features.launches

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.mindera.rocketscience.R
import com.mindera.rocketscience.domain.model.LaunchStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LaunchesScreen(
    viewModel: LaunchesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    var showFilterDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("SpaceX") },
                scrollBehavior = scrollBehavior,
                actions = {
                    IconButton(onClick = { showFilterDialog = true }) {
                        Icon(
                            painter = painterResource(R.drawable.baseline_filter_list_alt_24),
                            contentDescription = "Filter launches",
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        LaunchesContent(
            uiState = uiState,
            scrollBehavior = scrollBehavior,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        )

        if (showFilterDialog) {
            FilterDialog(
                filterState = uiState.filterState,
                availableYears = uiState.availableYears,
                onFilterApplied = { newFilterState ->
                    viewModel.applyFilter(newFilterState)
                    showFilterDialog = false
                },
                onDismiss = { showFilterDialog = false }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LaunchesContent(
    uiState: LaunchesUiState,
    scrollBehavior: TopAppBarScrollBehavior,
    modifier: Modifier = Modifier
) {
    when {
        uiState.isLoading -> {
            LoadingContent(modifier = modifier)
        }

        uiState.error != null -> {
            ErrorContent(
                error = uiState.error,
                modifier = modifier
            )
        }

        else -> {
            LazyColumn(
                modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                // Company section
                uiState.company?.let { company ->
                    item {
                        CompanyInfo(company = company)
                    }
                }

                // Launches section header
                item {
                    Text(
                        text = "Launches",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
                    )
                }

                // Launches items
                items(uiState.launches) { launch ->
                    LaunchItem(
                        launch = launch,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                    )
                }

                // Empty state
                if (uiState.launches.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No launches available",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LoadingContent(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorContent(
    error: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Error: $error",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error
        )
    }
}

@Composable
private fun CompanyInfo(
    company: CompanyUiModel,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "Company",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
        )

        Text(
            text = "${company.name} was founded by ${company.founder} in ${company.founded}. " +
                    "It has now ${company.employees} employees, ${company.launchSites} launch sites, " +
                    "and is valued at USD ${company.formattedValuation}.",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}

@Composable
private fun LaunchesList(
    launches: List<LaunchUiModel>,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "Launches",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
        )

        if (launches.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No launches available",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(launches) { launch ->
                    LaunchItem(launch = launch)
                }
            }
        }
    }
}

@Composable
private fun LaunchItem(
    launch: LaunchUiModel,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Mission patch image
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(launch.missionPatchUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = "Mission patch for ${launch.name}",
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(16.dp))

            // Mission information
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = launch.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = launch.dateTime,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = launch.rocketInfo,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = launch.launchStatus.toDisplayText(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Success/failure icon
            launch.success?.let { success ->
                Icon(
                    imageVector = if (success) Icons.Default.Check else Icons.Default.Close,
                    contentDescription = if (success) "Mission successful" else "Mission failed",
                    tint = if (success) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.error
                    },
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterDialog(
    filterState: LaunchFilterState,
    availableYears: List<String>,
    onFilterApplied: (LaunchFilterState) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedYear by remember(filterState) { mutableStateOf(filterState.selectedYear) }
    var launchSuccess by remember(filterState) { mutableStateOf(filterState.launchSuccess) }
    var sortOrder by remember(filterState) { mutableStateOf(filterState.sortOrder) }
    var yearDropdownExpanded by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Text(
                    text = "Filter Launches",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Year filter
                Text(
                    text = "Launch Year",
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                ExposedDropdownMenuBox(
                    expanded = yearDropdownExpanded,
                    onExpandedChange = { yearDropdownExpanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedYear ?: "All Years",
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = yearDropdownExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )

                    ExposedDropdownMenu(
                        expanded = yearDropdownExpanded,
                        onDismissRequest = { yearDropdownExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("All Years") },
                            onClick = {
                                selectedYear = null
                                yearDropdownExpanded = false
                            }
                        )
                        availableYears.forEach { year ->
                            DropdownMenuItem(
                                text = { Text(year) },
                                onClick = {
                                    selectedYear = year
                                    yearDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Launch success filter
                Text(
                    text = "Launch Success",
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Column {
                    LaunchSuccessFilter.entries.forEach { filter ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = launchSuccess == filter,
                                    onClick = { launchSuccess = filter },
                                    role = Role.RadioButton
                                )
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = launchSuccess == filter,
                                onClick = null
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = when (filter) {
                                    LaunchSuccessFilter.ALL -> "All Launches"
                                    LaunchSuccessFilter.SUCCESS_ONLY -> "Successful Only"
                                    LaunchSuccessFilter.FAILED_ONLY -> "Failed Only"
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Sort order
                Text(
                    text = "Sort Order",
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Row {
                    SortOrder.entries.forEach { order ->
                        Row(
                            modifier = Modifier
                                .selectable(
                                    selected = sortOrder == order,
                                    onClick = { sortOrder = order },
                                    role = Role.RadioButton
                                )
                                .padding(end = 16.dp, top = 8.dp, bottom = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = sortOrder == order,
                                onClick = null
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = when (order) {
                                    SortOrder.ASC -> "Oldest First"
                                    SortOrder.DESC -> "Newest First"
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            onFilterApplied(
                                LaunchFilterState(
                                    selectedYear = selectedYear,
                                    launchSuccess = launchSuccess,
                                    sortOrder = sortOrder
                                )
                            )
                        }
                    ) {
                        Text("Apply")
                    }
                }
            }
        }
    }
}

private fun LaunchStatus.toDisplayText(): String = when (this) {
    is LaunchStatus.DaysSinceLaunch -> "$days days since launch"
    is LaunchStatus.DaysUntilLaunch -> "$days days to launch"
    LaunchStatus.LaunchingToday -> "Launching today"
}
