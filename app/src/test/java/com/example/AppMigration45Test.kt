package com.example

import android.content.Context
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppMigrations
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class AppMigration45Test {
    @Test
    fun migration4To5PreservesExistingIssueAndCreatesNewSchema() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val databaseName = "migration-4-5-${System.nanoTime()}.db"
        context.deleteDatabase(databaseName)
        val helper = FrameworkSQLiteOpenHelperFactory().create(
            SupportSQLiteOpenHelper.Configuration.builder(context)
                .name(databaseName)
                .callback(object : SupportSQLiteOpenHelper.Callback(4) {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        db.execSQL("CREATE TABLE repo_issues (id TEXT NOT NULL PRIMARY KEY, issueNumber INTEGER NOT NULL, title TEXT NOT NULL, status TEXT NOT NULL)")
                        db.execSQL("CREATE TABLE repositories (id TEXT NOT NULL PRIMARY KEY)")
                        db.execSQL("CREATE TABLE no_code_artifacts (id TEXT NOT NULL PRIMARY KEY)")
                        db.execSQL("CREATE TABLE repo_discussions (id TEXT NOT NULL PRIMARY KEY)")
                        db.execSQL("INSERT INTO repo_issues(id, issueNumber, title, status) VALUES('iss_existing', 7, 'Existing field task', 'CLOSED')")
                    }
                    override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) = Unit
                })
                .build()
        )
        try {
            val db = helper.writableDatabase
            AppMigrations.MIGRATION_4_5.migrate(db)
            db.query("SELECT title, sortOrder, progressPercent, wbsWeight FROM repo_issues WHERE id = 'iss_existing'").use { cursor ->
                assertTrue(cursor.moveToFirst())
                assertEquals("Existing field task", cursor.getString(0))
                assertEquals(7, cursor.getInt(1))
                assertEquals(100, cursor.getInt(2))
                assertEquals(1.0, cursor.getDouble(3), 0.0)
            }
            val columns = mutableSetOf<String>()
            db.query("PRAGMA table_info(repo_issues)").use { cursor ->
                val index = cursor.getColumnIndexOrThrow("name")
                while (cursor.moveToNext()) columns += cursor.getString(index)
            }
            assertTrue(columns.containsAll(setOf("sortOrder", "plannedStartAt", "plannedEndAt", "wbsWeight", "progressPercent")))
            val tables = mutableSetOf<String>()
            db.query("SELECT name FROM sqlite_master WHERE type = 'table'").use { cursor ->
                while (cursor.moveToNext()) tables += cursor.getString(0)
            }
            assertTrue(tables.containsAll(setOf("saved_targets", "user_follows", "sync_outbox", "sync_metadata", "sync_cursors", "sync_conflicts", "push_registrations", "sync_runtime_state")))
        } finally {
            helper.close()
            context.deleteDatabase(databaseName)
        }
    }
}
