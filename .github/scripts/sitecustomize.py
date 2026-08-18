from pathlib import Path

HERE = Path(__file__).resolve().parent

ui_path = HERE / "implement_mobile_collaboration_v2_ui_models.py"
ui = ui_path.read_text(encoding="utf-8")
ui = ui.replace("import androidx.compose.foundation.layout.weight\n", "")
ui = ui.replace("Column(Modifier.fillMaxWidth().weight(1f)) {", "Column(Modifier.fillMaxWidth()) {")
ui_path.write_text(ui, encoding="utf-8")

integration_path = HERE / "implement_mobile_collaboration_v2_integration.py"
integration = integration_path.read_text(encoding="utf-8")
old = '''me = \'\'\'MainNavigationTab.ME -> {\\n                            val profile = inspectedProfileUser ?: activeUser\\n                            if (profile != null && activeUser != null) {\\n                                PersonalCenterSwitchScreen(\\n                                    profileUser = profile,\\n                                    activeUser = activeUser,\\n                                    auditLogs = scopedAuditLogs,\\n                                    follows = userFollows,\\n                                    savedTargets = savedTargets,\\n                                    syncStatus = syncStatus,\\n                                    onToggleFollow = { experienceViewModel.toggleFollow(activeUser.id, it) },\\n                                    onSyncNow = experienceViewModel::syncNow,\\n                                    governanceContent = {\\n\'\'\' + me_inner.rstrip() + \'\'\'\\n                                    }\\n                                )\\n                            }\\n                        }\'\'\''''
new = '''me = \'\'\'MainNavigationTab.ME -> {\\n                            val currentActiveUser = activeUser\\n                            val profile = inspectedProfileUser ?: currentActiveUser\\n                            if (profile != null && currentActiveUser != null) {\\n                                PersonalCenterSwitchScreen(\\n                                    profileUser = profile,\\n                                    activeUser = currentActiveUser,\\n                                    auditLogs = scopedAuditLogs,\\n                                    follows = userFollows,\\n                                    savedTargets = savedTargets,\\n                                    syncStatus = syncStatus,\\n                                    onToggleFollow = { experienceViewModel.toggleFollow(currentActiveUser.id, it) },\\n                                    onSyncNow = experienceViewModel::syncNow,\\n                                    governanceContent = {\\n\'\'\' + me_inner.rstrip() + \'\'\'\\n                                    }\\n                                )\\n                            }\\n                        }\'\'\''''
if old not in integration:
    raise RuntimeError("personal center integration marker missing")
integration_path.write_text(integration.replace(old, new, 1), encoding="utf-8")

try:
    Path(__file__).unlink()
except OSError:
    pass
