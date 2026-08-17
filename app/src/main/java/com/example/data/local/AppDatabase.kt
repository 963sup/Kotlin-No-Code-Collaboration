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
import com.example.data.model.RepoAccessRule
import com.example.data.model.RepoDiscussion
import com.example.data.model.RepoIssue
import com.example.data.model.Repository
import com.example.data.model.Team
import com.example.data.model.TeamMembership
import com.example.data.model.User

@Database(
    entities = [
        Enterprise::class,
        Organization::class,
        User::class,
        Team::class,
        TeamMembership::class,
        OrgMembership::class,
        Repository::class,
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
        AppNotification::class
    ],
    version = 4,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun governanceDao(): GovernanceDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "repo_governance_db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
