package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.foundation.isSystemInDarkTheme
import com.example.ui.theme.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.Worker
import com.example.data.model.LabourLog
import com.example.data.model.Material
import com.example.data.model.StockTransaction
import com.example.ui.SiteViewModel
import java.text.SimpleDateFormat
import java.util.*

enum class DashboardTab {
    LABOUR, STOCK, SYNC
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SiteDashboard(viewModel: SiteViewModel) {
    var activeTab by remember { mutableStateOf(DashboardTab.LABOUR) }
    val context = LocalContext.current
    val isDark = isSystemInDarkTheme()

    // Sync Alerts
    val isSyncing by viewModel.isSyncing.collectAsStateWithLifecycle()
    val syncMessage by viewModel.syncMessage.collectAsStateWithLifecycle()
    val isSyncSuccess by viewModel.isSyncSuccess.collectAsStateWithLifecycle()

    LaunchedEffect(syncMessage, isSyncSuccess) {
        if (syncMessage != null && isSyncSuccess != null) {
            Toast.makeText(context, syncMessage, Toast.LENGTH_LONG).show()
            viewModel.clearSyncStatus()
        }
    }

    Scaffold(
        topBar = {
            SiteHeader(
                isSyncing = isSyncing,
                onSyncClick = { viewModel.syncData() }
            )
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier.testTag("bottom_nav_bar"),
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ) {
                NavigationBarItem(
                    selected = activeTab == DashboardTab.LABOUR,
                    onClick = { activeTab = DashboardTab.LABOUR },
                    icon = { Icon(Icons.Default.People, contentDescription = "Labour Management") },
                    label = { Text("Labour") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = if (isDark) Color.White else PolishSecondary,
                        selectedTextColor = if (isDark) Color.White else PolishSecondary,
                        unselectedIconColor = (if (isDark) Color.White else PolishSecondary).copy(alpha = 0.6f),
                        unselectedTextColor = (if (isDark) Color.White else PolishSecondary).copy(alpha = 0.6f),
                        indicatorColor = if (isDark) PolishAccentBgDark else PolishAccentBg
                    ),
                    modifier = Modifier.testTag("nav_labour")
                )
                NavigationBarItem(
                    selected = activeTab == DashboardTab.STOCK,
                    onClick = { activeTab = DashboardTab.STOCK },
                    icon = { Icon(Icons.Default.Inventory, contentDescription = "Site Stock") },
                    label = { Text("Stock") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = if (isDark) Color.White else PolishSecondary,
                        selectedTextColor = if (isDark) Color.White else PolishSecondary,
                        unselectedIconColor = (if (isDark) Color.White else PolishSecondary).copy(alpha = 0.6f),
                        unselectedTextColor = (if (isDark) Color.White else PolishSecondary).copy(alpha = 0.6f),
                        indicatorColor = if (isDark) PolishAccentBgDark else PolishAccentBg
                    ),
                    modifier = Modifier.testTag("nav_stock")
                )
                NavigationBarItem(
                    selected = activeTab == DashboardTab.SYNC,
                    onClick = { activeTab = DashboardTab.SYNC },
                    icon = { Icon(Icons.Default.CloudSync, contentDescription = "Google Sheets Sync") },
                    label = { Text("Sync Sheets") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = if (isDark) Color.White else PolishSecondary,
                        selectedTextColor = if (isDark) Color.White else PolishSecondary,
                        unselectedIconColor = (if (isDark) Color.White else PolishSecondary).copy(alpha = 0.6f),
                        unselectedTextColor = (if (isDark) Color.White else PolishSecondary).copy(alpha = 0.6f),
                        indicatorColor = if (isDark) PolishAccentBgDark else PolishAccentBg
                    ),
                    modifier = Modifier.testTag("nav_sync")
                )
            }
        },
        contentWindowInsets = WindowInsets.safeDrawing,
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when (activeTab) {
                    DashboardTab.LABOUR -> LabourTab(viewModel)
                    DashboardTab.STOCK -> StockTab(viewModel)
                    DashboardTab.SYNC -> SyncTab(viewModel)
                }
            }
            SyncIndicatorFooter(viewModel = viewModel)
        }
    }
}

