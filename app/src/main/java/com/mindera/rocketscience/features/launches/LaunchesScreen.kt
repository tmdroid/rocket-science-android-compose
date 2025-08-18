package com.mindera.rocketscience.features.launches

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.mindera.rocketscience.domain.model.LaunchStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LaunchesScreen(
    viewModel: LaunchesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("SpaceX") }
            )
        }
    ) { paddingValues ->
        LaunchesContent(
            uiState = uiState,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        )
    }
}

@Composable
private fun LaunchesContent(
    uiState: LaunchesUiState,
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
            LaunchesList(
                launches = uiState.launches,
                modifier = modifier
            )
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
private fun LaunchesList(
    launches: List<LaunchUiModel>,
    modifier: Modifier = Modifier
) {
    if (launches.isEmpty()) {
        Box(
            modifier = modifier,
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No launches available",
                style = MaterialTheme.typography.bodyLarge
            )
        }
    } else {
        Column(modifier = modifier) {
            Text(
                text = "Launches",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
            )
            
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

private fun LaunchStatus.toDisplayText(): String = when (this) {
    is LaunchStatus.DaysSinceLaunch -> "$days days since launch"
    is LaunchStatus.DaysUntilLaunch -> "$days days to launch" 
    LaunchStatus.LaunchingToday -> "Launching today"
}
