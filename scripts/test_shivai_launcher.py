#!/usr/bin/env python3
"""
ShivAI Launcher Integration Test Script.

Tests all 5 interaction methods against the live cloud instance:
https://shivai-backend.onrender.com
"""

import sys
import json
import time
import urllib.request
import urllib.error

BASE_URL = "https://shivai-backend.onrender.com/api/v1"
API_KEY = "shivai-test-client-key" # Replace with your Render SHIVAI_API_KEY if master key is required
USER_ID = "shivam"


def print_section(title: str):
    print("\n" + "=" * 60)
    print(f" {title}")
    print("=" * 60)


def test_health():
    print_section("0. Cloud Instance Health Check")
    url = f"{BASE_URL}/health"
    start = time.time()
    try:
        req = urllib.request.Request(url, headers={"User-Agent": "ShivAI-Launcher-Test"})
        with urllib.request.urlopen(req, timeout=15) as resp:
            data = json.loads(resp.read().decode())
            latency = (time.time() - start) * 1000
            print(f"[OK] Health Status: {data.get('status')} | Service: {data.get('service')} | Version: {data.get('version')}")
            print(f"[OK] Cloud Latency: {latency:.1f}ms")
            return True
    except Exception as e:
        print(f"[FAIL] Health check failed: {e}")
        return False


def test_chat_method_2_and_5():
    print_section("Method 2 & 5: Simple Chat Turn (HTTP POST /api/v1/chat)")
    url = f"{BASE_URL}/chat"
    payload = {
        "message": "Hello ShivAI! This is a test from SHIV-OS Launcher.",
        "user_id": USER_ID,
        "temperature": 0.7,
        "max_tokens": 512
    }
    headers = {
        "Content-Type": "application/json",
        "X-API-Key": API_KEY,
        "Authorization": f"Bearer {API_KEY}"
    }

    req = urllib.request.Request(url, data=json.dumps(payload).encode(), headers=headers, method="POST")
    try:
        with urllib.request.urlopen(req, timeout=30) as resp:
            res = json.loads(resp.read().decode())
            print(f"ShivAI: {res.get('content')}")
            print(f"Model: {res.get('model')} | Provider: {res.get('provider')} | Latency: {res.get('latency_ms', 0):.1f}ms")
    except urllib.error.HTTPError as e:
        body = e.read().decode()
        print(f"HTTP {e.code}: {body}")
        if e.code == 401:
            print("Note: Provide your Render SHIVAI_API_KEY in API_KEY or app Settings.")
    except Exception as e:
        print(f"Error: {e}")


def test_streaming_method_3():
    print_section("Method 3: Real-Time Streaming (SSE /api/v1/chat/stream)")
    url = f"{BASE_URL}/chat/stream"
    payload = {
        "message": "Give 2 quick tips for Android Launcher design in 2 bullets.",
        "user_id": USER_ID
    }
    headers = {
        "Content-Type": "application/json",
        "X-API-Key": API_KEY,
        "Authorization": f"Bearer {API_KEY}"
    }

    req = urllib.request.Request(url, data=json.dumps(payload).encode(), headers=headers, method="POST")
    try:
        with urllib.request.urlopen(req, timeout=45) as resp:
            print("ShivAI Stream: ", end="", flush=True)
            for raw_line in resp:
                line = raw_line.decode().strip()
                if not line or not line.startswith("data:"):
                    continue
                data_str = line[5:].strip()
                if data_str == "[DONE]":
                    break
                try:
                    chunk = json.loads(data_str)
                    delta = chunk.get("delta", "")
                    print(delta, end="", flush=True)
                except Exception:
                    pass
            print()
    except urllib.error.HTTPError as e:
        print(f"HTTP {e.code}: {e.read().decode()}")
    except Exception as e:
        print(f"Error: {e}")


def test_memory_method_4():
    print_section("Method 4: Long-Term Memory (Persistent Engine)")
    # 1. Remember
    url = f"{BASE_URL}/memory"
    payload = {
        "key": "coding_style",
        "value": "Shivam prefers Python with strict type annotations and Kotlin Jetpack Compose for Android.",
        "category": "preference",
        "importance": 0.9
    }
    headers = {
        "Content-Type": "application/json",
        "X-API-Key": API_KEY,
        "Authorization": f"Bearer {API_KEY}"
    }

    print("1. Remembering preference via POST /api/v1/memory...")
    req = urllib.request.Request(url, data=json.dumps(payload).encode(), headers=headers, method="POST")
    try:
        with urllib.request.urlopen(req, timeout=15) as resp:
            data = json.loads(resp.read().decode())
            print(f"[OK] Remembered: key='{data.get('key')}', id='{data.get('id')}'")
    except urllib.error.HTTPError as e:
        print(f"HTTP {e.code}: {e.read().decode()}")
    except Exception as e:
        print(f"Error: {e}")


if __name__ == "__main__":
    print("Testing SHIV-OS Cloud Integration against:")
    print(f"Endpoint: {BASE_URL}")
    test_health()
    test_chat_method_2_and_5()
    test_streaming_method_3()
    test_memory_method_4()