@Composable
fun SiteHeader(isSyncing: Boolean, onSyncClick: () -> Unit) {
    val isDark = isSystemInDarkTheme()
    val bgColor = if (isDark) PolishSurfaceDark else Color.White
    val borderCol = if (isDark) PolishOutlineDark.copy(alpha = 0.3f) else Color(0xFFE0E2E5)
    val textCol = if (isDark) Color.White else Color(0xFF1B1B1F)
    val subTextCol = if (isDark) Color(0xFFC4C6D0) else Color(0xFF44474E)
    
    Column(
        modifier = Modifier
            .background(bgColor)
            .padding(top = 24.dp, start = 24.dp, end = 24.dp, bottom = 16.dp)
            .fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Site Monitor",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 24.sp,
                    color = textCol,
                    letterSpacing = (-0.5).sp
                )
            }
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Sync button
                IconButton(
                    onClick = onSyncClick,
                    modifier = Modifier.testTag("top_bar_sync_button")
                ) {
                    if (isSyncing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp,
                            color = if (isDark) PolishPrimaryDark else PolishPrimary
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Sync,
                            contentDescription = "Sync database",
                            tint = if (isDark) PolishPrimaryDark else PolishPrimary
                        )
                    }
                }
                
                // JD Avatar
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isDark) PolishAccentBgDark else PolishAccentBg),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "JD",
                        color = if (isDark) Color.White else PolishSecondary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(6.dp))
        
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFF4CAF50))
            )
            Text(
                text = "Oak Ridge Plaza • Phase 2",
                color = subTextCol,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Thin horizontal separator line
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(borderCol)
        )
    }
}

@Composable
fun SyncIndicatorFooter(viewModel: SiteViewModel) {
    val webAppUrl by viewModel.webAppUrl.collectAsStateWithLifecycle()
    val isDark = isSystemInDarkTheme()
    val bgColor = if (isDark) PolishSurfaceVariantDark else PolishSurfaceVariant
    val textCol = if (isDark) Color(0xFFC4C6D0) else Color(0xFF44474E)
    
    val hasUrl = webAppUrl.isNotBlank()
    val text = if (hasUrl) {
        "CONNECTED TO GOOGLE SHEETS • SYNC READY"
    } else {
        "OFFLINE MODE • CONFIGURE GOOGLE SHEETS"
    }
    val icon = if (hasUrl) Icons.Default.CloudDone else Icons.Default.CloudOff
    val iconColor = if (hasUrl) Color(0xFF4CAF50) else textCol.copy(alpha = 0.5f)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .background(bgColor, RoundedCornerShape(16.dp))
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = textCol,
            letterSpacing = 0.5.sp
        )
    }
}

