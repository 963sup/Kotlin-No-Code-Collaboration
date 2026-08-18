from pathlib import Path

HERE = Path(__file__).resolve().parent
TARGET = HERE / "implement_mobile_collaboration_v2_data.py"
text = TARGET.read_text(encoding="utf-8")
old = '''    opening_end = manifest_text.find(">")\n    if opening_end < 0:\n        raise RuntimeError("AndroidManifest.xml opening tag missing")'''
new = '''    manifest_start = manifest_text.find("<manifest")\n    opening_end = manifest_text.find(">", manifest_start)\n    if manifest_start < 0 or opening_end < 0:\n        raise RuntimeError("AndroidManifest.xml opening tag missing")'''
if old not in text:
    raise RuntimeError("manifest permission insertion marker missing")
TARGET.write_text(text.replace(old, new, 1), encoding="utf-8")
try:
    Path(__file__).unlink()
except OSError:
    pass
