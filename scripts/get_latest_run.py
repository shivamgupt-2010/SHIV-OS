import os
import sys
import json
import urllib.request

token = os.environ.get("GITHUB_TOKEN") or (sys.argv[1] if len(sys.argv) > 1 else "")
repo = 'shivamgupt-2010/SHIV-OS'
headers = {
    'Authorization': f'Bearer {token}',
    'Accept': 'application/vnd.github.v3+json',
    'User-Agent': 'SHIV-OS-Checker'
}

url = f'https://api.github.com/repos/{repo}/actions/runs'
req = urllib.request.Request(url, headers=headers)
try:
    with urllib.request.urlopen(req) as resp:
        data = json.loads(resp.read().decode('utf-8'))
        runs = data.get('workflow_runs', [])
        print(f"Total workflow runs: {len(runs)}")
        for r in runs[:6]:
            print("=" * 60)
            print(f"Run ID:     {r['id']}")
            print(f"Name:       {r['name']}")
            print(f"Event:      {r['event']}")
            print(f"Status:     {r['status']}")
            print(f"Conclusion: {r['conclusion']}")
            print(f"Commit:     {r['head_sha'][:8]} - {r['head_commit']['message'].splitlines()[0] if r.get('head_commit') else ''}")
            print(f"HTML URL:   {r['html_url']}")
            print(f"Created At: {r['created_at']}")
            print(f"Updated At: {r['updated_at']}")
except urllib.error.HTTPError as e:
    print(f"Error {e.code}: {e.read().decode('utf-8')}")
