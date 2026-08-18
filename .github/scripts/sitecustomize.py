from pathlib import Path

HERE = Path(__file__).resolve().parent
TARGET = HERE / "implement_mobile_collaboration_v2_data.py"
text = TARGET.read_text(encoding="utf-8")

start = '''\nreplace_once(\n    ".env.example",'''
end = '''\n\nreplace_once(\n    "app/src/main/AndroidManifest.xml",'''

if start in text:
    a = text.index(start)
    b = text.index(end, a)
    replacement = r'''
env_path = ROOT / ".env.example"
env_text = env_path.read_text(encoding="utf-8")
if "SYNC_BASE_URL=" not in env_text:
    if env_text and not env_text.endswith("\n"):
        env_text += "\n"
    env_text += (
        "\n# Authenticated collaboration sync endpoint. "
        "The invalid default keeps sync disabled.\n"
        "SYNC_BASE_URL=https://sync.invalid/\n"
    )
    env_path.write_text(env_text, encoding="utf-8")
'''
    text = text[:a] + "\n" + replacement + text[b:]
    TARGET.write_text(text, encoding="utf-8")

try:
    Path(__file__).unlink()
except OSError:
    pass
