#!/usr/bin/env python3
"""Apply only deterministic, architecture-independent safety corrections.

The script intentionally does not rewrite navigation or domain ownership because those
changes require repository-specific ViewModel and permission wiring. It is idempotent
and fails when a destructive migration call remains.
"""

from __future__ import annotations

import json
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
SOURCE_ROOT = ROOT / "app" / "src" / "main"
changes: list[dict[str, str]] = []


def record(path: Path, reason: str) -> None:
    changes.append({"path": str(path.relative_to(ROOT)), "reason": reason})


for path in SOURCE_ROOT.rglob("*.kt"):
    original = path.read_text(encoding="utf-8")
    updated = original

    # A light scheme must not be constructed by darkColorScheme. Match only a
    # declaration whose identifier explicitly denotes the light scheme.
    light_pattern = re.compile(
        r"((?:private\s+|internal\s+|public\s+)?val\s+\w*Light\w*Scheme\s*=\s*)darkColorScheme\s*\(",
        re.MULTILINE,
    )
    if light_pattern.search(updated):
        updated = light_pattern.sub(r"\1lightColorScheme(", updated)
        if "import androidx.compose.material3.lightColorScheme" not in updated:
            import_anchor = "import androidx.compose.material3.darkColorScheme\n"
            if import_anchor in updated:
                updated = updated.replace(
                    import_anchor,
                    import_anchor + "import androidx.compose.material3.lightColorScheme\n",
                    1,
                )
        record(path, "construct the declared light palette with lightColorScheme")

    # Destructive migration is never an acceptable default for field data.
    destructive_patterns = (
        r"\s*\.fallbackToDestructiveMigration\s*\(\s*\)",
        r"\s*\.fallbackToDestructiveMigrationOnDowngrade\s*\(\s*\)",
        r"\s*\.fallbackToDestructiveMigrationFrom\s*\([^)]*\)",
    )
    before_migration = updated
    for pattern in destructive_patterns:
        updated = re.sub(pattern, "", updated)
    if updated != before_migration:
        record(path, "remove destructive Room migration fallback")

    if updated != original:
        path.write_text(updated, encoding="utf-8")

remaining: list[str] = []
for path in SOURCE_ROOT.rglob("*.kt"):
    text = path.read_text(encoding="utf-8")
    if "fallbackToDestructiveMigration" in text:
        remaining.append(str(path.relative_to(ROOT)))

report = {
    "changes": changes,
    "remainingDestructiveMigrationReferences": remaining,
}
(ROOT / "FIRST_PRINCIPLES_SAFETY_PATCH.json").write_text(
    json.dumps(report, ensure_ascii=False, indent=2) + "\n",
    encoding="utf-8",
)

if remaining:
    raise SystemExit(
        "Destructive migration references remain: " + ", ".join(remaining)
    )
