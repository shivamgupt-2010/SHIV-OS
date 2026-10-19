import urllib.request
import json
import subprocess
import os
import zipfile
import sys

RUN_ID = 34586364944
TOKEN = os.environ.get("GITHUB_TOKEN", "")
REPO = "shivamgupt-2010/SHIV-OS"
DEST_DIR = r"F:\SHIV\SHIV-OS"
ZIP_PATH = os.path.join(DEST_DIR, "app-debug-artifact.zip")
APK_PATH = os.path.join(DEST_DIR, "app-debug.apk")

headers = {
    "Authorization": f"Bearer {TOKEN}",
    "Accept": "application/vnd.github.v3+json",
    "User-Agent": "SHIV-OS-Deployer"
}

print(f"Fetching artifact URL for run {RUN_ID}...")
art_url = f"https://api.github.com/repos/{REPO}/actions/runs/{RUN_ID}/artifacts"
req = urllib.request.Request(art_url, headers=headers)
with urllib.request.urlopen(req) as resp:
    data = json.loads(resp.read().decode("utf-8"))

artifacts = data.get("artifacts", [])
if not artifacts:
    print("No artifacts found!")
    sys.exit(1)

download_url = artifacts[0]["archive_download_url"]
print(f"Artifact found: {artifacts[0]['name']} -> downloading...")

curl_cmd = f'curl.exe -s -L -H "Authorization: Bearer {TOKEN}" "{download_url}" -o "{ZIP_PATH}"'
res = subprocess.run(curl_cmd, shell=True)
if res.returncode != 0 or not os.path.exists(ZIP_PATH):
    print("Download failed!")
    sys.exit(1)

if os.path.exists(APK_PATH):
    try:
        os.remove(APK_PATH)
    except Exception:
        pass

with zipfile.ZipFile(ZIP_PATH, "r") as z:
    z.extractall(DEST_DIR)

print(f"APK extracted: {APK_PATH}, size: {os.path.getsize(APK_PATH)} bytes")

print("Installing to phone via ADB...")
install_out = subprocess.getoutput(f'adb install -r "{APK_PATH}"')
print("ADB Install Result:\n", install_out)

print("Starting LauncherActivity...")
start_out = subprocess.getoutput("adb shell am start -n com.aistudio.shivai.vrmzkq/com.example.LauncherActivity")
print("Launch Result:\n", start_out)
print("[SUCCESS] Installed and launched successfully!")
