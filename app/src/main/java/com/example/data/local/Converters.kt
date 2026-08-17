package com.example.data.local

import androidx.room.TypeConverter
import com.example.data.model.ApprovalStatus
import com.example.data.model.ArtifactType
import com.example.data.model.DiscussionCategory
import com.example.data.model.GranteeType
import com.example.data.model.IssuePriority
import com.example.data.model.IssueStatus
import com.example.data.model.LifecycleState
import com.example.data.model.OrgRole
import com.example.data.model.OwnerType
import com.example.data.model.PolicyVerdict
import com.example.data.model.RepoRole
import com.example.data.model.ReviewDecision
import com.example.data.model.TeamRole

class Converters {
    @TypeConverter
    fun fromOwnerType(value: OwnerType): String = value.name

    @TypeConverter
    fun toOwnerType(value: String): OwnerType = OwnerType.valueOf(value)

    @TypeConverter
    fun fromRepoRole(value: RepoRole): String = value.name

    @TypeConverter
    fun toRepoRole(value: String): RepoRole = RepoRole.valueOf(value)

    @TypeConverter
    fun fromOrgRole(value: OrgRole): String = value.name

    @TypeConverter
    fun toOrgRole(value: String): OrgRole = OrgRole.valueOf(value)

    @TypeConverter
    fun fromTeamRole(value: TeamRole): String = value.name

    @TypeConverter
    fun toTeamRole(value: String): TeamRole = TeamRole.valueOf(value)

    @TypeConverter
    fun fromGranteeType(value: GranteeType): String = value.name

    @TypeConverter
    fun toGranteeType(value: String): GranteeType = GranteeType.valueOf(value)

    @TypeConverter
    fun fromArtifactType(value: ArtifactType): String = value.name

    @TypeConverter
    fun toArtifactType(value: String): ArtifactType = ArtifactType.valueOf(value)

    @TypeConverter
    fun fromLifecycleState(value: LifecycleState): String = value.name

    @TypeConverter
    fun toLifecycleState(value: String): LifecycleState = LifecycleState.valueOf(value)

    @TypeConverter
    fun fromReviewDecision(value: ReviewDecision): String = value.name

    @TypeConverter
    fun toReviewDecision(value: String): ReviewDecision = ReviewDecision.valueOf(value)

    @TypeConverter
    fun fromApprovalStatus(value: ApprovalStatus): String = value.name

    @TypeConverter
    fun toApprovalStatus(value: String): ApprovalStatus = ApprovalStatus.valueOf(value)

    @TypeConverter
    fun fromPolicyVerdict(value: PolicyVerdict): String = value.name

    @TypeConverter
    fun toPolicyVerdict(value: String): PolicyVerdict = PolicyVerdict.valueOf(value)

    @TypeConverter
    fun fromIssueStatus(value: IssueStatus): String = value.name

    @TypeConverter
    fun toIssueStatus(value: String): IssueStatus = IssueStatus.valueOf(value)

    @TypeConverter
    fun fromIssuePriority(value: IssuePriority): String = value.name

    @TypeConverter
    fun toIssuePriority(value: String): IssuePriority = IssuePriority.valueOf(value)

    @TypeConverter
    fun fromDiscussionCategory(value: DiscussionCategory): String = value.name

    @TypeConverter
    fun toDiscussionCategory(value: String): DiscussionCategory = DiscussionCategory.valueOf(value)
}
