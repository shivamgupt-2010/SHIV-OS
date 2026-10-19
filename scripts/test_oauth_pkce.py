import urllib.request
import urllib.parse
import json
import base64
import hashlib
import secrets
import re

BASE = 'https://shiv-database.onrender.com'
CLIENT_ID = 'shiv_client_1d07df81d86aa737330cd3e9'
REDIRECT_URI = 'shivlauncher://oauth/callback'

print("=== Step 1: Login to get Session Token ===")
login_data = json.dumps({'email_or_username': 'shreemanglam_admin', 'password': 'ShreeManglamSecure2026!'}).encode()
req = urllib.request.Request(f'{BASE}/auth/login', data=login_data, headers={'Content-Type': 'application/json'})
with urllib.request.urlopen(req) as resp:
    res = json.loads(resp.read().decode())
token = res['data']['access_token']
print(f"[OK] Logged in successfully. Token acquired: {token[:20]}...")

print("\n=== Step 2: Generate PKCE (RFC 7636) ===")
# 64 random bytes -> URL-safe base64 without padding
verifier_bytes = secrets.token_bytes(64)
code_verifier = base64.urlsafe_b64encode(verifier_bytes).decode('ascii').rstrip('=')
sha256 = hashlib.sha256(code_verifier.encode('ascii')).digest()
code_challenge = base64.urlsafe_b64encode(sha256).decode('ascii').rstrip('=')
oauth_state = secrets.token_urlsafe(16)

print(f"Code Verifier:  {code_verifier}")
print(f"Code Challenge: {code_challenge}")
print(f"OAuth State:    {oauth_state}")

print("\n=== Step 3: Call /oauth/authorize ===")
params = {
    'client_id': CLIENT_ID,
    'redirect_uri': REDIRECT_URI,
    'response_type': 'code',
    'scope': 'openid profile email offline_access',
    'code_challenge': code_challenge,
    'code_challenge_method': 'S256',
    'state': oauth_state,
    'session_token': token
}
auth_url = f'{BASE}/oauth/authorize?{urllib.parse.urlencode(params)}'

class NoRedirect(urllib.request.HTTPRedirectHandler):
    def redirect_request(self, req, fp, code, msg, headers, newurl):
        return None

opener = urllib.request.build_opener(NoRedirect)

redirect_url = None
try:
    resp = opener.open(auth_url)
    html_content = resp.read().decode('utf-8')
    print(f"Status: {resp.status}. Prompting user consent form...")
    # Extract consent_token from form
    match = re.search(r'name=["\']consent_token["\']\s+value=["\']([^"\']+)["\']', html_content)
    if not match:
        raise Exception("Could not find consent_token in HTML response")
    consent_token = match.group(1)
    print(f"Extracted Consent Token: {consent_token[:25]}...")
    
    # Approve consent
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
    
    creq = urllib.request.Request(f'{BASE}/oauth/authorize/consent', data=consent_form, headers={
        'Authorization': f'Bearer {token}',
        'Content-Type': 'application/x-www-form-urlencoded'
    })
    try:
        opener.open(creq)
    except urllib.error.HTTPError as he:
        if he.code in (302, 303, 307):
            redirect_url = he.headers.get('Location')
            print(f"[OK] Consent approved! Redirecting to: {redirect_url}")
        else:
            raise he

except urllib.error.HTTPError as e:
    if e.code in (302, 303, 307):
        redirect_url = e.headers.get('Location')
        print(f"[OK] Immediate Redirect to: {redirect_url}")
    else:
        print(f"[ERROR] {e.code}: {e.read().decode()}")

if not redirect_url:
    print("[FAIL] No redirect URL received!")
    exit(1)

parsed = urllib.parse.urlparse(redirect_url)
q_params = urllib.parse.parse_qs(parsed.query)
auth_code = q_params.get('code', [None])[0]
returned_state = q_params.get('state', [None])[0]

print(f"\nCaptured Authorization Code: {auth_code}")
print(f"Returned State:              {returned_state}")
assert returned_state == oauth_state, "OAuth State mismatch!"

print("\n=== Step 4: Exchange Authorization Code for Tokens (/oauth/token) ===")
token_payload = urllib.parse.urlencode({
    'grant_type': 'authorization_code',
    'code': auth_code,
    'redirect_uri': REDIRECT_URI,
    'client_id': CLIENT_ID,
    'code_verifier': code_verifier
}).encode('utf-8')

treq = urllib.request.Request(f'{BASE}/oauth/token', data=token_payload, headers={
    'Content-Type': 'application/x-www-form-urlencoded'
})

with urllib.request.urlopen(treq) as tresp:
    token_res = json.loads(tresp.read().decode('utf-8'))

print("\n=== Token Exchange Result ===")
print(f"Access Token:  {token_res.get('access_token', '')[:25]}...")
print(f"ID Token:      {token_res.get('id_token', '')[:25]}...")
print(f"Refresh Token: {token_res.get('refresh_token', '')[:25]}...")
print(f"Token Type:    {token_res.get('token_type')}")
print(f"Expires In:    {token_res.get('expires_in')} seconds")
print(f"Scope:         {token_res.get('scope')}")

print("\n=== Step 5: Verify User Info with Access Token (/oauth/userinfo) ===")
uireq = urllib.request.Request(f'{BASE}/oauth/userinfo', headers={
    'Authorization': f"Bearer {token_res['access_token']}"
})
with urllib.request.urlopen(uireq) as uiresp:
    user_info = json.loads(uiresp.read().decode('utf-8'))
print("UserInfo response:", json.dumps(user_info, indent=2))

print("\n[SUCCESS] 100% End-to-End PKCE OAuth Flow Verified with client_id=" + CLIENT_ID)
