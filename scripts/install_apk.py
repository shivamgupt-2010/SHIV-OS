import zipfile
import os
import subprocess
import time

dest_dir = r"F:\SHIV\SHIV-OS"
zip_path = os.path.join(dest_dir, "app-debug-artifact.zip")
print("Extracting", zip_path)

with zipfile.ZipFile(zip_path, "r") as z:
    z.extractall(dest_dir)

apk_path = os.path.join(dest_dir, "app-debug.apk")
print("Extracted APK:", apk_path, "Size:", os.path.getsize(apk_path))

# ADB install
print("Installing to device...")
res = subprocess.getoutput(f'adb install -r "{apk_path}"')
print("Install result:", res)

# Launch
print("Launching SHIV-OS Launcher...")
launch = subprocess.getoutput("adb shell am start -n com.aistudio.shivai.vrmzkq/com.example.LauncherActivity")
print("Launch result:", launch)

time.sleep(2)

# Screencap
print("Capturing live screenshot...")
subprocess.getoutput("adb shell screencap -p /sdcard/redesigned_screen.png")
subprocess.getoutput(r'adb pull /sdcard/redesigned_screen.png "C:\Users\KRISHN GUPTA\.gemini\antigravity\brain\4d567cee-9b80-49bf-b278-c8edbf722260\redesigned_screen.png"')
print("[SUCCESS] Screenshot saved!")
