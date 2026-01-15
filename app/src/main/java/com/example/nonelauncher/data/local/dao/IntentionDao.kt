package com.example.nonelauncher.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.nonelauncher.data.local.entity.Intention
import kotlinx.coroutines.flow.Flow

@Dao
interface IntentionDao {
    @Insert
    suspend fun insertIntention(intention: Intention)

    @Query("SELECT * FROM intentions ORDER BY timestamp DESC")
    fun getAllIntentions(): Flow<List<Intention>>
}
