import os

reps = {
    "associate由": "associateBy",
    "group由": "groupBy",
    "sorted由": "sortedBy",
    "then由": "thenBy",
    "distinct由": "distinctBy",
    "currentBlocked由": "currentBlockedBy",
    "onUpdateIssue狀態：": "onUpdateIssueStatus:",
    "onUpdateIssue狀態": "onUpdateIssueStatus",
    "new狀態：": "newStatus:",
    "new狀態": "newStatus",
    "notificationFilter狀態：": "notificationFilterStatus:",
    "notificationFilter狀態": "notificationFilterStatus",
    "onUpdate狀態：": "onUpdateStatus:",
    "onUpdate狀態": "onUpdateStatus",
    "role範圍：": "roleScope:",
    "role範圍": "roleScope",
    "角色範圍：": "roleScope:",
    "角色範圍": "roleScope",
}

for root, dirs, files in os.walk("app/src/main/java"):
    for file in files:
        if file.endswith(".kt"):
            path = os.path.join(root, file)
            with open(path, "r", encoding="utf-8") as f:
                content = f.read()
            
            changed = False
            for k, v in reps.items():
                if k in content:
                    content = content.replace(k, v)
                    changed = True
            
            if changed:
                with open(path, "w", encoding="utf-8") as f:
                    f.write(content)
                print(f"Updated {path}")
print("Done fixing identifiers.")
