from pathlib import Path

ROOT = Path('.')
patches = {
    ROOT / 'app/src/main/java/com/example/ui/components/RepoDiscussionsSection.kt': {
        'All Categories (${discussions.size})': '所有分類（${discussions.size}）',
    },
    ROOT / 'app/src/main/java/com/example/ui/screens/RepositoriesScreen.kt': {
        'Select Specific ${selectedOwnerType.displayName()}': '選擇指定的 ${selectedOwnerType.displayName()}',
    },
    ROOT / 'app/src/main/java/com/example/ui/screens/UserProfileScreen.kt': {
        'PERSONAL WORKSPACES (OWNED BY ${user.username.uppercase()})': '個人工作區（由 ${user.username.uppercase()} 擁有）',
        'RFC DISCUSSIONS INITIATED (${discussions.size})': '已發起的 RFC 討論（${discussions.size}）',
    },
}
for path, mapping in patches.items():
    text = path.read_text(encoding='utf-8')
    for old, new in mapping.items():
        text = text.replace(old, new)
    path.write_text(text, encoding='utf-8')

# Check user-facing literal values rather than matching Kotlin identifiers such as HomeScreen.
exact_literals = [
    'Access Governance', 'Home', 'Repositories', 'Inbox', 'Me',
    'Kanban Board', 'Nested Tasks', 'Overview', 'Issues', 'Discussions', 'Artifacts',
    'New Issue', 'New Discussion', 'Unified Inbox', 'Repository Settings',
    'Work Requiring Attention', 'SELECT BLOCKING PREREQUISITE',
]
phrase_literals = ['All Categories (', 'Select Specific ', 'PERSONAL WORKSPACES', 'RFC DISCUSSIONS INITIATED']
ui_files = list((ROOT / 'app/src/main/java/com/example/ui').rglob('*.kt')) + [ROOT / 'app/src/main/java/com/example/MainActivity.kt']
joined = '\n'.join(path.read_text(encoding='utf-8') for path in ui_files)
remaining = [token for token in exact_literals if f'"{token}"' in joined]
remaining += [token for token in phrase_literals if f'"{token}' in joined]
if remaining:
    raise RuntimeError(f'fixed English UI literals remain: {sorted(set(remaining))}')
print('Final fixed-English UI literals: 0')
