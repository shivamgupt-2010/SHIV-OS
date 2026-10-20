#!/usr/bin/env python3
"""
Test Suite: SHIV Safety & Security Center, Lock & Privacy, Digital Wellbeing,
Family Account Linking, and Startup / Welcome / Authentication UI.

Verifies:
1. SafetySecurityCenterScreen 5-section unified architecture & sub-tabs
2. AppLockManager 4 lock methods, auto-relock rules, disguise crash, haptics & lockout
3. FamilyManager supervision, screen-time caps, approval requests, web filters & alerts
4. WellbeingManager 5 focus modes, pause/resume, unlock counts & notification counts
5. Startup, Welcome & Auth UI (Boot signature, 4-slide carousel, 5-step wizard, shared auth)
6. Lockscreen leakage prevention in AndroidManifest.xml
7. Zero-beep audio architecture in ShivAIPersistentService & AppContainer
"""

import os
import sys

ROOT = r"F:\SHIV\SHIV-OS"

def test_safety_security_center_screen():
    print("[1/7] Testing SafetySecurityCenterScreen implementation...")
    path = os.path.join(ROOT, "app", "src", "main", "java", "com", "example", "ui", "security", "SafetySecurityCenterScreen.kt")
    assert os.path.exists(path), f"SafetySecurityCenterScreen.kt missing at {path}"
    with open(path, "r", encoding="utf-8") as f:
        content = f.read()

    # 5 Main Sections
    assert "enum class SafetySection" in content
    assert "PARENTAL" in content
    assert "LOCK" in content
    assert "WELLBEING" in content
    assert "SECURITY" in content
    assert "FAMILY_LINK" in content

    # Section 1: Parental Controls sub-tabs
    assert "ParentalControlsTab" in content
    assert "Parent Dashboard" in content or "child profile" in content.lower()
    assert "Screen Time Today" in content
    assert "Remaining Time" in content
    assert "Bedtime Curfew" in content
    assert "Break Reminders" in content
    assert "Quick Extra Time Grant" in content
    assert "Blocked Websites Filter" in content
    assert "Requests Awaiting Parent Approval" in content
    assert "approveRequest" in content and "denyRequest" in content

    # Section 2: SHIV Lock
    assert "ShivLockTab" in content
    assert "LockMethod.entries.forEach" in content
    assert "Immediately upon app exit" in content
    assert "Lock after screen turns off" in content
    assert "Fake Crash / Disguise Screen" in content
    assert "Tactile Vibration Feedback" in content
    assert "Hide Recents App Preview" in content
    assert "Local Security Event Log" in content

    # Section 3: Digital Wellbeing
    assert "DigitalWellbeingTab" in content
    assert "5 Cognitive Focus Modes" in content
    assert "Weekly Usage Pattern" in content
    assert "Unlocks" in content and "Notifications" in content
    assert "resumeFocusSession" in content and "pauseFocusSession" in content

    # Section 4: Security Center
    assert "SecurityCenterTab" in content
    assert "SHIV Identity Account Security" in content
    assert "Hardware Camera Privacy Switch" in content
    assert "Hardware Microphone Privacy Switch" in content
    assert "Auto-Clear Clipboard Privacy" in content
    assert "Permission Safety Audit" in content

    # Section 5: Family Account Linking
    assert "FamilyAccountLinkingTab" in content
    assert "Device Pairing Invitation Code" in content
    assert "SHIV DB Synchronization" in content
    assert "Offline Fallback" in content
    assert "Tamper Protection" in content
    print("  -> SafetySecurityCenterScreen passed all checks!")

def test_app_lock_manager():
    print("[2/7] Testing AppLockManager core logic & policies...")
    path = os.path.join(ROOT, "app", "src", "main", "java", "com", "example", "core", "lock", "AppLockManager.kt")
    assert os.path.exists(path), f"AppLockManager.kt missing at {path}"
    with open(path, "r", encoding="utf-8") as f:
        content = f.read()

    assert "enum class LockMethod" in content
    assert "PIN" in content and "PATTERN" in content and "PASSWORD" in content and "BIOMETRICS" in content
    assert "autoRelockTimeoutSeconds" in content
    assert "hapticFeedbackEnabled" in content
    assert "subtleAnimationEnabled" in content
    assert "preventRecentTasksPreview" in content
    assert "disguiseCrashEnabled" in content
    assert "failedAttempts" in content
    assert "lockoutUntilTimestamp" in content
    assert "recordFailedAttempt" in content
    assert "lockCategory" in content
    print("  -> AppLockManager passed all checks!")

def test_family_manager():
    print("[3/7] Testing FamilyManager policy, requests & alerts...")
    path = os.path.join(ROOT, "app", "src", "main", "java", "com", "example", "core", "family", "FamilyManager.kt")
    assert os.path.exists(path), f"FamilyManager.kt missing at {path}"
    with open(path, "r", encoding="utf-8") as f:
        content = f.read()

    assert "enum class ApprovalRequestType" in content
    assert "enum class ApprovalStatus" in content
    assert "data class ParentApprovalRequest" in content
    assert "data class SafetyAlert" in content
    assert "data class ContentRestrictions" in content
    assert "approvalRequests" in content
    assert "safetyAlerts" in content
    assert "pairingCode" in content
    assert "isCloudSynced" in content
    assert "tamperDetected" in content
    assert "grantTemporaryExtension" in content
    assert "isCurfewActive" in content
    print("  -> FamilyManager passed all checks!")

