import urllib.request
import json
import time
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

print(f"Tracking CI build for Run ID: {RUN_ID}...")

for attempt in range(50):
    url = f"https://api.github.com/repos/{REPO}/actions/runs/{RUN_ID}"
    req = urllib.request.Request(url, headers=headers)
    try:
        with urllib.request.urlopen(req) as resp:
            data = json.loads(resp.read().decode("utf-8"))
            status = data.get("status")
            conclusion = data.get("conclusion")
            print(f"[{attempt+1}] Status: {status} | Conclusion: {conclusion}")
            if status == "completed":
                if conclusion == "success":
                    print("[SUCCESS] Build passed! Proceeding to download...")
                    break
                else:
                    print(f"[FAILED] Build finished with conclusion: {conclusion}")
                    sys.exit(1)
    except Exception as e:
        print(f"Polling error: {e}")
    time.sleep(10)

# Fetch artifacts
print("Fetching artifact download URL...")
art_url = f"https://api.github.com/repos/{REPO}/actions/runs/{RUN_ID}/artifacts"
req = urllib.request.Request(art_url, headers=headers)
with urllib.request.urlopen(req) as resp:
    data = json.loads(resp.read().decode("utf-8"))

artifacts = data.get("artifacts", [])
if not artifacts:
    print("No artifacts found!")
    sys.exit(1)

download_url = artifacts[0]["archive_download_url"]
print(f"Artifact found: {artifacts[0]['name']} -> {download_url}")

# Download using curl to handle S3 redirects cleanly
print("Downloading artifact zip via curl...")
curl_cmd = f'curl.exe -s -L -H "Authorization: Bearer {TOKEN}" "{download_url}" -o "{ZIP_PATH}"'
res = subprocess.run(curl_cmd, shell=True)
if res.returncode != 0 or not os.path.exists(ZIP_PATH):
    print("Failed to download artifact zip!")
    sys.exit(1)

print(f"Downloaded zip size: {os.path.getsize(ZIP_PATH)} bytes. Extracting...")
with zipfile.ZipFile(ZIP_PATH, "r") as z:
    z.extractall(DEST_DIR)

if not os.path.exists(APK_PATH):
    print("APK not found after extraction!")
    sys.exit(1)

print(f"Extracted APK: {APK_PATH} ({os.path.getsize(APK_PATH)} bytes)")

# Install to Vivo phone via ADB
print("\n[ADB] Installing SHIV-OS APK to phone...")
install_out = subprocess.getoutput(f'adb install -r "{APK_PATH}"')
print("ADB Install Result:\n", install_out)

# Launch LauncherActivity
print("\n[ADB] Launching SHIV-OS Launcher...")
launch_out = subprocess.getoutput("adb shell am start -n com.aistudio.shivai.vrmzkq/com.example.LauncherActivity")
print("ADB Launch Result:\n", launch_out)

print("\n[ALL DONE] SHIV-OS Launcher deployed successfully!")
