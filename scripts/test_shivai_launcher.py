#!/usr/bin/env python3
"""
ShivAI Launcher Ecosystem Integration Test Script.

Tests all Core interaction methods against the live cloud instance:
https://real-shivai.onrender.com
"""

import sys
import json
import time
import urllib.request
import urllib.error

BASE_URL = "https://real-shivai.onrender.com"
AUTH_KEY = "shivai_launcher_auth_key_v1"

def print_section(title: str):
    print("\n" + "=" * 60)
    print(f" {title}")
    print("=" * 60)

def call(path: str, data=None):
    headers = {
        "Content-Type": "application/json",
        "Authorization": f"Bearer {AUTH_KEY}"
    }
    is_get = data is None and path in ["/api/health", "/api/system/state", "/api/system/traces", "/sdk/latest"]
    method = "GET" if is_get else "POST"
    req_data = json.dumps(data).encode("utf-8") if data is not None else (None if is_get else b"{}")
    req = urllib.request.Request(f"{BASE_URL}{path}", data=req_data, headers=headers, method=method)
    start = time.time()
    try:
        with urllib.request.urlopen(req, timeout=15) as resp:
            latency = (time.time() - start) * 1000
            return resp.status, json.loads(resp.read().decode()), latency
    except urllib.error.HTTPError as e:
        latency = (time.time() - start) * 1000
        return e.code, e.read().decode(), latency
    except Exception as e:
        latency = (time.time() - start) * 1000
        return 0, str(e), latency

def run_all_tests():
    print(f"Testing ShivAI Cognitive Core at {BASE_URL}...")
    
    print_section("1. Diagnostics & Health Check (/api/health)")
    status, res, latency = call("/api/health")
    print(f"[{'OK' if status == 200 else 'FAIL'}] Status: {status} | Latency: {latency:.1f}ms | Response: {res}")
    
    print_section("2. System State Check (/api/system/state)")
    status, res, latency = call("/api/system/state")
    print(f"[{'OK' if status == 200 else 'FAIL'}] Status: {status} | Latency: {latency:.1f}ms | Active State: {res.get('status') if isinstance(res, dict) else res}")
    
    print_section("3. Identity Engine (/api/identity/current)")
    status, res, latency = call("/api/identity/current", {})
    print(f"[{'OK' if status == 200 else 'FAIL'}] Status: {status} | Current User: {res}")
    
    print_section("4. Cognitive Memory Store & Retrieve (/api/memory/*)")
    s1, r1, _ = call("/api/memory/store", {"key": "launcher_test_pref", "data": {"theme": "cosmic", "grid": 5}})
    print(f"Store Status: {s1} -> {r1}")
    s2, r2, _ = call("/api/memory/retrieve", {"key": "launcher_test_pref"})
    print(f"Retrieve Status: {s2} -> {r2}")
    
    print_section("5. Reasoning Core & Plan Intent (/api/reasoning/plan)")
    status, res, latency = call("/api/reasoning/plan", {"intent": "Open camera and take photo", "context": {}})
    print(f"[{'OK' if status == 200 else 'FAIL'}] Status: {status} | Latency: {latency:.1f}ms")
    if isinstance(res, dict) and "plan" in res:
        plan = res["plan"]
        print(f"Strategy: {plan.get('strategy')} | Confidence: {plan.get('confidence')}")

    print_section("6. Voice Engine Wake (/api/voice/wake)")
    status, res, latency = call("/api/voice/wake", {})
    print(f"[{'OK' if status == 200 else 'FAIL'}] Status: {status} | Voice Engine: {res}")

    print_section("7. Analytics Event Ingestion (/api/analytics/events)")
    status, res, latency = call("/api/analytics/events", {"event": {"type": "LAUNCHER_TEST", "payload": {"platform": "android"}}})
    print(f"[{'OK' if status == 200 else 'FAIL'}] Status: {status} | Logged: {res}")

if __name__ == "__main__":
    run_all_tests()
