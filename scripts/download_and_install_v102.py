import urllib.request
import json
import zipfile
import io
import os
import subprocess
import sys

token = os.environ.get("GITHUB_TOKEN", "")
repo = 'shivamgupt-2010/SHIV-OS'
run_id = 34578178860
dest_dir = r"F:\SHIV\SHIV-OS"

headers = {
    'Authorization': f'Bearer {token}',
    'Accept': 'application/vnd.github.v3+json',
    'User-Agent': 'SHIV-OS-Deployer'
}

print(f"Fetching artifacts for run {run_id}...")
url = f'https://api.github.com/repos/{repo}/actions/runs/{run_id}/artifacts'
req = urllib.request.Request(url, headers=headers)
with urllib.request.urlopen(req) as resp:
    data = json.loads(resp.read().decode('utf-8'))

artifacts = data.get('artifacts', [])
if not artifacts:
    print("No artifacts found!")
    sys.exit(1)

artifact = artifacts[0]
download_url = artifact['archive_download_url']
print(f"Downloading artifact '{artifact['name']}' from {download_url}...")

dl_req = urllib.request.Request(download_url, headers=headers)
with urllib.request.urlopen(dl_req) as resp:
    zip_bytes = resp.read()

print(f"Downloaded {len(zip_bytes)} bytes. Extracting APK...")
with zipfile.ZipFile(io.BytesIO(zip_bytes)) as zf:
    zf.extractall(dest_dir)

apk_path = os.path.join(dest_dir, "app-debug.apk")
if not os.path.exists(apk_path):
    print("APK not found after extraction!")
    sys.exit(1)

apk_size = os.path.getsize(apk_path)
print(f"[SUCCESS] Extracted APK to {apk_path} ({apk_size} bytes)")

# Install to Vivo phone via ADB
print("\nInstalling to connected phone via ADB...")
install_out = subprocess.getoutput(f'adb install -r "{apk_path}"')
print("ADB Install Result:", install_out)

# Launch LauncherActivity
print("\nLaunching SHIV-OS Launcher on phone...")
launch_out = subprocess.getoutput('adb shell am start -n com.aistudio.shivai.vrmzkq/com.example.LauncherActivity')
print("ADB Launch Result:", launch_out)
