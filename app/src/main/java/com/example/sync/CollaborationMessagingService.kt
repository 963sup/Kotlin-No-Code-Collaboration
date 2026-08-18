package com.example.sync

import com.example.data.local.AppDatabase
import com.example.data.model.PushRegistration
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Push payloads are treated only as an untrusted sync hint. Domain mutations are
 * fetched through the authenticated sync API and validated before Room writes.
 */
class CollaborationMessagingService : FirebaseMessagingService() {
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onNewToken(token: String) {
        serviceScope.launch {
            AppDatabase.getInstance(applicationContext)
                .collaborationExperienceDao()
                .upsertPushRegistration(PushRegistration(token = token))
            SyncScheduler.requestNow(applicationContext)
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        SyncScheduler.requestNow(applicationContext)
    }
}
