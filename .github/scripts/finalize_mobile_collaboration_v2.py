from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]


def patch(rel: str, old: str, new: str) -> None:
    path = ROOT / rel
    text = path.read_text(encoding="utf-8")
    if old in text:
        text = text.replace(old, new, 1)
        path.write_text(text, encoding="utf-8")


# WBS plan changes alter scheduling/weight/progress and must always traverse the
# existing assignment-policy path; being the original author is not an override.
patch(
    "app/src/main/java/com/example/data/repository/GovernanceRepository.kt",
    "if (evaluation.verdict != PolicyVerdict.ALLOWED && issue.authorUserId != actor.id)",
    "if (evaluation.verdict != PolicyVerdict.ALLOWED)"
)

patch(
    "app/src/main/java/com/example/ui/model/ExperienceProjections.kt",
    '''    fun stats(user: User, auditLogs: List<AuditLog>): SocialStats {\n        val public = auditLogs.filter { it.actorUserId == user.id && PublicActivityPolicy.isPublic(it) }''',
    '''    fun stats(\n        user: User,\n        auditLogs: List<AuditLog>,\n        visibleRepositoryIds: Set<String>\n    ): SocialStats {\n        val public = auditLogs.filter { log ->\n            log.actorUserId == user.id &&\n                PublicActivityPolicy.isPublic(log) &&\n                log.repoId in visibleRepositoryIds\n        }'''
)

patch(
    "app/src/main/java/com/example/ui/screens/SocialProfileScreen.kt",
    '''    auditLogs: List<AuditLog>,\n    follows: List<UserFollow>,''',
    '''    auditLogs: List<AuditLog>,\n    visibleRepositoryIds: Set<String>,\n    follows: List<UserFollow>,'''
)
patch(
    "app/src/main/java/com/example/ui/screens/SocialProfileScreen.kt",
    '''    val stats = SocialProjection.stats(profileUser, auditLogs)''',
    '''    val stats = SocialProjection.stats(profileUser, auditLogs, visibleRepositoryIds)'''
)

patch(
    "app/src/main/java/com/example/ui/screens/PersonalCenterSwitchScreen.kt",
    '''    auditLogs: List<AuditLog>,\n    follows: List<UserFollow>,''',
    '''    auditLogs: List<AuditLog>,\n    visibleRepositoryIds: Set<String>,\n    follows: List<UserFollow>,'''
)
patch(
    "app/src/main/java/com/example/ui/screens/PersonalCenterSwitchScreen.kt",
    '''SocialProfileScreen(profileUser, activeUser, auditLogs, follows, savedTargets, syncStatus, onToggleFollow, onSyncNow)''',
    '''SocialProfileScreen(profileUser, activeUser, auditLogs, visibleRepositoryIds, follows, savedTargets, syncStatus, onToggleFollow, onSyncNow)'''
)

patch(
    "app/src/main/java/com/example/MainActivity.kt",
    '''                                    auditLogs = scopedAuditLogs,\n                                    follows = userFollows,''',
    '''                                    auditLogs = scopedAuditLogs,\n                                    visibleRepositoryIds = scopedRepoIds,\n                                    follows = userFollows,'''
)

print("current-main mobile v2 final defense-in-depth pass applied")
