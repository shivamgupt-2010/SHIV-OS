import os
import urllib.request
import json
import time
import sys

token = os.environ.get("GITHUB_TOKEN", "")
repo = 'shivamgupt-2010/SHIV-OS'
commit_sha = '42123c06a942e00fff9d0af2332761dbb1c4c88a'
headers = {
    'Authorization': f'Bearer {token}',
    'Accept': 'application/vnd.github.v3+json',
    'User-Agent': 'SHIV-OS-Checker'
}

def get_runs():
    url = f'https://api.github.com/repos/{repo}/actions/runs?head_sha={commit_sha}'
    req = urllib.request.Request(url, headers=headers)
    with urllib.request.urlopen(req) as resp:
        data = json.loads(resp.read().decode('utf-8'))
        return data.get('workflow_runs', [])

for i in range(30):
    runs = get_runs()
    if runs:
        run = runs[0]
        run_id = run['id']
        status = run['status']
        conclusion = run['conclusion']
        print(f"[{i+1}] Run ID: {run_id} | Status: {status} | Conclusion: {conclusion}")
        if status == 'completed':
            if conclusion == 'success':
                print("[SUCCESS] Build succeeded!")
                sys.exit(0)
            else:
                print(f"[FAILED] Build failed with conclusion: {conclusion}")
                sys.exit(1)
    else:
        print(f"[{i+1}] Waiting for run to trigger...")
    time.sleep(15)

print("Timed out waiting for run.")
sys.exit(2)
