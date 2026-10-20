import os
import sys
import time
import json
import zipfile
import io
import urllib.request
import urllib.error

TOKEN = os.environ.get("GITHUB_TOKEN") or (sys.argv[1] if len(sys.argv) > 1 else "")
RUN_ID = sys.argv[2] if len(sys.argv) > 2 else "39283743598"
REPO = "shivamgupt-2010/SHIV-OS"
OUTPUT_DIR = r"F:\SHIV\SHIV-OS\build-output"

headers = {
    "Authorization": f"Bearer {TOKEN}",
    "Accept": "application/vnd.github.v3+json",
    "User-Agent": "SHIV-OS-CI-Tracker"
}

def get_run_status():
    url = f"https://api.github.com/repos/{REPO}/actions/runs/{RUN_ID}"
    req = urllib.request.Request(url, headers=headers)
    try:
        with urllib.request.urlopen(req, timeout=30) as resp:
            return json.loads(resp.read().decode("utf-8"))
    except Exception as e:
        print(f"Error fetching run status: {e}", flush=True)
        return None

def get_run_jobs():
    url = f"https://api.github.com/repos/{REPO}/actions/runs/{RUN_ID}/jobs"
    req = urllib.request.Request(url, headers=headers)
    try:
        with urllib.request.urlopen(req, timeout=30) as resp:
            return json.loads(resp.read().decode("utf-8")).get("jobs", [])
    except Exception as e:
        return []

def download_artifact(run_data):
    art_url = f"https://api.github.com/repos/{REPO}/actions/runs/{RUN_ID}/artifacts"
    req = urllib.request.Request(art_url, headers=headers)
    with urllib.request.urlopen(req, timeout=30) as resp:
        artifacts = json.loads(resp.read().decode("utf-8")).get("artifacts", [])
    
    if not artifacts:
        print("No artifacts found for this run!", flush=True)
        return False
    
    os.makedirs(OUTPUT_DIR, exist_ok=True)
    for art in artifacts:
        art_id = art["id"]
        art_name = art["name"]
        art_size = art["size_in_bytes"]
        print(f"Downloading artifact: {art_name} ({art_size} bytes)...", flush=True)
        
        dl_url = f"https://api.github.com/repos/{REPO}/actions/artifacts/{art_id}/zip"
        req_dl = urllib.request.Request(dl_url, headers=headers)
        with urllib.request.urlopen(req_dl, timeout=120) as resp_dl:
            zip_bytes = resp_dl.read()
        
        with zipfile.ZipFile(io.BytesIO(zip_bytes)) as zf:
            zf.extractall(OUTPUT_DIR)
            print(f"Extracted files to {OUTPUT_DIR}: {zf.namelist()}", flush=True)
    
    return True

print("=" * 70, flush=True)
print(f" TRACKING GITHUB ACTIONS RUN: {RUN_ID}", flush=True)
print(f" Repository: https://github.com/{REPO}", flush=True)
print(f" Run URL:    https://github.com/{REPO}/actions/runs/{RUN_ID}", flush=True)
print("=" * 70, flush=True)

start_time = time.time()
max_wait_seconds = 1800  # 30 minutes

while (time.time() - start_time) < max_wait_seconds:
    data = get_run_status()
    if data:
        status = data.get("status")
        conclusion = data.get("conclusion")
        elapsed = int(time.time() - start_time)
        
        jobs = get_run_jobs()
        current_step = ""
        if jobs:
            job = jobs[0]
            steps = job.get("steps", [])
            running_steps = [s["name"] for s in steps if s.get("status") == "in_progress"]
            if running_steps:
                current_step = f" -> Step: {running_steps[0]}"
            elif job.get("status") == "in_progress":
                completed_count = sum(1 for s in steps if s.get("status") == "completed")
                current_step = f" -> Steps: {completed_count}/{len(steps)}"
        
        print(f"[{elapsed:3d}s] Status: {status:<12} | Conclusion: {str(conclusion):<10}{current_step}", flush=True)
        
        if status == "completed":
            print("-" * 70, flush=True)
            if conclusion == "success":
                print("[SUCCESS] Build finished successfully! Fetching APK artifact...", flush=True)
                download_artifact(data)
                
                # Check for apk in output dir
                apks = [f for f in os.listdir(OUTPUT_DIR) if f.endswith(".apk")]
                print(f"[VERIFIED] Available APKs in {OUTPUT_DIR}:", flush=True)
                for apk in apks:
                    path = os.path.join(OUTPUT_DIR, apk)
                    size_mb = os.path.getsize(path) / (1024 * 1024)
                    print(f" -> {apk} ({size_mb:.2f} MB)", flush=True)
                sys.exit(0)
            else:
                print(f"[FAILED] Workflow concluded with status: {conclusion}", flush=True)
                sys.exit(1)
                
    time.sleep(12)

print("Timed out waiting for workflow completion.", flush=True)
sys.exit(2)
