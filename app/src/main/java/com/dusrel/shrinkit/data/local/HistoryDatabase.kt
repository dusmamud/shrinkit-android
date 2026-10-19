package com.dusrel.shrinkit.data.local

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

/**
 * A processed image record for the History screen.
 * The actual bytes live in MediaStore (Pictures/ShrinkIt); this keeps the
 * metadata needed for listing, stats and re-sharing.
 */
@Entity(tableName = "history")
data class ProcessedImageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val filename: String,
    val contentUri: String,
    val originalBytes: Long,
    val processedBytes: Long,
    val width: Int,
    val height: Int,
    val format: String,
    val createdAt: Long
)

@Dao
interface HistoryDao {

    @Query("SELECT * FROM history ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<ProcessedImageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: ProcessedImageEntity): Long

    @Query("DELETE FROM history WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM history")
    suspend fun clearAll()
}

@Database(
    entities = [ProcessedImageEntity::class],
    version = 1,
    exportSchema = false
)
abstract class HistoryDatabase : RoomDatabase() {
    abstract fun historyDao(): HistoryDao
}