// ==========================================
// LABOUR TAB
// ==========================================
@Composable
fun LabourTab(viewModel: SiteViewModel) {
    val workers by viewModel.workers.collectAsStateWithLifecycle()
    val activeWorkers by viewModel.activeWorkers.collectAsStateWithLifecycle()
    val allLogs by viewModel.allLabourLogs.collectAsStateWithLifecycle()
    val selectedDate by viewModel.selectedDate.collectAsStateWithLifecycle()
    val isDark = isSystemInDarkTheme()

    var showAddWorkerDialog by remember { mutableStateOf(false) }
    var showRecordLogDialog by remember { mutableStateOf(false) }
    var activeSubTab by remember { mutableStateOf(0) } // 0: Daily Attendance, 1: Worker Directory

    val filteredLogs = remember(allLogs, selectedDate) {
        allLogs.filter { it.date == selectedDate }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Quick statistics for today
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatsCard(
                title = "Checked In Today",
                value = filteredLogs.count { it.status == "Present" || it.status == "Half Day" }.toString(),
                icon = Icons.Default.HowToReg,
                tint = MaterialTheme.colorScheme.primary,
                containerColor = if (isDark) PolishAccentBgDark else PolishAccentBg,
                contentColor = if (isDark) Color.White else PolishSecondary,
                modifier = Modifier.weight(1f)
            )
            StatsCard(
                title = "Today's Wage Est.",
                value = "$${String.format("%.2f", filteredLogs.sumOf { it.amountEarned })}",
                icon = Icons.Default.Payments,
                tint = MaterialTheme.colorScheme.secondary,
                containerColor = if (isDark) PolishSurfaceDark else Color.White,
                contentColor = if (isDark) PolishPrimaryDark else PolishPrimary,
                modifier = Modifier.weight(1f)
            )
        }

        // Sub Tabs
        TabRow(selectedTabIndex = activeSubTab) {
            Tab(
                selected = activeSubTab == 0,
                onClick = { activeSubTab = 0 },
                text = { Text("Daily Attendance", fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = activeSubTab == 1,
                onClick = { activeSubTab = 1 },
                text = { Text("Worker Directory (${workers.size})", fontWeight = FontWeight.Bold) }
            )
        }

        if (activeSubTab == 0) {
            // ATTENDANCE SUB-TAB
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Quick date picker / selector display
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                    shape = RoundedCornerShape(12.dp),
                    onClick = {
                        // For simplicity, we toggle between today and yesterday as fast options, or type custom date
                    }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.CalendarToday, contentDescription = null, size(16.dp))
                        Text(
                            text = "Date: $selectedDate",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                    }
                }

                Button(
                    onClick = { showRecordLogDialog = true },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                    modifier = Modifier.testTag("record_attendance_btn")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text("Check-In")
                }
            }

            if (filteredLogs.isEmpty()) {
                EmptyStateCard(
                    title = "No Attendance Logged Today",
                    description = "Tap 'Check-In' to record attendance, hours worked, and wage rates for workers on site.",
                    icon = Icons.Default.EventNote
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(filteredLogs) { log ->
                        AttendanceLogItem(log = log, onDelete = { viewModel.deleteLabourLog(log.id) })
                    }
                }
            }
        } else {
            // WORKERS DIRECTORY SUB-TAB
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ) {
                Button(
                    onClick = { showAddWorkerDialog = true },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("add_worker_btn")
                ) {
                    Icon(Icons.Default.PersonAdd, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text("New Worker")
                }
            }

            if (workers.isEmpty()) {
                EmptyStateCard(
                    title = "Your Directory is Empty",
                    description = "Register your masonry, helpers, carpentry, and supervisor staff to begin recording attendance.",
                    icon = Icons.Default.GroupAdd
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(workers) { worker ->
                        WorkerDirectoryItem(
                            worker = worker,
                            onActiveToggle = { isActive ->
                                viewModel.updateWorkerActiveStatus(worker, isActive)
                            }
                        )
                    }
                }
            }
        }
    }

    // DIALOGS
    if (showAddWorkerDialog) {
        AddWorkerDialog(
            onDismiss = { showAddWorkerDialog = false },
            onSave = { name, role, rate, phone ->
                viewModel.addWorker(name, role, rate, phone)
                showAddWorkerDialog = false
            }
        )
    }

    if (showRecordLogDialog) {
        RecordAttendanceDialog(
            workers = activeWorkers,
            onDismiss = { showRecordLogDialog = false },
            onSave = { worker, status, hours, payStatus, notes ->
                viewModel.recordLabourAttendance(worker, status, hours, payStatus, notes)
                showRecordLogDialog = false
            }
        )
    }
}

