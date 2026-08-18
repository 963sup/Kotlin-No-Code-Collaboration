package com.example

import android.content.Context
import android.content.pm.ApplicationInfo
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class BackupPolicyTest {
    @Test
    fun enterpriseCollaborationDataIsNotEligibleForAutoBackup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val allowBackupFlag = context.applicationInfo.flags and ApplicationInfo.FLAG_ALLOW_BACKUP
        assertEquals(0, allowBackupFlag)
    }
}
