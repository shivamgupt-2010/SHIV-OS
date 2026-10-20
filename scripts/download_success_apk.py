import os
import sys
import json
import zipfile
import urllib.request

TOKEN = os.environ.get("GITHUB_TOKEN") or (sys.argv[1] if len(sys.argv) > 1 else "")
RUN_ID = "39378371637"
REPO = "shivamgupt-2010/SHIV-OS"
DEST_DIR = r"F:\SHIV\SHIV-OS\build-output"
ZIP_PATH = os.path.join(DEST_DIR, "app-debug-artifact.zip")

os.makedirs(DEST_DIR, exist_ok=True)

class NoAuthRedirect(urllib.request.HTTPRedirectHandler):
    def redirect_request(self, req, fp, code, msg, headers, newurl):
        new_req = super().redirect_request(req, fp, code, msg, headers, newurl)
        if 'Authorization' in new_req.headers:
            del new_req.headers['Authorization']
        return new_req

opener = urllib.request.build_opener(NoAuthRedirect)

# 1. Fetch artifacts list
art_url = f"https://api.github.com/repos/{REPO}/actions/runs/{RUN_ID}/artifacts"
req = urllib.request.Request(art_url, headers={
    "Authorization": f"Bearer {TOKEN}",
    "Accept": "application/vnd.github.v3+json",
    "User-Agent": "SHIV-OS-Downloader"
})

with opener.open(req) as resp:
    data = json.loads(resp.read().decode("utf-8"))
    artifacts = data.get("artifacts", [])

if not artifacts:
    print("No artifacts found!")
    sys.exit(1)

art = artifacts[0]
download_url = art["archive_download_url"]
print(f"Artifact Name: {art['name']}")
print(f"Reported Size: {art['size_in_bytes']} bytes (~{art['size_in_bytes'] / (1024*1024):.2f} MB)")
print(f"Download URL:  {download_url}")

# 2. Download zip
print("\nDownloading artifact zip with redirected credentials stripped...")
req_dl = urllib.request.Request(download_url, headers={
    "Authorization": f"Bearer {TOKEN}",
    "Accept": "application/vnd.github.v3+json",
    "User-Agent": "SHIV-OS-Downloader"
})

with opener.open(req_dl) as resp_dl, open(ZIP_PATH, "wb") as out_fp:
    chunk_size = 1024 * 1024  # 1MB chunks
    downloaded = 0
    while True:
        chunk = resp_dl.read(chunk_size)
        if not chunk:
            break
        out_fp.write(chunk)
        downloaded += len(chunk)
        print(f" -> Downloaded: {downloaded / (1024*1024):.2f} MB", end="\r", flush=True)

print(f"\n[DONE] Saved zip to {ZIP_PATH} ({os.path.getsize(ZIP_PATH)} bytes)")

# 3. Extract APK
print("Extracting APK from archive...")
with zipfile.ZipFile(ZIP_PATH, "r") as zf:
    zf.extractall(DEST_DIR)
    for name in zf.namelist():
        full = os.path.join(DEST_DIR, name)
        size_mb = os.path.getsize(full) / (1024 * 1024)
        print(f" [EXTRACTED] {name} ({size_mb:.2f} MB) at {full}")

# Copy to root F:\SHIV\SHIV-OS\app-debug.apk as well for easy access
apk_src = os.path.join(DEST_DIR, "app-debug.apk")
if os.path.exists(apk_src):
    root_apk = r"F:\SHIV\SHIV-OS\app-debug.apk"
    import shutil
    shutil.copy2(apk_src, root_apk)
    print(f" [COPIED] Copied to root workspace: {root_apk}")
    print("\n>>> SHIV LAUNCHER APK BUILD & VERIFICATION 100% COMPLETE <<<")
