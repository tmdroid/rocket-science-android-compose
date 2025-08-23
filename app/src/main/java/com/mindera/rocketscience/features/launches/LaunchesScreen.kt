package com.mindera.rocketscience.features.launches

import android.content.Context
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.core.net.toUri
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
    val context = LocalContext.current
    var showLinkChoiceDialog by remember { mutableStateOf(false) }
    var selectedLaunch by remember { mutableStateOf<LaunchUiModel?>(null) }
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
                        onItemClick = { launchItem ->
                            if (launchItem.videoUrl != null && launchItem.wikipediaUrl != null) {
                                selectedLaunch = launchItem
                                showLinkChoiceDialog = true
                            } else {
                                openLaunchDetails(context, launchItem)
                            }
                        },
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

    // Link choice dialog
    if (showLinkChoiceDialog && selectedLaunch != null) {
        LinkChoiceDialog(
            launch = selectedLaunch!!,
            onVideoSelected = { launch ->
                openLaunchUrl(context, launch.videoUrl!!)
                showLinkChoiceDialog = false
                selectedLaunch = null
            },
            onWikipediaSelected = { launch ->
                openLaunchUrl(context, launch.wikipediaUrl!!)
                showLinkChoiceDialog = false
                selectedLaunch = null
            },
            onDismiss = {
                showLinkChoiceDialog = false
                selectedLaunch = null
            }
        )
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
                    LaunchItem(
                        launch = launch,
                        onItemClick = { /* This function is not used anymore */ }
                    )
                }
            }
        }
    }
}

@Composable
private fun LaunchItem(
    launch: LaunchUiModel,
    onItemClick: (LaunchUiModel) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onItemClick(launch) },
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

@Composable
private fun LinkChoiceDialog(
    launch: LaunchUiModel,
    onVideoSelected: (LaunchUiModel) -> Unit,
    onWikipediaSelected: (LaunchUiModel) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Open Launch Details",
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Text(
                text = "Choose how you'd like to learn more about ${launch.name}:",
                style = MaterialTheme.typography.bodyMedium
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { onVideoSelected(launch) }) { Text("Watch Video") }
                Button(onClick = { onWikipediaSelected(launch) }) { Text("Read Article") }
            }
        }
    )
}

private fun openLaunchDetails(context: Context, launch: LaunchUiModel) {
    val url = launch.videoUrl ?: launch.wikipediaUrl
    if (url != null) {
        openLaunchUrl(context, url)
    }
}

private fun openLaunchUrl(context: Context, url: String) {
    val customTabsIntent = CustomTabsIntent.Builder()
        .setShowTitle(true)
        .setUrlBarHidingEnabled(true)
        .build()

    try {
        customTabsIntent.launchUrl(context, url.toUri())
    } catch (e: Exception) {
        // Fallback to regular browser if Chrome Custom Tabs fails
        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, url.toUri())
        context.startActivity(intent)
    }
}
