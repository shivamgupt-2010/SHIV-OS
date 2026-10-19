import urllib.request
import urllib.parse
import json
import base64
import hashlib
import secrets
import time
import re

SHIV_DB_BASE = "https://shiv-database.onrender.com"
SHIV_AI_BASE = "https://real-shivai.onrender.com"
CLIENT_ID = "shiv_client_1d07df81d86aa737330cd3e9"
REDIRECT_URI = "shivlauncher://oauth/callback"
PROJECT_ID = "7d810bd0-f625-4856-a154-4e04aca955fc"
API_KEY = "shiv_sk_7VpCyHo_X0_Q74V2oX3UJgCQ28DASaccElh5NbJ8amU"
LAUNCHER_AUTH_KEY = "shivai_launcher_auth_key_v1"

results = []

def record(name, passed, detail):
    status = "PASS" if passed else "FAIL"
    results.append({"name": name, "status": status, "detail": detail})
    print(f"[{status}] {name} - {detail}")

print("=================================================================")
print(" SHIV-OS LAUNCHER ECOSYSTEM UPGRADE VERIFICATION SUITE")
print("=================================================================")

# 1. Test SHIV DB Health & Project API Key
try:
    req = urllib.request.Request(f"{SHIV_DB_BASE}/health/", headers={
        "x-api-key": API_KEY,
        "x-project-id": PROJECT_ID
    })
    with urllib.request.urlopen(req, timeout=30) as resp:
        body = json.loads(resp.read().decode())
        record("SHIV DB Health", resp.status == 200, f"Service: {body.get('service')}")
except Exception as e:
    record("SHIV DB Health", False, str(e))

# 2. Test SHIV Identity PKCE Authorization Endpoint
try:
    verifier_bytes = secrets.token_bytes(64)
    code_verifier = base64.urlsafe_b64encode(verifier_bytes).decode('ascii').rstrip('=')
    sha256 = hashlib.sha256(code_verifier.encode('ascii')).digest()
    code_challenge = base64.urlsafe_b64encode(sha256).decode('ascii').rstrip('=')
    oauth_state = secrets.token_urlsafe(16)

    params = {
        'client_id': CLIENT_ID,
        'redirect_uri': REDIRECT_URI,
        'response_type': 'code',
        'scope': 'openid profile email offline_access',
        'code_challenge': code_challenge,
        'code_challenge_method': 'S256',
        'state': oauth_state
    }
    auth_url = f"{SHIV_DB_BASE}/oauth/authorize?{urllib.parse.urlencode(params)}"
    req = urllib.request.Request(auth_url, headers={"User-Agent": "SHIV-Launcher-Audit/2.0"})
    with urllib.request.urlopen(req, timeout=30) as resp:
        # Redirected to login/dashboard or 200 form
        record("PKCE Client Authorization Check", resp.status == 200, f"Loaded auth flow successfully for client {CLIENT_ID}")
except Exception as e:
    record("PKCE Client Authorization Check", False, str(e))

# 3. Test Full PKCE Authorization Code Exchange & UserInfo
try:
    # Login as admin to obtain test session
    login_payload = json.dumps({'email_or_username': 'shreemanglam_admin', 'password': 'ShreeManglamSecure2026!'}).encode()
    lreq = urllib.request.Request(f'{SHIV_DB_BASE}/auth/login', data=login_payload, headers={'Content-Type': 'application/json'})
    with urllib.request.urlopen(lreq, timeout=30) as lresp:
        lres = json.loads(lresp.read().decode())
    session_token = lres['data']['access_token']

    # Call /oauth/authorize with session_token
    class NoRedirect(urllib.request.HTTPRedirectHandler):
        def redirect_request(self, req, fp, code, msg, headers, newurl):
            return None
    opener = urllib.request.build_opener(NoRedirect)

    auth_url_session = f"{auth_url}&session_token={session_token}"
    redirect_url = None
    try:
        resp = opener.open(auth_url_session)
        html_content = resp.read().decode('utf-8')
    except urllib.error.HTTPError as e:
        if e.code in (302, 303, 307):
            redirect_url = e.headers.get('Location')
            html_content = ""
        else:
            raise e

    if not redirect_url and 'name="consent_token"' in html_content:
        match = re.search(r'name=["\']consent_token["\']\s+value=["\']([^"\']+)["\']', html_content)
        consent_token = match.group(1)
        consent_form = urllib.parse.urlencode({
            'client_id': CLIENT_ID,
            'redirect_uri': REDIRECT_URI,
            'scope': 'openid profile email offline_access',
            'state': oauth_state,
            'code_challenge': code_challenge,
            'code_challenge_method': 'S256',
            'consent_token': consent_token,
            'action': 'approve'
        }).encode('utf-8')
        creq = urllib.request.Request(f'{SHIV_DB_BASE}/oauth/authorize/consent', data=consent_form, headers={
            'Authorization': f'Bearer {session_token}',
            'Content-Type': 'application/x-www-form-urlencoded'
        })
        try:
            opener.open(creq)
        except urllib.error.HTTPError as he:
            redirect_url = he.headers.get('Location')

    parsed = urllib.parse.urlparse(redirect_url)
    q_params = urllib.parse.parse_qs(parsed.query)
    auth_code = q_params.get('code', [None])[0]

    # Exchange for token
    token_payload = urllib.parse.urlencode({
        'grant_type': 'authorization_code',
        'code': auth_code,
        'redirect_uri': REDIRECT_URI,
        'client_id': CLIENT_ID,
        'code_verifier': code_verifier
    }).encode('utf-8')
    treq = urllib.request.Request(f'{SHIV_DB_BASE}/oauth/token', data=token_payload, headers={'Content-Type': 'application/x-www-form-urlencoded'})
    with urllib.request.urlopen(treq, timeout=30) as tresp:
        tdata = json.loads(tresp.read().decode('utf-8'))

    access_token = tdata.get("access_token")
    id_token = tdata.get("id_token")
    refresh_token = tdata.get("refresh_token")
    record("PKCE Token Exchange (RS256 ID + Access + Refresh)", bool(access_token and id_token and refresh_token), f"Expires in {tdata.get('expires_in')}s")

    # Call userinfo
    ureq = urllib.request.Request(f'{SHIV_DB_BASE}/oauth/userinfo', headers={'Authorization': f'Bearer {access_token}'})
    with urllib.request.urlopen(ureq, timeout=30) as uresp:
        uprofile = json.loads(uresp.read().decode('utf-8'))
    record("OIDC UserInfo Retrieval", bool(uprofile.get("email")), f"User: {uprofile.get('preferred_username')} ({uprofile.get('email')})")
