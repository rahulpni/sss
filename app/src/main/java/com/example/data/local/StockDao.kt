package com.example.data.local

import androidx.room.*
import com.example.data.model.Material
import com.example.data.model.StockTransaction
import kotlinx.coroutines.flow.Flow

@Dao
interface StockDao {
    @Query("SELECT * FROM materials ORDER BY name ASC")
    fun getAllMaterials(): Flow<List<Material>>

    @Query("SELECT * FROM materials WHERE id = :id")
    suspend fun getMaterialById(id: Int): Material?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMaterial(material: Material)

    @Update
    suspend fun updateMaterial(material: Material)

    @Query("SELECT * FROM stock_transactions ORDER BY date DESC, id DESC")
    fun getAllTransactions(): Flow<List<StockTransaction>>

    @Query("SELECT * FROM stock_transactions WHERE materialId = :materialId ORDER BY date DESC, id DESC")
    fun getTransactionsForMaterial(materialId: Int): Flow<List<StockTransaction>>

    @Query("SELECT * FROM stock_transactions WHERE synced = 0")
    suspend fun getUnsyncedTransactions(): List<StockTransaction>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: StockTransaction)

    @Query("UPDATE stock_transactions SET synced = 1 WHERE id IN (:ids)")
    suspend fun markTransactionsAsSynced(ids: List<Int>)

    @Query("UPDATE materials SET currentStock = currentStock + :amount WHERE id = :materialId")
    suspend fun adjustMaterialStock(materialId: Int, amount: Double)

    @Query("DELETE FROM stock_transactions WHERE id = :id")
    suspend fun deleteTransaction(id: Int)

    @Query("DELETE FROM materials WHERE id = :id")
    suspend fun deleteMaterial(id: Int)
}