@Composable
fun AttendanceLogItem(log: LabourLog, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = log.workerName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Badge(
                        containerColor = when (log.status) {
                            "Present" -> Color(0xFF4CAF50).copy(alpha = 0.15f)
                            "Half Day" -> Color(0xFFFF9800).copy(alpha = 0.15f)
                            else -> Color(0xFFF44336).copy(alpha = 0.15f)
                        },
                        contentColor = when (log.status) {
                            "Present" -> Color(0xFF388E3C)
                            "Half Day" -> Color(0xFFF57C00)
                            else -> Color(0xFFD32F2F)
                        }
                    ) {
                        Text(log.status, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "${log.hoursWorked} hrs",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Earned: $${String.format("%.2f", log.amountEarned)}",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                    Badge(
                        containerColor = if (log.paymentStatus == "Paid") Color(0xFF4CAF50).copy(alpha = 0.12f) else Color(0xFFFF5722).copy(alpha = 0.12f),
                        contentColor = if (log.paymentStatus == "Paid") Color(0xFF2E7D32) else Color(0xFFD84315)
                    ) {
                        Text(log.paymentStatus, modifier = Modifier.padding(horizontal = 4.dp))
                    }
                }

                if (log.notes.isNotBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Notes: ${log.notes}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (log.synced) {
                    Icon(
                        imageVector = Icons.Default.CloudDone,
                        contentDescription = "Synced with Sheets",
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.CloudQueue,
                        contentDescription = "Unsynced local",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(Modifier.width(8.dp))

                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Delete log",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
fun WorkerDirectoryItem(worker: Worker, onActiveToggle: (Boolean) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = worker.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = if (worker.isActive) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )

                Spacer(Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = worker.role,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        text = "Rate: $${worker.dailyRate}/day",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (worker.phoneNumber.isNotBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                        Text(
                            text = worker.phoneNumber,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = if (worker.isActive) "Active" else "Inactive",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (worker.isActive) Color(0xFF4CAF50) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
                Switch(
                    checked = worker.isActive,
                    onCheckedChange = onActiveToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Color(0xFF4CAF50)
                    )
                )
            }
        }
    }
}

// ==========================================
// STOCK TAB
// ==========================================
@Composable
fun StockTab(viewModel: SiteViewModel) {
    val materials by viewModel.materials.collectAsStateWithLifecycle()
    val transactions by viewModel.stockTransactions.collectAsStateWithLifecycle()
    val isDark = isSystemInDarkTheme()

    var showAddMaterialDialog by remember { mutableStateOf(false) }
    var showAdjustStockDialog by remember { mutableStateOf(false) }
    var selectedMaterialToAdjust by remember { mutableStateOf<Material?>(null) }
    var activeSubTab by remember { mutableStateOf(0) } // 0: Stock Inventory, 1: Ledger History

    val lowStockCount = remember(materials) {
        materials.count { it.currentStock <= it.minimumRequired }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Quick statistics for Today's Stock
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatsCard(
                title = "Total Items",
                value = materials.size.toString(),
                icon = Icons.Default.Category,
                tint = MaterialTheme.colorScheme.primary,
                containerColor = if (isDark) PolishSurfaceDark else Color.White,
                contentColor = if (isDark) PolishPrimaryDark else PolishPrimary,
                modifier = Modifier.weight(1f)
            )
            StatsCard(
                title = "Low Stock Alerts",
                value = lowStockCount.toString(),
                icon = Icons.Default.Warning,
                tint = if (lowStockCount > 0) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                containerColor = if (isDark) PolishAlertBgDark else PolishAlertBg,
                contentColor = if (isDark) PolishAlertTextDark else PolishAlertText,
                modifier = Modifier.weight(1f)
            )
        }

        // Sub Tabs
        TabRow(selectedTabIndex = activeSubTab) {
            Tab(
                selected = activeSubTab == 0,
                onClick = { activeSubTab = 0 },
                text = { Text("Stock Inventory", fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = activeSubTab == 1,
                onClick = { activeSubTab = 1 },
                text = { Text("Ledger History (${transactions.size})", fontWeight = FontWeight.Bold) }
            )
        }

        if (activeSubTab == 0) {
            // STOCK INVENTORY LIST
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ) {
                Button(
                    onClick = { showAddMaterialDialog = true },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("add_material_btn")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text("Add Material")
                }
            }

            if (materials.isEmpty()) {
                EmptyStateCard(
                    title = "No Materials Added",
                    description = "Press 'Add Material' to record construction materials such as Cement, Sand, Steel, and Brick.",
                    icon = Icons.Default.Inventory
                )
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(materials) { mat ->
                        MaterialInventoryCard(
                            material = mat,
                            onAdjustStock = {
                                selectedMaterialToAdjust = mat
                                showAdjustStockDialog = true
                            },
                            onDelete = { viewModel.deleteMaterial(mat.id) }
                        )
                    }
                }
            }
        } else {
            // STOCK TRANSACTION HISTORY (LEDGER)
            if (transactions.isEmpty()) {
                EmptyStateCard(
                    title = "Ledger is Empty",
                    description = "All incoming material additions and project usages will display here.",
                    icon = Icons.Default.ReceiptLong
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(transactions) { tx ->
                        LedgerItem(
                            transaction = tx,
                            onDelete = { viewModel.deleteStockTransaction(tx) }
                        )
                    }
                }
            }
        }
    }

    // DIALOGS
    if (showAddMaterialDialog) {
        AddMaterialDialog(
            onDismiss = { showAddMaterialDialog = false },
            onSave = { name, category, unit, initStock, minStock ->
                viewModel.addMaterial(name, category, unit, initStock, minStock)
                showAddMaterialDialog = false
            }
        )
    }

    if (showAdjustStockDialog && selectedMaterialToAdjust != null) {
        AdjustStockDialog(
            material = selectedMaterialToAdjust!!,
            onDismiss = {
                showAdjustStockDialog = false
                selectedMaterialToAdjust = null
            },
            onSave = { type, qty, ref, user ->
                viewModel.recordStockTransaction(selectedMaterialToAdjust!!, type, qty, ref, user)
                showAdjustStockDialog = false
                selectedMaterialToAdjust = null
            }
        )
    }
}

@Composable
fun MaterialInventoryCard(material: Material, onAdjustStock: () -> Unit, onDelete: () -> Unit) {
    val isLowStock = material.currentStock <= material.minimumRequired

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onAdjustStock() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = if (isLowStock) BorderStroke(1.5.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.8f)) else null
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = material.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = material.category,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Delete material",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Divider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.08f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Current Stock",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${if (material.currentStock % 1 == 0.0) material.currentStock.toInt() else material.currentStock} ${material.unit}",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 20.sp,
                        color = if (isLowStock) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
                    )
                }

                if (isLowStock) {
                    Box(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "LOW",
                            color = MaterialTheme.colorScheme.secondary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    }
                }
            }

            Button(
                onClick = onAdjustStock,
                contentPadding = PaddingValues(0.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(32.dp),
                shape = RoundedCornerShape(6.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.onPrimaryContainer)
            ) {
                Icon(Icons.Default.SwapVert, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("Log In / Out", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun LedgerItem(transaction: StockTransaction, onDelete: () -> Unit) {
    val isIn = transaction.type == "IN"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                // Colored Indicator icon for In vs Out
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            if (isIn) Color(0xFF4CAF50).copy(alpha = 0.12f) else MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(10.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isIn) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                        contentDescription = if (isIn) "Stock Received" else "Stock Used",
                        tint = if (isIn) Color(0xFF388E3C) else MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Column {
                    Text(
                        text = transaction.materialName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = if (isIn) "Added: +${transaction.quantity}" else "Used: -${transaction.quantity}",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = if (isIn) Color(0xFF388E3C) else MaterialTheme.colorScheme.secondary
                        )
                        Text(
                            text = transaction.date,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                    if (transaction.reference.isNotBlank() || transaction.recordedBy.isNotBlank()) {
                        val notes = listOfNotNull(
                            transaction.reference.takeIf { it.isNotBlank() },
                            transaction.recordedBy.takeIf { it.isNotBlank() }?.let { "By: $it" }
                        ).joinToString(" | ")
                        Text(
                            text = notes,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                            overflow = TextOverflow.Ellipsis,
                            maxLines = 1
                        )
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (transaction.synced) {
                    Icon(
                        imageVector = Icons.Default.CloudDone,
                        contentDescription = "Synced",
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.CloudQueue,
                        contentDescription = "Unsynced",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(Modifier.width(8.dp))

                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Delete transaction",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

// ==========================================
// SYNC TAB
// ==========================================
@Composable
fun SyncTab(viewModel: SiteViewModel) {
    val webAppUrl by viewModel.webAppUrl.collectAsStateWithLifecycle()
    val isSyncing by viewModel.isSyncing.collectAsStateWithLifecycle()
    val allLogs by viewModel.allLabourLogs.collectAsStateWithLifecycle()
    val allTxs by viewModel.stockTransactions.collectAsStateWithLifecycle()

    val unsyncedLogsCount = remember(allLogs) { allLogs.count { !it.synced } }
    val unsyncedTxsCount = remember(allTxs) { allTxs.count { !it.synced } }

    var urlInput by remember { mutableStateOf(webAppUrl) }
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    val appsScriptCode = """
function doPost(e) {
  try {
    var requestData = JSON.parse(e.postData.contents);
    var action = requestData.action;
    var ss = SpreadsheetApp.getActiveSpreadsheet();
    
    if (action === "sync_labour") {
      var sheet = getOrCreateSheet(ss, "Labour Logs");
      if (sheet.getLastRow() === 0) {
        sheet.appendRow(["Log ID", "Worker ID", "Worker Name", "Date", "Status", "Hours Worked", "Amount Earned", "Payment Status", "Notes"]);
      }
      var logs = requestData.data;
      for (var i = 0; i < logs.length; i++) {
        var log = logs[i];
        sheet.appendRow([
          log.id,
          log.workerId,
          log.workerName,
          log.date,
          log.status,
          log.hoursWorked,
          log.amountEarned,
          log.paymentStatus,
          log.notes
        ]);
      }
      return ContentService.createTextOutput(JSON.stringify({ success: true, message: "Synced " + logs.length + " labour logs" }))
        .setMimeType(ContentService.MimeType.JSON);
    }
    
    if (action === "sync_stock") {
      var sheet = getOrCreateSheet(ss, "Stock Transactions");
      if (sheet.getLastRow() === 0) {
        sheet.appendRow(["Transaction ID", "Material ID", "Material Name", "Type", "Quantity", "Date", "Reference", "Recorded By"]);
      }
      var txs = requestData.data;
      for (var i = 0; i < txs.length; i++) {
        var tx = txs[i];
        sheet.appendRow([
          tx.id,
          tx.materialId,
          tx.materialName,
          tx.type,
          tx.quantity,
          tx.date,
          tx.reference,
          tx.recordedBy
        ]);
      }
      return ContentService.createTextOutput(JSON.stringify({ success: true, message: "Synced " + txs.length + " stock transactions" }))
        .setMimeType(ContentService.MimeType.JSON);
    }
    
    return ContentService.createTextOutput(JSON.stringify({ success: false, error: "Unknown action" }))
      .setMimeType(ContentService.MimeType.JSON);
      
  } catch (err) {
    return ContentService.createTextOutput(JSON.stringify({ success: false, error: err.toString() }))
      .setMimeType(ContentService.MimeType.JSON);
  }
}

function getOrCreateSheet(ss, name) {
  var sheet = ss.getSheetByName(name);
  if (!sheet) {
    sheet = ss.insertSheet(name);
  }
  return sheet;
}
""".trimIndent()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Google Sheets Sync",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Secure your offline construction logs. Instantly backup and stream real-time attendance and inventory balances directly into your corporate Google Sheets.",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        // Status Card
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Pending Local Sync Queue",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.People, contentDescription = null, size(16.dp), tint = MaterialTheme.colorScheme.primary)
                            Text("Unsynced Attendance Logs:", fontSize = 13.sp)
                        }
                        Text("$unsyncedLogsCount logs", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.Inventory, contentDescription = null, size(16.dp), tint = MaterialTheme.colorScheme.primary)
                            Text("Unsynced Stock Ledger:", fontSize = 13.sp)
                        }
                        Text("$unsyncedTxsCount entries", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }

                    Button(
                        onClick = { viewModel.syncData() },
                        enabled = !isSyncing,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("sync_now_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        if (isSyncing) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                        } else {
                            Icon(Icons.Default.Sync, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Sync All Logs Now")
                        }
                    }
                }
            }
        }

        // Configure URL Card
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Connection Configurations",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )

                    OutlinedTextField(
                        value = urlInput,
                        onValueChange = { urlInput = it },
                        label = { Text("Google Apps Script Web App URL") },
                        placeholder = { Text("https://script.google.com/macros/s/.../exec") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("web_app_url_input"),
                        trailingIcon = {
                            if (urlInput.isNotBlank()) {
                                IconButton(onClick = { urlInput = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                                }
                            }
                        }
                    )

                    Button(
                        onClick = {
                            viewModel.setWebAppUrl(urlInput)
                            Toast.makeText(context, "URL Configuration Saved!", Toast.LENGTH_SHORT).show()
                        },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .align(Alignment.End)
                            .testTag("save_url_button")
                    ) {
                        Text("Save Configuration")
                    }
                }
            }
        }

        // Setup Instructions
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                        Text(
                            text = "Step-By-Step Setup Instructions",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Text("We use Google Apps Script for secure, hassle-free communication. Set it up in 1 minute:", fontSize = 13.sp)

                    Divider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.08f))

                    Text("1. Create a Google Sheet and name it any name.", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text("2. Click Extensions > Apps Script in the menu bar.", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text("3. Copy our optimized deployment script below:", fontSize = 12.sp, fontWeight = FontWeight.Bold)

                    Button(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(appsScriptCode))
                            Toast.makeText(context, "Apps Script copied to clipboard!", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().testTag("copy_script_button")
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Copy Script To Clipboard")
                    }

                    Text("4. Paste the code into the script editor, replacing any existing code.", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text("5. Click Deploy > New Deployment. Choose Web App, change 'Who has access' to 'Anyone', then deploy and copy your Web App URL here!", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ==========================================
// SHARED STYLED WIDGETS
// ==========================================
@Composable
fun StatsCard(
    title: String, 
    value: String, 
    icon: androidx.compose.ui.graphics.vector.ImageVector, 
    tint: Color, 
    modifier: Modifier = Modifier,
    containerColor: Color? = null,
    contentColor: Color? = null
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = containerColor ?: MaterialTheme.colorScheme.surface,
            contentColor = contentColor ?: MaterialTheme.colorScheme.onSurface
        )
    ) {
        Row(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title, 
                    fontSize = 13.sp, 
                    fontWeight = FontWeight.Medium, 
                    color = contentColor?.copy(alpha = 0.8f) ?: MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = value, 
                    fontSize = 28.sp, 
                    fontWeight = FontWeight.Bold, 
                    color = contentColor ?: MaterialTheme.colorScheme.primary
                )
            }
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background((contentColor ?: tint).copy(alpha = 0.12f), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon, 
                    contentDescription = null, 
                    tint = contentColor ?: tint, 
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

@Composable
fun EmptyStateCard(title: String, description: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(32.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f), RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
            }

            Text(
                text = title,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                text = description,
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
            )
        }
    }
}

// ==========================================
// DIALOG IMPLEMENTATIONS
// ==========================================

@Composable
fun AddWorkerDialog(onDismiss: () -> Unit, onSave: (String, String, Double, String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("Mason") }
    var rateInput by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }

    val roles = listOf("Mason", "Helper", "Carpenter", "Bar Bender", "Plumber", "Electrician", "Supervisor")

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Register New Labour", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.primary)

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Worker Full Name *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("worker_name_input")
                )

                // Role Dropdown Selection
                var expanded by remember { mutableStateOf(false) }
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = role,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Labour Role / Craft *") },
                        trailingIcon = {
                            IconButton(onClick = { expanded = true }) {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                            }
                        },
                        modifier = Modifier.fillMaxWidth().clickable { expanded = true }.testTag("worker_role_input")
                    )
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        roles.forEach { r ->
                            DropdownMenuItem(
                                text = { Text(r) },
                                onClick = {
                                    role = r
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = rateInput,
                    onValueChange = { rateInput = it },
                    label = { Text("Daily Wage Rate ($) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("worker_rate_input")
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone Number") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("worker_phone_input")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val rate = rateInput.toDoubleOrNull()
                            if (name.isNotBlank() && rate != null) {
                                onSave(name, role, rate, phone)
                            }
                        },
                        enabled = name.isNotBlank() && rateInput.toDoubleOrNull() != null,
                        modifier = Modifier.testTag("save_worker_dialog_btn")
                    ) {
                        Text("Add Worker")
                    }
                }
            }
        }
    }
}

@Composable
fun RecordAttendanceDialog(workers: List<Worker>, onDismiss: () -> Unit, onSave: (Worker, String, Double, String, String) -> Unit) {
    if (workers.isEmpty()) {
        Dialog(onDismissRequest = onDismiss) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("No Active Workers", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("Please register active workers in the 'Worker Directory' first.", textAlign = TextAlign.Center)
                    Button(onClick = onDismiss) { Text("OK") }
                }
            }
        }
        return
    }

    var selectedWorkerIndex by remember { mutableIntStateOf(0) }
    var attendanceStatus by remember { mutableStateOf("Present") }
    var hoursWorkedInput by remember { mutableStateOf("8.0") }
    var paymentStatus by remember { mutableStateOf("Unpaid") }
    var notes by remember { mutableStateOf("") }

    var expandedWorkerDropdown by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Log Daily Attendance", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.primary)

                // Worker Select
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = workers[selectedWorkerIndex].name,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Select Worker") },
                        trailingIcon = {
                            IconButton(onClick = { expandedWorkerDropdown = true }) {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                            }
                        },
                        modifier = Modifier.fillMaxWidth().clickable { expandedWorkerDropdown = true }.testTag("dialog_select_worker")
                    )
                    DropdownMenu(
                        expanded = expandedWorkerDropdown,
                        onDismissRequest = { expandedWorkerDropdown = false }
                    ) {
                        workers.forEachIndexed { idx, worker ->
                            DropdownMenuItem(
                                text = { Text("${worker.name} (${worker.role})") },
                                onClick = {
                                    selectedWorkerIndex = idx
                                    expandedWorkerDropdown = false
                                }
                            )
                        }
                    }
                }

                // Attendance status segment
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Attendance Status", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Present", "Half Day", "Absent").forEach { status ->
                            val selected = attendanceStatus == status
                            Button(
                                onClick = {
                                    attendanceStatus = status
                                    hoursWorkedInput = when (status) {
                                        "Present" -> "8.0"
                                        "Half Day" -> "4.0"
                                        else -> "0.0"
                                    }
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(status, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Hours input
                OutlinedTextField(
                    value = hoursWorkedInput,
                    onValueChange = { hoursWorkedInput = it },
                    label = { Text("Hours Worked") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("dialog_hours_input")
                )

                // Payment Status
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Payout Settlement State", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Unpaid", "Paid").forEach { pay ->
                            val selected = paymentStatus == pay
                            Button(
                                onClick = { paymentStatus = pay },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(pay, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Work Notes / Assigned Task") },
                    placeholder = { Text("e.g. brick layering on 2nd floor slab") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("dialog_notes_input")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val hours = hoursWorkedInput.toDoubleOrNull() ?: 0.0
                            onSave(workers[selectedWorkerIndex], attendanceStatus, hours, paymentStatus, notes)
                        },
                        modifier = Modifier.testTag("save_attendance_dialog_btn")
                    ) {
                        Text("Log Check-In")
                    }
                }
            }
        }
    }
}

@Composable
fun AddMaterialDialog(onDismiss: () -> Unit, onSave: (String, String, String, Double, Double) -> Unit) {
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Structural") }
    var unit by remember { mutableStateOf("Bags") }
    var initStockInput by remember { mutableStateOf("") }
    var minStockInput by remember { mutableStateOf("") }

    val categories = listOf("Structural", "Finishing", "Plumbing", "Electrical", "Safety Gear", "Aggregates")
    val units = listOf("Bags", "Tons", "Cum", "Pieces", "Liters", "Meters")

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Register Site Material", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.primary)

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Material Name *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("mat_name_input")
                )

                // Category dropdown
                var expCat by remember { mutableStateOf(false) }
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Material Category") },
                        trailingIcon = {
                            IconButton(onClick = { expCat = true }) {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                            }
                        },
                        modifier = Modifier.fillMaxWidth().clickable { expCat = true }.testTag("mat_category_input")
                    )
                    DropdownMenu(expanded = expCat, onDismissRequest = { expCat = false }) {
                        categories.forEach { c ->
                            DropdownMenuItem(text = { Text(c) }, onClick = { category = c; expCat = false })
                        }
                    }
                }

                // Unit dropdown
                var expUnit by remember { mutableStateOf(false) }
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = unit,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Measurement Unit") },
                        trailingIcon = {
                            IconButton(onClick = { expUnit = true }) {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                            }
                        },
                        modifier = Modifier.fillMaxWidth().clickable { expUnit = true }.testTag("mat_unit_input")
                    )
                    DropdownMenu(expanded = expUnit, onDismissRequest = { expUnit = false }) {
                        units.forEach { u ->
                            DropdownMenuItem(text = { Text(u) }, onClick = { unit = u; expUnit = false })
                        }
                    }
                }

                OutlinedTextField(
                    value = initStockInput,
                    onValueChange = { initStockInput = it },
                    label = { Text("Current Initial Stock *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("mat_stock_input")
                )

                OutlinedTextField(
                    value = minStockInput,
                    onValueChange = { minStockInput = it },
                    label = { Text("Alert Safety Limit *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("mat_min_input")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val initStock = initStockInput.toDoubleOrNull() ?: 0.0
                            val minStock = minStockInput.toDoubleOrNull() ?: 0.0
                            if (name.isNotBlank()) {
                                onSave(name, category, unit, initStock, minStock)
                            }
                        },
                        enabled = name.isNotBlank() && initStockInput.toDoubleOrNull() != null && minStockInput.toDoubleOrNull() != null,
                        modifier = Modifier.testTag("save_material_dialog_btn")
                    ) {
                        Text("Save Material")
                    }
                }
            }
        }
    }
}

