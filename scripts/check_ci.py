import os
import urllib.request
import json
import time
import sys

token = os.environ.get("GITHUB_TOKEN", "") or (sys.argv[1] if len(sys.argv) > 1 else "")
repo = 'shivamgupt-2010/SHIV-OS'
commit_sha = sys.argv[2] if len(sys.argv) > 2 else '7d1beb6a0e8b42e3a16bbb2de6b8bf2e90e9bfd9'
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

print(f"Monitoring GitHub Actions for commit {commit_sha}...")
for i in range(40):
    runs = get_runs()
    if runs:
        run = runs[0]
        run_id = run['id']
        status = run['status']
        conclusion = run['conclusion']
        html_url = run['html_url']
        print(f"[{i+1}] Run ID: {run_id} | Status: {status} | Conclusion: {conclusion} | URL: {html_url}", flush=True)
        if status == 'completed':
            if conclusion == 'success':
                print("[SUCCESS] Build succeeded!", flush=True)
                # Check artifacts
                art_url = f'https://api.github.com/repos/{repo}/actions/runs/{run_id}/artifacts'
                req_art = urllib.request.Request(art_url, headers=headers)
                with urllib.request.urlopen(req_art) as resp_art:
                    art_data = json.loads(resp_art.read().decode('utf-8'))
                    artifacts = art_data.get('artifacts', [])
                    print(f"Artifacts found ({len(artifacts)}):", flush=True)
                    for a in artifacts:
                        print(f" - {a['name']} ({a['size_in_bytes']} bytes): {a['archive_download_url']}", flush=True)
                sys.exit(0)
            else:
                print(f"[FAILED] Build failed with conclusion: {conclusion}", flush=True)
                sys.exit(1)
    else:
        print(f"[{i+1}] Waiting for run to trigger...", flush=True)
    time.sleep(15)

print("Timed out waiting for run.")
sys.exit(2)
