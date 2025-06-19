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
import com.usagecontrol.android.ui.navigation.Screen
import com.usagecontrol.android.ui.viewmodels.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("使用制限管理") },
                actions = {
                    IconButton(onClick = { navController.navigate(Screen.Settings.route) }) {
                        Icon(Icons.Default.Settings, contentDescription = "設定")
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = null) },
                    label = { Text("ホーム") },
                    selected = true,
                    onClick = { }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Apps, contentDescription = null) },
                    label = { Text("アプリ制限") },
                    selected = false,
                    onClick = { navController.navigate(Screen.AppRestrictions.route) }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Analytics, contentDescription = null) },
                    label = { Text("使用統計") },
                    selected = false,
                    onClick = { navController.navigate(Screen.UsageStats.route) }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.TrendingUp, contentDescription = null) },
                    label = { Text("レベル") },
                    selected = false,
                    onClick = { navController.navigate(Screen.ProgressiveLevels.route) }
                )
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                CurrentLevelCard(
                    currentLevel = uiState.currentLevel,
                    levelName = uiState.currentLevelName,
                    progress = uiState.levelProgress
                )
            }

            item {
                PermissionsStatusCard(
                    hasUsageStatsPermission = uiState.hasUsageStatsPermission,
                    hasAccessibilityPermission = uiState.hasAccessibilityPermission,
                    hasOverlayPermission = uiState.hasOverlayPermission,
                    onRequestPermissions = { navController.navigate(Screen.Permissions.route) }
                )
            }

            item {
                QuickStatsCard(
                    todayUsage = uiState.todayTotalUsage,
                    blockedAttempts = uiState.todayBlockedAttempts,
                    activeRestrictions = uiState.activeRestrictions
                )
            }

            items(uiState.recentlyUsedApps) { app ->
                RecentAppUsageCard(app = app)
            }
        }
    }
}

@Composable
fun CurrentLevelCard(
    currentLevel: Int,
    levelName: String,
    progress: Float
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "現在のレベル",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "レベル $currentLevel",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = levelName,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${(progress * 100).toInt()}% 完了",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
fun PermissionsStatusCard(
    hasUsageStatsPermission: Boolean,
    hasAccessibilityPermission: Boolean,
    hasOverlayPermission: Boolean,
    onRequestPermissions: () -> Unit
) {
    val allPermissionsGranted = hasUsageStatsPermission && hasAccessibilityPermission && hasOverlayPermission

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (allPermissionsGranted) 
                MaterialTheme.colorScheme.primaryContainer 
            else 
                MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (allPermissionsGranted) Icons.Default.CheckCircle else Icons.Default.Warning,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (allPermissionsGranted) "権限設定完了" else "権限設定が必要",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            if (!allPermissionsGranted) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "アプリが正常に動作するためには、以下の権限が必要です：",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                PermissionStatus("使用統計アクセス", hasUsageStatsPermission)
                PermissionStatus("アクセシビリティサービス", hasAccessibilityPermission)
                PermissionStatus("他のアプリの上に重ねて表示", hasOverlayPermission)
                
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = onRequestPermissions,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("権限を設定する")
                }
            }
        }
    }
}

@Composable
fun PermissionStatus(name: String, isGranted: Boolean) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 2.dp)
    ) {
        Icon(
            imageVector = if (isGranted) Icons.Default.Check else Icons.Default.Close,
            contentDescription = null,
            tint = if (isGranted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = name,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
fun QuickStatsCard(
    todayUsage: Long,
    blockedAttempts: Int,
    activeRestrictions: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "今日の統計",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    label = "使用時間",
                    value = formatDuration(todayUsage),
                    icon = Icons.Default.Timer
                )
                StatItem(
                    label = "ブロック回数",
                    value = "$blockedAttempts",
                    icon = Icons.Default.Block
                )
                StatItem(
                    label = "制限中アプリ",
                    value = "$activeRestrictions",
                    icon = Icons.Default.Apps
                )
            }
        }
    }
}

@Composable
fun StatItem(
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
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
fun RecentAppUsageCard(app: RecentAppUsage) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // App icon would go here
            Icon(
                imageVector = Icons.Default.Apps,
                contentDescription = null,
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = app.appName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = formatDuration(app.usageTime),
                    style = MaterialTheme.typography.bodySmall
                )
            }
            
            if (app.isRestricted) {
                Icon(
                    imageVector = Icons.Default.Block,
                    contentDescription = "制限中",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

data class RecentAppUsage(
    val appName: String,
    val packageName: String,
    val usageTime: Long,
    val isRestricted: Boolean
)

fun formatDuration(milliseconds: Long): String {
    val hours = milliseconds / (1000 * 60 * 60)
    val minutes = (milliseconds % (1000 * 60 * 60)) / (1000 * 60)
    
    return when {
        hours > 0 -> "${hours}時間${minutes}分"
        minutes > 0 -> "${minutes}分"
        else -> "1分未満"
    }
}