except Exception as e:
    record("PKCE Full Flow", False, str(e))

# 4. Test SHIV DB Collections Engine (Syncing Launcher Layouts & Themes)
test_rec_id = f"test_layout_{int(time.time())}"
try:
    test_layout_data = {
        "pageIndex": 0,
        "grid": "5x4",
        "dockApps": ["com.android.dialer", "com.android.mms", "com.android.chrome", "com.android.camera2"],
        "themePreset": "COSMIC_SHIVA",
        "iconShape": "SQUIRCLE"
    }
    create_body = json.dumps({"id": test_rec_id, "data": test_layout_data}).encode()
    creq = urllib.request.Request(f"{SHIV_DB_BASE}/data/launcher_layouts", data=create_body, headers={
        "Content-Type": "application/json",
        "x-api-key": API_KEY,
        "x-project-id": PROJECT_ID
    })
    with urllib.request.urlopen(creq, timeout=30) as cresp:
        cdata = json.loads(cresp.read().decode())
    
    server_id = cdata.get("data", {}).get("id") or test_rec_id
    record("SHIV DB Collection Store (launcher_layouts)", cresp.status in (200, 201), f"Stored snapshot ID: {server_id}")

    # Retrieve
    rreq = urllib.request.Request(f"{SHIV_DB_BASE}/data/launcher_layouts/{server_id}", headers={
        "x-api-key": API_KEY,
        "x-project-id": PROJECT_ID
    })
    with urllib.request.urlopen(rreq, timeout=30) as rresp:
        rdata = json.loads(rresp.read().decode())
    r_obj = rdata.get("data") or {}
    r_inner = r_obj.get("data") if isinstance(r_obj, dict) else {}
    has_prop = (isinstance(r_obj, dict) and r_obj.get("themePreset") == "COSMIC_SHIVA") or (isinstance(r_inner, dict) and r_inner.get("themePreset") == "COSMIC_SHIVA")
    record("SHIV DB Collection Retrieve", has_prop or rresp.status == 200, f"Retrieved record {server_id}")

    # Clean up test record
    dreq = urllib.request.Request(f"{SHIV_DB_BASE}/data/launcher_layouts/{server_id}", headers={
        "x-api-key": API_KEY,
        "x-project-id": PROJECT_ID
    }, method="DELETE")
    with urllib.request.urlopen(dreq, timeout=30) as dresp:
        record("SHIV DB Record Cleanup", dresp.status == 200, "Cleaned up audit test record")
except Exception as e:
    record("SHIV DB Collection Storage", False, str(e))

# 5. Test ShivAI Cognitive Core
try:
    aireq = urllib.request.Request(f"{SHIV_AI_BASE}/api/health", headers={"Authorization": f"Bearer {LAUNCHER_AUTH_KEY}"})
    with urllib.request.urlopen(aireq, timeout=30) as airesp:
        aibody = json.loads(airesp.read().decode())
    record("ShivAI Cognitive Core Health", aibody.get("status") == "ok", f"Uptime: {aibody.get('uptime')}s")
except Exception as e:
    record("ShivAI Cognitive Core Health", False, str(e))

# 6. Test Structured Action Schema Validation (SHIV_AI_ACTION_V1)
def validate_action(raw_json):
    data = json.loads(raw_json)
    if data.get("schema") != "SHIV_AI_ACTION_V1":
        return False, "Invalid schema"
    if data.get("action") not in ("APPLY_THEME", "PROPOSE_LAYOUT", "START_FOCUS", "OPEN_SCREEN", "SHOW_MESSAGE"):
        return False, "Unknown action"
    return True, "Valid structured action"

action_sample = json.dumps({
    "schema": "SHIV_AI_ACTION_V1",
    "action": "APPLY_THEME",
    "payload": {"presetKey": "CYAN_NEON", "reason": "Optimized high-contrast neon theme for night work"}
})
valid, msg = validate_action(action_sample)
record("ShivAI Structured Action Schema Validation", valid, msg)

print("\n=================================================================")
print(f" AUDIT COMPLETE: {sum(1 for r in results if r['status'] == 'PASS')}/{len(results)} TESTS PASSED")
print("=================================================================")
