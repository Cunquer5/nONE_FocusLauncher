package com.example.nonelauncher.data.repository

import com.example.nonelauncher.data.local.dao.IntentionDao
import com.example.nonelauncher.data.local.entity.Intention
import kotlinx.coroutines.flow.Flow

class IntentionRepository(private val intentionDao: IntentionDao) {
    suspend fun logIntention(packageName: String, appLabel: String, text: String) {
        val intention = Intention(
            packageName = packageName,
            appLabel = appLabel,
            intentionText = text,
            timestamp = System.currentTimeMillis()
        )
        intentionDao.insertIntention(intention)
    }

    fun getAllIntentions(): Flow<List<Intention>> {
        return intentionDao.getAllIntentions()
    }
}
