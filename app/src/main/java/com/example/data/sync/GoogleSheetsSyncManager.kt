package com.example.data.sync

import android.content.Context
import android.util.Log
import com.example.data.model.LabourLog
import com.example.data.model.StockTransaction
import com.example.data.repository.SiteRepository
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

class GoogleSheetsSyncManager(
    private val repository: SiteRepository,
    private val context: Context
) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()

    suspend fun syncWithGoogleSheets(webAppUrl: String): SyncResult = withContext(Dispatchers.IO) {
        if (webAppUrl.isBlank()) {
            return@withContext SyncResult.Error("Google Sheets Web App URL is not configured.")
        }

        try {
            val unsyncedLogs = repository.getUnsyncedLabourLogs()
            val unsyncedTxs = repository.getUnsyncedTransactions()

            if (unsyncedLogs.isEmpty() && unsyncedTxs.isEmpty()) {
                return@withContext SyncResult.Success(0, 0, "All data is already in sync.")
            }

            var syncedLogsCount = 0
            var syncedTxsCount = 0

            // 1. Sync Labour Logs if any
            if (unsyncedLogs.isNotEmpty()) {
                val labourPayload = mapOf(
                    "action" to "sync_labour",
                    "data" to unsyncedLogs
                )
                val type = Types.newParameterizedType(Map::class.java, String::class.java, Any::class.java)
                val adapter = moshi.adapter<Map<String, Any>>(type)
                val jsonBody = adapter.toJson(labourPayload)

                val request = Request.Builder()
                    .url(webAppUrl)
                    .post(jsonBody.toRequestBody("application/json".toMediaType()))
                    .build()

                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        return@withContext SyncResult.Error("Labour sync failed: HTTP ${response.code}")
                    }
                    val bodyString = response.body?.string() ?: ""
                    if (bodyString.contains("\"success\":true") || bodyString.contains("success")) {
                        val ids = unsyncedLogs.map { it.id }
                        repository.markLabourLogsAsSynced(ids)
                        syncedLogsCount = unsyncedLogs.size
                    } else {
                        return@withContext SyncResult.Error("Labour sync server error: $bodyString")
                    }
                }
            }

            // 2. Sync Stock Transactions if any
            if (unsyncedTxs.isNotEmpty()) {
                val stockPayload = mapOf(
                    "action" to "sync_stock",
                    "data" to unsyncedTxs
                )
                val type = Types.newParameterizedType(Map::class.java, String::class.java, Any::class.java)
                val adapter = moshi.adapter<Map<String, Any>>(type)
                val jsonBody = adapter.toJson(stockPayload)

                val request = Request.Builder()
                    .url(webAppUrl)
                    .post(jsonBody.toRequestBody("application/json".toMediaType()))
                    .build()

                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        return@withContext SyncResult.Error("Stock sync failed: HTTP ${response.code}")
                    }
                    val bodyString = response.body?.string() ?: ""
                    if (bodyString.contains("\"success\":true") || bodyString.contains("success")) {
                        val ids = unsyncedTxs.map { it.id }
                        repository.markTransactionsAsSynced(ids)
                        syncedTxsCount = unsyncedTxs.size
                    } else {
                        return@withContext SyncResult.Error("Stock sync server error: $bodyString")
                    }
                }
            }

            return@withContext SyncResult.Success(syncedLogsCount, syncedTxsCount, "Synced $syncedLogsCount logs and $syncedTxsCount transactions.")

        } catch (e: Exception) {
            Log.e("SyncManager", "Sync Error", e)
            return@withContext SyncResult.Error(e.message ?: "Unknown sync error occurred.")
        }
    }
}

sealed class SyncResult {
    data class Success(val labourCount: Int, val stockCount: Int, val message: String) : SyncResult()
    data class Error(val errorMessage: String) : SyncResult()
}
