from pathlib import Path

HERE = Path(__file__).resolve().parent

integration_path = HERE / "implement_mobile_collaboration_v2_integration.py"
integration = integration_path.read_text(encoding="utf-8")
old = '''    marker = f"MainNavigationTab.{label} ->"\n    start = text.find(marker)\n    if start < 0:\n        raise RuntimeError(f"Missing {marker}")'''
new = '''    marker = f"MainNavigationTab.{label} ->"\n    navigation_root = text.find("when (currentTab)")\n    if navigation_root < 0:\n        raise RuntimeError("Main navigation when(currentTab) missing")\n    start = text.find(marker, navigation_root)\n    if start < 0:\n        raise RuntimeError(f"Missing {marker} in currentTab navigation")'''
if old not in integration:
    raise RuntimeError("integration find_branch marker missing")
integration_path.write_text(integration.replace(old, new, 1), encoding="utf-8")

ui_path = HERE / "implement_mobile_collaboration_v2_ui_models.py"
ui = ui_path.read_text(encoding="utf-8")
ui = ui.replace('title = "$prefix態勢｜${scopeName ?: "目前範圍"}",', 'title = "${prefix}態勢｜${scopeName ?: "目前範圍"}",')
ui = ui.replace('import androidx.compose.foundation.layout.weight\\n', '')
ui_path.write_text(ui, encoding="utf-8")

try:
    Path(__file__).unlink()
except OSError:
    pass
