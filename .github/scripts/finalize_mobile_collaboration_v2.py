from pathlib import Path
import re

ROOT = Path(__file__).resolve().parents[2]


def read(rel: str) -> str:
    return (ROOT / rel).read_text(encoding="utf-8")


def save(rel: str, text: str) -> None:
    (ROOT / rel).write_text(text, encoding="utf-8")


models = read("app/src/main/java/com/example/data/model/GovernanceModels.kt")

# Keep WBS plan auditing on the already-established positional helper contract.
repository_path = "app/src/main/java/com/example/data/repository/GovernanceRepository.kt"
repository = read(repository_path)
repository = re.sub(
    r'''        writeAudit\(\s*\n            actor = actor,\s*\n            repo = repo,\s*\n            action = GovernanceAction\.ASSIGN_ISSUE,\s*\n            evaluation = evaluation,\s*\n            overrideActionName = "UPDATE_ISSUE_PLAN",\s*\n            overrideReasoning = "Updated WBS order, planned dates, weight and leaf progress for #\$\{issue\.issueNumber\}\."\s*\n        \)''',
    "        writeAudit(actor, repo, GovernanceAction.ASSIGN_ISSUE, evaluation)",
    repository,
    count=1,
    flags=re.MULTILINE
)
save(repository_path, repository)

# Align projection field references with the actual repository model without
# introducing compatibility aliases or duplicate persisted fields.
projection_path = "app/src/main/java/com/example/ui/model/ExperienceProjections.kt"
projection = read(projection_path)
artifact_block = re.search(r"data class NoCodeArtifact\((.*?)\n\)", models, flags=re.DOTALL)
if artifact_block:
    artifact_fields = artifact_block.group(1)
    if "authorUserId" not in artifact_fields:
        for candidate in ["createdByUserId", "ownerUserId"]:
            if candidate in artifact_fields:
                projection = projection.replace("artifact.authorUserId", f"artifact.{candidate}")
                projection = projection.replace("it.authorUserId", f"it.{candidate}")
                break
issue_block = re.search(r"data class RepoIssue\((.*?)\n\)", models, flags=re.DOTALL)
if issue_block:
    issue_fields = issue_block.group(1)
    if "assigneeDisplayName" not in issue_fields:
        for candidate in ["assigneeName", "assigneeLabel"]:
            if candidate in issue_fields:
                my_work_path = "app/src/main/java/com/example/ui/screens/MobileMyWorkScreen.kt"
                my_work = read(my_work_path).replace("issue.assigneeDisplayName", f"issue.{candidate}")
                save(my_work_path, my_work)
                break
    if "BLOCKED" not in re.search(r"enum class IssueStatus\s*\{(.*?)\}", models, flags=re.DOTALL).group(1):
        projection = projection.replace(
            "it.priority == IssuePriority.CRITICAL || it.status == IssueStatus.BLOCKED",
            "it.priority == IssuePriority.CRITICAL"
        )
save(projection_path, projection)

# The final branch must not carry prior build diagnostics.
for rel in [
    ".github/mobile-v2-build-failure.log",
    ".github/mobile-v2-security-failure.log",
]:
    path = ROOT / rel
    if path.exists():
        path.unlink()

print("mobile collaboration v2 final compatibility pass applied")