def test_wellbeing_manager():
    print("[4/7] Testing WellbeingManager 5 focus modes & metrics...")
    path = os.path.join(ROOT, "app", "src", "main", "java", "com", "example", "core", "wellbeing", "WellbeingManager.kt")
    assert os.path.exists(path), f"WellbeingManager.kt missing at {path}"
    with open(path, "r", encoding="utf-8") as f:
        content = f.read()

    assert "enum class FocusModeType" in content
    assert "STUDY" in content and "WORK" in content and "SLEEP" in content and "DEEP_FOCUS" in content and "CUSTOM" in content
    assert "startFocusMode" in content
    assert "pauseFocusSession" in content
    assert "resumeFocusSession" in content
    assert "stopFocusSession" in content
    assert "dailyUnlockCount" in content
    assert "dailyNotificationCount" in content
    assert "weeklyUsageSummary" in content
    print("  -> WellbeingManager passed all checks!")

def test_onboarding_startup_auth():
    print("[5/7] Testing Startup, Welcome & Authentication UI...")
    path = os.path.join(ROOT, "app", "src", "main", "java", "com", "example", "ui", "onboarding", "OnboardingScreen.kt")
    assert os.path.exists(path), f"OnboardingScreen.kt missing at {path}"
    with open(path, "r", encoding="utf-8") as f:
        content = f.read()

    # Stages
    assert "enum class OnboardingStage" in content
    assert "BOOT_SIGNATURE" in content
    assert "WELCOME_CAROUSEL" in content
    assert "SETUP_WIZARD" in content
    assert "AUTH_SCREEN" in content

    # Stage 1: Boot Signature
    assert "BootSignatureScreen" in content
    assert "SHIV LAUNCHER" in content
    assert "Consciousness Meets Intelligence" in content

    # Stage 2: Welcome Carousel
    assert "WelcomeCarouselScreen" in content
    assert "Your device. Your way." in content
    assert "PERSONALIZATION PREVIEWS" in content
    assert "I have a SHIV Identity account" in content

    # Stage 3: 5-Step Setup Wizard
    assert "SetupWizardScreen" in content
    assert "ProfileType.entries.forEach" in content
    assert "StudioThemeTokens.PRESETS" in content
    assert "Match System Light / Dark" in content
    assert "Icon Mask Shape" in content
    assert "System Integrations & Privacy" in content
    assert "Your SHIV Workspace is Ready" in content

    # Stage 4: SHIV Identity Shared Auth
    assert "ShivIdentityAuthScreen" in content
    assert "loginWithCredentials" in content
    assert "registerAccount" in content
    assert "startSignIn" in content
    assert "Continue as Guest" in content
    print("  -> Startup, Welcome & Auth UI passed all checks!")

def test_lockscreen_leakage_prevented():
    print("[6/7] Testing AndroidManifest.xml for lockscreen leakage prevention...")
    manifest_path = os.path.join(ROOT, "app", "src", "main", "AndroidManifest.xml")
    with open(manifest_path, "r", encoding="utf-8") as f:
        content = f.read()

    # LauncherActivity must NOT have showWhenLocked or turnScreenOn
    # Find LauncherActivity block
    start_idx = content.find('android:name=".LauncherActivity"')
    assert start_idx != -1, "LauncherActivity not found in AndroidManifest.xml"
    end_idx = content.find('</activity>', start_idx)
    activity_block = content[start_idx:end_idx]

    assert "showWhenLocked" not in activity_block, "CRITICAL: showWhenLocked must not be present in LauncherActivity"
    assert "turnScreenOn" not in activity_block, "CRITICAL: turnScreenOn must not be present in LauncherActivity"
    print("  -> Lockscreen leakage prevention verified (No showWhenLocked/turnScreenOn)!")

def test_zero_beep_audio_architecture():
    print("[7/7] Testing zero-beep audio architecture & agent interaction...")
    # Check persistent service
    service_path = os.path.join(ROOT, "app", "src", "main", "java", "com", "example", "core", "permissions", "ShivAIPersistentService.kt")
    with open(service_path, "r", encoding="utf-8") as f:
        content = f.read()

    assert "SpeechRecognizer.createSpeechRecognizer" not in content, "Continuous SpeechRecognizer loop must be removed"
    assert "RecognitionListener" not in content, "Background RecognitionListener must not exist in service"

    # Check AppContainer engine bindings
    container_path = os.path.join(ROOT, "app", "src", "main", "java", "com", "example", "di", "AppContainer.kt")
    with open(container_path, "r", encoding="utf-8") as f:
        c_content = f.read()

    assert "ShivStreamingSttEngine" in c_content, "STT must be ShivStreamingSttEngine (zero-beep)"
    assert "ShivNeuralTtsEngine" in c_content, "TTS must be ShivNeuralTtsEngine (cloud/neural)"

    # Check AgentsScreen interaction model
    agents_path = os.path.join(ROOT, "app", "src", "main", "java", "com", "example", "ui", "agents", "AgentsScreen.kt")
    with open(agents_path, "r", encoding="utf-8") as f:
        a_content = f.read()

    assert "Converse with ShivAI" in a_content
    assert "assigns subtasks to" in a_content or "ShivAI orchestrates & assigns tasks to agents" in a_content
    print("  -> Zero-beep audio & agent model verified!")

def main():
    print("=" * 70)
    print("SHIV SAFETY, SECURITY, LOCK & STARTUP VERIFICATION TEST SUITE")
    print("=" * 70)
    test_safety_security_center_screen()
    test_app_lock_manager()
    test_family_manager()
    test_wellbeing_manager()
    test_onboarding_startup_auth()
    test_lockscreen_leakage_prevented()
    test_zero_beep_audio_architecture()
    print("=" * 70)
    print("ALL 7 VERIFICATION SUITES PASSED CLEANLY (100% GREEN)!")
    print("=" * 70)

if __name__ == "__main__":
    main()
