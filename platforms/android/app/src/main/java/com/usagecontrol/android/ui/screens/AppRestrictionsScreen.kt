package com.usagecontrol.android.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.usagecontrol.android.data.entities.AppRestriction
import com.usagecontrol.android.ui.viewmodels.AppRestrictionsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppRestrictionsScreen(
    navController: NavController,
    viewModel: AppRestrictionsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("アプリ制限") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "戻る")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.refreshInstalledApps() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "更新")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.addAllInstalledApps() }
            ) {
                Icon(Icons.Default.Add, contentDescription = "アプリを追加")
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                SummaryCard(
                    totalApps = uiState.restrictions.size,
                    blockedApps = uiState.restrictions.count { it.isBlocked },
                    restrictedApps = uiState.restrictions.count { it.dailyTimeLimit > 0 }
                )
            }

            items(uiState.restrictions) { restriction ->
                AppRestrictionCard(
                    restriction = restriction,
                    onToggleBlocked = { viewModel.toggleAppBlocked(restriction.packageName) },
                    onUpdateTimeLimit = { timeLimit -> 
                        viewModel.updateTimeLimit(restriction.packageName, timeLimit) 
                    }
                )
            }
        }
    }
}

@Composable
fun SummaryCard(
    totalApps: Int,
    blockedApps: Int,
    restrictedApps: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "制限概要",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                SummaryItem(
                    label = "総アプリ数",
                    value = totalApps.toString(),
                    icon = Icons.Default.Apps
                )
                SummaryItem(
                    label = "ブロック中",
                    value = blockedApps.toString(),
                    icon = Icons.Default.Block
                )
                SummaryItem(
                    label = "時間制限",
                    value = restrictedApps.toString(),
                    icon = Icons.Default.Timer
                )
            }
        }
    }
}

@Composable
fun SummaryItem(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppRestrictionCard(
    restriction: AppRestriction,
    onToggleBlocked: () -> Unit,
    onUpdateTimeLimit: (Long) -> Unit
) {
    var showTimeLimitDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // App icon placeholder
                Icon(
                    Icons.Default.Apps,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp)
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = restriction.appName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = restriction.packageName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    if (restriction.dailyTimeLimit > 0) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "制限: ${formatDuration(restriction.dailyTimeLimit)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "使用済み: ${formatDuration(restriction.usedTimeToday)}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
                
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Switch(
                        checked = restriction.isBlocked,
                        onCheckedChange = { onToggleBlocked() }
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    OutlinedButton(
                        onClick = { showTimeLimitDialog = true },
                        modifier = Modifier.size(width = 80.dp, height = 32.dp),
                        contentPadding = PaddingValues(4.dp)
                    ) {
                        Text(
                            text = "時間",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
            
            if (restriction.usedTimeToday > 0 && restriction.dailyTimeLimit > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = (restriction.usedTimeToday.toFloat() / restriction.dailyTimeLimit).coerceAtMost(1f),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }

    if (showTimeLimitDialog) {
        TimeLimitDialog(
            currentLimit = restriction.dailyTimeLimit,
            onConfirm = { newLimit ->
                onUpdateTimeLimit(newLimit)
                showTimeLimitDialog = false
            },
            onDismiss = { showTimeLimitDialog = false }
        )
    }
}

@Composable
fun TimeLimitDialog(
    currentLimit: Long,
    onConfirm: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    var hours by remember { mutableStateOf((currentLimit / (1000 * 60 * 60)).toInt()) }
    var minutes by remember { mutableStateOf(((currentLimit % (1000 * 60 * 60)) / (1000 * 60)).toInt()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("時間制限を設定") },
        text = {
            Column {
                Text("1日の使用時間上限を設定してください")
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("時間: ")
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    OutlinedTextField(
                        value = hours.toString(),
                        onValueChange = { 
                            hours = it.toIntOrNull()?.coerceIn(0, 23) ?: 0
                        },
                        modifier = Modifier.width(80.dp),
                        singleLine = true
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("時間")
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    OutlinedTextField(
                        value = minutes.toString(),
                        onValueChange = { 
                            minutes = it.toIntOrNull()?.coerceIn(0, 59) ?: 0
                        },
                        modifier = Modifier.width(80.dp),
                        singleLine = true
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("分")
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val limitInMillis = (hours * 60 * 60 + minutes * 60) * 1000L
                    onConfirm(limitInMillis)
                }
            ) {
                Text("設定")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("キャンセル")
            }
        }
    )
}
