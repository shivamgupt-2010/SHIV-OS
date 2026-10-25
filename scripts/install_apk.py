import subprocess
import time
import sys
import os

ADB_PATH = r"C:\Users\KRISHN GUPTA\AppData\Local\Microsoft\WinGet\Packages\Google.PlatformTools_Microsoft.Winget.Source_8wekyb3d8bbwe\platform-tools\adb.exe"
APK_PATH = r"F:\SHIV\SHIV-OS\app-debug.apk"
PACKAGE_NAME = "com.aistudio.shivai.vrmzkq"
ACTIVITY_NAME = "com.example.LauncherActivity"

print("==================================================================")
print("  SHIV LAUNCHER AUTOMATIC DEPLOYMENT & SCREENSHOT VERIFIER")
print("==================================================================")

def run_adb(args):
    cmd = [ADB_PATH] + args
    res = subprocess.run(cmd, capture_output=True, text=True)
    return res.stdout.strip()

print(f"Checking for connected Android device via ADB...")
devices_out = run_adb(["devices"])
print(devices_out)

lines = [line.strip() for line in devices_out.splitlines() if line.strip() and not line.startswith("*") and not line.startswith("List")]
device_serial = None
for line in lines:
    parts = line.split()
    if len(parts) >= 2 and parts[1] == "device":
        device_serial = parts[0]
        break

if not device_serial:
    print("\n[WAITING] No authorized device detected yet. Waiting for USB reconnection...")
    for i in range(15):
        time.sleep(2)
        devices_out = run_adb(["devices"])
        lines = [line.strip() for line in devices_out.splitlines() if line.strip() and not line.startswith("*") and not line.startswith("List")]
        for line in lines:
            parts = line.split()
            if len(parts) >= 2 and parts[1] == "device":
                device_serial = parts[0]
                break
        if device_serial:
            break
        print(f" . Waiting for phone ({15 - i} attempts left)...")

if not device_serial:
    print("\n[ERROR] Device not detected or unauthorized. Please ensure:")
    print(" 1. The USB cable is firmly plugged into the PC and phone.")
    print(" 2. USB Debugging is ENABLED in Developer Options.")
    print(" 3. The 'Allow USB debugging' prompt on the phone screen is accepted.")
    sys.exit(1)

print(f"\n[FOUND] Connected device: {device_serial}")
print(f"Installing {APK_PATH} (replacing existing package if present)...")

install_res = subprocess.run([ADB_PATH, "-s", device_serial, "install", "-r", "-d", APK_PATH], capture_output=True, text=True)
print(f"Install Output:\n{install_res.stdout}\n{install_res.stderr}")

if "Success" in install_res.stdout:
    print("\n[SUCCESS] APK installed successfully!")
    print(f"Launching {PACKAGE_NAME}...")
    launch_res = run_adb(["-s", device_serial, "shell", "am", "start", "-n", f"{PACKAGE_NAME}/.LauncherActivity"])
    print(f"Launch Result: {launch_res}")
    
    time.sleep(3)
    
    # Capture screenshot to verify
    screenshot_dir = r"C:\Users\KRISHN GUPTA\.gemini\antigravity\brain\4d567cee-9b80-49bf-b278-c8edbf722260"
    screenshot_path = os.path.join(screenshot_dir, "screen_verified_latest.png")
    run_adb(["-s", device_serial, "shell", "screencap", "-p", "/sdcard/screen_verified.png"])
    run_adb(["-s", device_serial, "pull", "/sdcard/screen_verified.png", screenshot_path])
    print(f"[VERIFIED] Screen captured to {screenshot_path}")
else:
    print("\n[FAIL] Installation failed. Output:\n", install_res.stderr or install_res.stdout)
