package com.example.data.repository

import com.example.data.local.LabourDao
import com.example.data.local.StockDao
import com.example.data.model.Worker
import com.example.data.model.LabourLog
import com.example.data.model.Material
import com.example.data.model.StockTransaction
import kotlinx.coroutines.flow.Flow

class SiteRepository(
    private val labourDao: LabourDao,
    private val stockDao: StockDao
) {
    // Labour Management
    val allWorkers: Flow<List<Worker>> = labourDao.getAllWorkers()
    val activeWorkers: Flow<List<Worker>> = labourDao.getActiveWorkers()
    val allLabourLogs: Flow<List<LabourLog>> = labourDao.getAllLabourLogs()

    fun getLabourLogsForDate(date: String): Flow<List<LabourLog>> {
        return labourDao.getLabourLogsForDate(date)
    }

    suspend fun insertWorker(worker: Worker) {
        labourDao.insertWorker(worker)
    }

    suspend fun updateWorker(worker: Worker) {
        labourDao.updateWorker(worker)
    }

    suspend fun insertLabourLog(log: LabourLog) {
        labourDao.insertLabourLog(log)
    }

    suspend fun insertLabourLogs(logs: List<LabourLog>) {
        labourDao.insertLabourLogs(logs)
    }

    suspend fun deleteLabourLog(id: Int) {
        labourDao.deleteLabourLog(id)
    }

    // Material & Stock Management
    val allMaterials: Flow<List<Material>> = stockDao.getAllMaterials()
    val allStockTransactions: Flow<List<StockTransaction>> = stockDao.getAllTransactions()

    fun getTransactionsForMaterial(materialId: Int): Flow<List<StockTransaction>> {
        return stockDao.getTransactionsForMaterial(materialId)
    }

    suspend fun insertMaterial(material: Material) {
        stockDao.insertMaterial(material)
    }

    suspend fun updateMaterial(material: Material) {
        stockDao.updateMaterial(material)
    }

    suspend fun deleteMaterial(id: Int) {
        stockDao.deleteMaterial(id)
    }

    // Records a transaction and automatically adjusts material stock!
    suspend fun recordStockTransaction(transaction: StockTransaction) {
        // Insert transaction first
        stockDao.insertTransaction(transaction)
        
        // Adjust stock
        val adjustment = if (transaction.type == "IN") {
            transaction.quantity
        } else {
            -transaction.quantity
        }
        stockDao.adjustMaterialStock(transaction.materialId, adjustment)
    }

    suspend fun deleteStockTransaction(transaction: StockTransaction) {
        // Reverse the stock adjustment before deleting the transaction
        val reversalAdjustment = if (transaction.type == "IN") {
            -transaction.quantity
        } else {
            transaction.quantity
        }
        stockDao.adjustMaterialStock(transaction.materialId, reversalAdjustment)
        stockDao.deleteTransaction(transaction.id)
    }

    // Sync helpers
    suspend fun getUnsyncedLabourLogs(): List<LabourLog> {
        return labourDao.getUnsyncedLabourLogs()
    }

    suspend fun getUnsyncedTransactions(): List<StockTransaction> {
        return stockDao.getUnsyncedTransactions()
    }

    suspend fun markLabourLogsAsSynced(ids: List<Int>) {
        labourDao.markLabourLogsAsSynced(ids)
    }

    suspend fun markTransactionsAsSynced(ids: List<Int>) {
        stockDao.markTransactionsAsSynced(ids)
    }
}
