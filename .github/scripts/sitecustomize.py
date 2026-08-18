from pathlib import Path

HERE = Path(__file__).resolve().parent
TARGET = HERE / "implement_mobile_collaboration_v2_data.py"
text = TARGET.read_text(encoding="utf-8")

start = '''\nreplace_once(\n    "app/src/main/AndroidManifest.xml",\n    '<manifest xmlns:android="http://schemas.android.com/apk/res/android">','''
next_block = '''\nreplace_once(\n    "app/src/main/AndroidManifest.xml",\n    \'\'\'        <activity'''

if start in text:
    a = text.index(start)
    b = text.index(next_block, a)
    replacement = r'''
manifest_path = ROOT / "app/src/main/AndroidManifest.xml"
manifest_text = manifest_path.read_text(encoding="utf-8")
if "android.permission.INTERNET" not in manifest_text:
    opening_end = manifest_text.find(">")
    if opening_end < 0:
        raise RuntimeError("AndroidManifest.xml opening tag missing")
    manifest_text = (
        manifest_text[:opening_end + 1]
        + "\n\n    <uses-permission android:name=\"android.permission.INTERNET\" />"
        + "\n    <uses-permission android:name=\"android.permission.ACCESS_NETWORK_STATE\" />"
        + manifest_text[opening_end + 1:]
    )
    manifest_path.write_text(manifest_text, encoding="utf-8")
'''
    text = text[:a] + "\n" + replacement + text[b:]
    TARGET.write_text(text, encoding="utf-8")

try:
    Path(__file__).unlink()
except OSError:
    pass
