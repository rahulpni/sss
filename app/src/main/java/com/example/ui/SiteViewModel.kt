package com.example.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.Worker
import com.example.data.model.LabourLog
import com.example.data.model.Material
import com.example.data.model.StockTransaction
import com.example.data.repository.SiteRepository
import com.example.data.sync.GoogleSheetsSyncManager
import com.example.data.sync.SyncResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SiteViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val repository = SiteRepository(database.labourDao(), database.stockDao())
    private val syncManager = GoogleSheetsSyncManager(repository, application)

    private val sharedPrefs = application.getSharedPreferences("site_sync_prefs", Context.MODE_PRIVATE)

    // Sync state
    private val _webAppUrl = MutableStateFlow(sharedPrefs.getString("web_app_url", "") ?: "")
    val webAppUrl: StateFlow<String> = _webAppUrl.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _syncMessage = MutableStateFlow<String?>(null)
    val syncMessage: StateFlow<String?> = _syncMessage.asStateFlow()

    private val _isSyncSuccess = MutableStateFlow<Boolean?>(null)
    val isSyncSuccess: StateFlow<Boolean?> = _isSyncSuccess.asStateFlow()

    // Selected date for attendance logging
    private val _selectedDate = MutableStateFlow(getCurrentDateString())
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    // Data Flows
    val workers: StateFlow<List<Worker>> = repository.allWorkers.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val activeWorkers: StateFlow<List<Worker>> = repository.activeWorkers.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val allLabourLogs: StateFlow<List<LabourLog>> = repository.allLabourLogs.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val materials: StateFlow<List<Material>> = repository.allMaterials.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val stockTransactions: StateFlow<List<StockTransaction>> = repository.allStockTransactions.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        // Pre-populate database with some standard construction materials if empty
        viewModelScope.launch {
            repository.allMaterials.collect { list ->
                if (list.isEmpty()) {
                    prepopulateMaterials()
                }
            }
        }
    }

    private suspend fun prepopulateMaterials() {
        val defaultMaterials = listOf(
            Material(name = "Cement (OPC)", category = "Structural", unit = "Bags", currentStock = 120.0, minimumRequired = 30.0),
            Material(name = "Steel Reinforcement 12mm", category = "Structural", unit = "Tons", currentStock = 4.5, minimumRequired = 1.0),
            Material(name = "River Sand", category = "Structural", unit = "Cum", currentStock = 25.0, minimumRequired = 10.0),
            Material(name = "Coarse Aggregate 20mm", category = "Structural", unit = "Cum", currentStock = 30.0, minimumRequired = 8.0),
            Material(name = "Red Bricks", category = "Finishing", unit = "Pieces", currentStock = 5000.0, minimumRequired = 1000.0),
            Material(name = "PVC Drain Pipe 4\"", category = "Plumbing", unit = "Pieces", currentStock = 15.0, minimumRequired = 5.0)
        )
        for (m in defaultMaterials) {
            repository.insertMaterial(m)
        }
    }

    fun setWebAppUrl(url: String) {
        sharedPrefs.edit().putString("web_app_url", url).apply()
        _webAppUrl.value = url
    }

    fun setSelectedDate(date: String) {
        _selectedDate.value = date
    }

    // Workers
    fun addWorker(name: String, role: String, dailyRate: Double, phoneNumber: String) {
        viewModelScope.launch {
            repository.insertWorker(Worker(name = name, role = role, dailyRate = dailyRate, phoneNumber = phoneNumber))
        }
    }

    fun updateWorkerActiveStatus(worker: Worker, isActive: Boolean) {
        viewModelScope.launch {
            repository.updateWorker(worker.copy(isActive = isActive))
        }
    }

    // Labour Logs
    fun recordLabourAttendance(worker: Worker, status: String, hoursWorked: Double, paymentStatus: String, notes: String, date: String = _selectedDate.value) {
        viewModelScope.launch {
            val amountEarned = (worker.dailyRate * (hoursWorked / 8.0)) // standard 8-hour shift is full pay
            val log = LabourLog(
                workerId = worker.id,
                workerName = worker.name,
                date = date,
                status = status,
                hoursWorked = hoursWorked,
                amountEarned = amountEarned,
                paymentStatus = paymentStatus,
                notes = notes,
                synced = false
            )
            repository.insertLabourLog(log)
        }
    }

    fun deleteLabourLog(id: Int) {
        viewModelScope.launch {
            repository.deleteLabourLog(id)
        }
    }

    // Materials & Stock Transactions
    fun addMaterial(name: String, category: String, unit: String, currentStock: Double, minimumRequired: Double) {
        viewModelScope.launch {
            repository.insertMaterial(
                Material(
                    name = name,
                    category = category,
                    unit = unit,
                    currentStock = currentStock,
                    minimumRequired = minimumRequired
                )
            )
        }
    }

    fun deleteMaterial(id: Int) {
        viewModelScope.launch {
            repository.deleteMaterial(id)
        }
    }

    fun recordStockTransaction(material: Material, type: String, quantity: Double, reference: String, recordedBy: String) {
        viewModelScope.launch {
            val transaction = StockTransaction(
                materialId = material.id,
                materialName = material.name,
                type = type,
                quantity = quantity,
                date = getCurrentDateString(),
                reference = reference,
                recordedBy = recordedBy,
                synced = false
            )
            repository.recordStockTransaction(transaction)
        }
    }

    fun deleteStockTransaction(transaction: StockTransaction) {
        viewModelScope.launch {
            repository.deleteStockTransaction(transaction)
        }
    }

    // Google Sheets Sync
    fun syncData() {
        val url = _webAppUrl.value
        if (url.isBlank()) {
            _isSyncSuccess.value = false
            _syncMessage.value = "Web App URL is blank. Configure it in Sync settings."
            return
        }

        viewModelScope.launch {
            _isSyncing.value = true
            _syncMessage.value = "Connecting to Google Sheets..."
            _isSyncSuccess.value = null

            val result = syncManager.syncWithGoogleSheets(url)
            _isSyncing.value = false
            when (result) {
                is SyncResult.Success -> {
                    _isSyncSuccess.value = true
                    _syncMessage.value = result.message
                }
                is SyncResult.Error -> {
                    _isSyncSuccess.value = false
                    _syncMessage.value = result.errorMessage
                }
            }
        }
    }

    fun clearSyncStatus() {
        _syncMessage.value = null
        _isSyncSuccess.value = null
    }

    private fun getCurrentDateString(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }
}