@Composable
fun AdjustStockDialog(material: Material, onDismiss: () -> Unit, onSave: (String, Double, String, String) -> Unit) {
    var type by remember { mutableStateOf("OUT") } // IN: Stock Delivery, OUT: Used on site
    var quantityInput by remember { mutableStateOf("") }
    var reference by remember { mutableStateOf("") }
    var recordedBy by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Record Stock Transaction",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "${material.name} (Current: ${material.currentStock} ${material.unit})",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.secondary
                )

                // Select transaction type IN or OUT
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { type = "IN" },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (type == "IN") Color(0xFF4CAF50) else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (type == "IN") Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("STOCK IN (Delivery)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = { type = "OUT" },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (type == "OUT") MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (type == "OUT") Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("STOCK OUT (Usage)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                OutlinedTextField(
                    value = quantityInput,
                    onValueChange = { quantityInput = it },
                    label = { Text("Quantity (${material.unit}) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("dialog_qty_input")
                )

                OutlinedTextField(
                    value = reference,
                    onValueChange = { reference = it },
                    label = { Text("Reference / Invoice / Area Used") },
                    placeholder = { Text(if (type == "IN") "e.g. Invoice #2938 from Supplier" else "e.g. Ground Floor Slab Casting") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("dialog_ref_input")
                )

                OutlinedTextField(
                    value = recordedBy,
                    onValueChange = { recordedBy = it },
                    label = { Text("Recorded By (Supervisor Name)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("dialog_recorded_by_input")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val qty = quantityInput.toDoubleOrNull()
                            if (qty != null && qty > 0.0) {
                                onSave(type, qty, reference, recordedBy)
                            }
                        },
                        enabled = quantityInput.toDoubleOrNull() != null && (quantityInput.toDoubleOrNull() ?: 0.0) > 0.0,
                        modifier = Modifier.testTag("save_adjust_dialog_btn")
                    ) {
                        Text("Record Entry")
                    }
                }
            }
        }
    }
}

// Utility extension for custom sizing in clean Compose
private fun size(dp: androidx.compose.ui.unit.Dp): Modifier = Modifier.size(dp)
