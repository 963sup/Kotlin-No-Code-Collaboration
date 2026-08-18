package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.data.model.AppNotification
import com.example.data.model.ArtifactApproval
import com.example.data.model.ArtifactReview
import com.example.data.model.AuditLog
import com.example.data.model.DiscussionComment
import com.example.data.model.Enterprise
import com.example.data.model.IssueComment
import com.example.data.model.IssueDependency
import com.example.data.model.NoCodeArtifact
import com.example.data.model.OrgMembership
import com.example.data.model.Organization
import com.example.data.model.PushRegistration
import com.example.data.model.RepoAccessRule
import com.example.data.model.RepoDiscussion
import com.example.data.model.RepoIssue
import com.example.data.model.Repository
import com.example.data.model.SavedTarget
import com.example.data.model.SyncConflict
import com.example.data.model.SyncCursor
import com.example.data.model.SyncMetadata
import com.example.data.model.SyncOutbox
import com.example.data.model.SyncRuntimeState
import com.example.data.model.TaskChecklist
import com.example.data.model.Team
import com.example.data.model.TeamMembership
import com.example.data.model.User
import com.example.data.model.UserFollow
import com.example.data.model.WorkEvidence
import com.example.data.model.WorkVerification

@Database(
    entities = [
        Enterprise::class,
        Organization::class,
        User::class,
        Team::class,
        TeamMembership::class,
        OrgMembership::class,
        Repository::class,
        WorkEvidence::class,
        WorkVerification::class,
        TaskChecklist::class,
        RepoAccessRule::class,
        NoCodeArtifact::class,
        ArtifactReview::class,
        ArtifactApproval::class,
        AuditLog::class,
        RepoIssue::class,
        IssueDependency::class,
        IssueComment::class,
        RepoDiscussion::class,
        DiscussionComment::class,
        AppNotification::class,
        SavedTarget::class,
        UserFollow::class,
        SyncOutbox::class,
        SyncMetadata::class,
        SyncCursor::class,
        SyncConflict::class,
        PushRegistration::class,
        SyncRuntimeState::class,
    ],
    version = AppMigrations.CURRENT_VERSION,
    exportSchema = false,
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun governanceDao(): GovernanceDao
    abstract fun collaborationExperienceDao(): CollaborationExperienceDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase = INSTANCE ?: synchronized(this) {
            val instance = Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "repo_governance_db",
            )
                .addMigrations(AppMigrations.MIGRATION_4_5, AppMigrations.MIGRATION_5_6)
                .addCallback(DatabaseSyncTriggers.callback)
                .build()
            INSTANCE = instance
            instance
        }
    }
}
