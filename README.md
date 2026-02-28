# NFC PoC — Android NFC Vulnerability Research App

> ⚠️ **LEGAL DISCLAIMER**: This tool is intended **exclusively for authorized security research and penetration testing**. You must have explicit written authorization from the system owner before testing any NFC-based access control, transit, or payment system. Unauthorized interception or cloning of NFC cards is illegal in most jurisdictions. The developer assumes no liability for misuse.

---

## Overview

A production-ready Android application (Kotlin) for NFC security research demonstrating:
- **Card Reading**: MIFARE Classic, MIFARE Ultralight, ISO 14443-4 (A/B), NFC-F (FeliCa)
- **Full Dump Storage**: All sector/page/APDU data persisted via Room database
- **HCE Emulation**: Replay ISO 14443-4 cards against payment terminals and transit readers using Android Host Card Emulation

**Target Device**: OnePlus phone with OxygenOS 16 (NXP NFC chipset — fully compatible)

---

## Architecture

```
┌──────────────────────────────────────────────────────────┐
│                        UI Layer                          │
│  ScanFragment → CardListFragment → ReplayFragment        │
│  CardDetailActivity                                      │
└──────────────┬───────────────────────────┬───────────────┘
               │                           │
     ┌─────────▼──────────┐   ┌───────────▼───────────────┐
     │  NfcReaderManager  │   │  EmulationSessionManager  │
     │  ├ MifareClassic   │   │  + ApduRouter (state mach)│
     │  ├ MifareUltralight│   │  + HceEmulationService    │
     │  ├ IsoDep (EMV)    │   └───────────────────────────┘
     │  ├ NfcB            │
     │  └ NfcF (FeliCa)   │
     └─────────┬──────────┘
               │
     ┌─────────▼──────────┐
     │     Data Layer     │
     │  CardRepository    │
     │  Room DB (NfcCard) │
     └────────────────────┘
```

---

## Card Support Matrix

| Card Type | Read | HCE Emulation |
|-----------|------|---------------|
| MIFARE Classic 1K/2K/4K | ✅ Full sector dump + key brute-force | ❌ Not supported by Android HCE |
| MIFARE Ultralight / C | ✅ All pages | ❌ Not supported by Android HCE |
| ISO 14443-4A (payment/transit) | ✅ Full EMV APDU flow | ✅ **Supported** |
| ISO 14443-4B | ✅ ATQB + IsoDep | ✅ **Supported** |
| NFC-F (FeliCa) | ✅ IDm/PMm/SystemCode + service reads | ❌ Not supported by Android HCE |

> **HCE Note**: Android HCE only supports ISO 14443-4 (IsoDep) emulation at the OS level. MIFARE Classic/Ultralight/FeliCa dumps are stored and can be used with external hardware (PN532, ACR122U) for emulation.

---

## Prerequisites

- **Android Studio** Hedgehog (2023.1.1) or later
- **Android SDK**: API 26+ (Android 8.0+), Target API 35
- **Java/Kotlin**: JDK 17
- **OnePlus Device** with OxygenOS 14+ (NFC enabled in settings)
- **NFC enabled** on the device (Settings → NFC)
- **Default payment app**: The HCE service competes with other payment apps; set NFC PoC as preferred or use the "Other" category reader

---

## Build Instructions

### 1. Open in Android Studio

```
File → Open → e:\Learning\Python\mobil\nfc_poc
```

Wait for Gradle sync to complete (~2–3 minutes on first run).

### 2. Configure SDK (if needed)

```
File → Project Structure → SDK Location → set Android SDK path
```

### 3. Build Debug APK

**Via IDE**: Click 🔨 Build → Make Project  
**Via Terminal**:
```powershell
cd e:\Learning\Python\mobil\nfc_poc
.\gradlew assembleDebug
```
Output APK: `app\build\outputs\apk\debug\app-debug.apk`

### 4. Build Release APK

#### 4a. Generate a Signing Key (first time only)

```
Build → Generate Signed Bundle/APK → APK → Create new keystore
```

Or via CLI:
```powershell
keytool -genkey -v -keystore nfc_poc_key.jks -keyalg RSA -keysize 2048 -validity 10000 -alias nfc_poc
```

#### 4b. Add signing config to `app/build.gradle.kts`

```kotlin
android {
    signingConfigs {
        create("release") {
            storeFile = file("../nfc_poc_key.jks")
            storePassword = "YOUR_STORE_PASSWORD"
            keyAlias = "nfc_poc"
            keyPassword = "YOUR_KEY_PASSWORD"
        }
    }
    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            // ... existing config
        }
    }
}
```

#### 4c. Build Release

```powershell
.\gradlew assembleRelease
```
Output APK: `app\build\outputs\apk\release\app-release.apk`

---

## Deploy to Device

### Via USB (recommended)

```powershell
# Enable USB Debugging: Settings → Developer Options → USB Debugging
adb install app\build\outputs\apk\debug\app-debug.apk

# Or use Gradle:
.\gradlew installDebug
```

### Via Android Studio

Select your device in the device dropdown → Click ▶ Run

### Via File Transfer

1. Copy `app-debug.apk` to the device
2. Enable "Install from Unknown Sources" in Settings
3. Open the APK on the device

---

## Usage Guide

### Step 1: Scan a Card

1. Open the app → **Scan** tab
2. Hold an NFC card to the **back** of your OnePlus phone (NFC antenna location)
3. The app reads the card automatically:
   - **MIFARE Classic**: Shows sector read progress (with % success)
   - **ISO-DEP cards**: Shows AID and APDU exchange count
4. Tap **Save Card** → optionally enter a label → **Save**

### Step 2: View Card Details

1. Go to **Cards** tab
2. Tap any saved card
3. View full hex dump, APDU log, tech-specific fields
4. Tap **Export** to share as text (for analysis)
5. For ISO-DEP cards: tap **Load for Replay**

### Step 3: Replay (Emulate)

1. Go to **Replay** tab
2. If a card is loaded, tap **Start Emulation**
3. Hold the phone to a compatible reader
4. The HCE service responds automatically to APDU commands
5. Tap **Stop Emulation** when done

---

## Key Files Reference

```
app/src/main/java/com/nfcpoc/
├── NfcPocApplication.kt        — App init (Timber logging)
├── data/
│   ├── model/
│   │   ├── NfcCard.kt          — Room entity (all card data)
│   │   ├── CardType.kt         — Card technology enum
│   │   └── CardModels.kt       — MifareSector, UltralightPage, ApduExchange
│   ├── database/
│   │   ├── Converters.kt       — Room JSON converters
│   │   ├── CardDao.kt          — Database access object
│   │   └── CardDatabase.kt     — Room singleton
│   └── repository/
│       └── CardRepository.kt   — Data access abstraction
├── nfc/
│   ├── KeyDictionary.kt        — 20 default MIFARE keys
│   ├── NfcReaderManager.kt     — Tag dispatcher
│   └── handlers/
│       ├── MifareClassicHandler.kt   — Sector brute-force reader
│       ├── MifareUltralightHandler.kt — Page reader
│       ├── IsoDepHandler.kt          — EMV APDU flow
│       ├── NfcBHandler.kt            — ISO 14443-3B reader
│       └── NfcFHandler.kt            — FeliCa reader
├── hce/
│   ├── ApduRouter.kt           — APDU response state machine
│   ├── EmulationSessionManager.kt — Session singleton
│   └── HceEmulationService.kt  — HostApduService implementation
└── ui/
    ├── MainActivity.kt          — NFC foreground dispatch host
    ├── scan/                    — Scan screen
    ├── cards/                   — Card list + detail
    └── replay/                  — HCE replay screen
```

---

## Troubleshooting

| Issue | Solution |
|-------|----------|
| `MIFARE Classic: all keys failed` | Card uses a proprietary key. Add known keys to `KeyDictionary.kt` |
| `IsoDep: PPSE select failed` | Card may be MIFARE Classic with IsoDep wrapper — check hex dump |
| HCE emulation not triggering | Ensure NFC is ON, another payment app may be default — disable it or tap the reader repeatedly |
| `NfcAdapter == null` | Device doesn't support NFC (shouldn't happen on OnePlus) |
| App crashes on Android 14+ | Check `PendingIntent.FLAG_MUTABLE` — already set in `MainActivity` |
| Gradle sync fails | Check internet connection; verify Java 17 is configured in Android Studio settings |

---

## Adding Custom MIFARE Keys

Edit `app/src/main/java/com/nfcpoc/nfc/KeyDictionary.kt`:

```kotlin
// Add your key at the end of ALL_KEYS list:
byteArrayOf(0xAB.toByte(), 0xCD.toByte(), 0xEF.toByte(), 0x12.toByte(), 0x34.toByte(), 0x56.toByte()),
```

---

## Security Research Notes

- **MIFARE Classic** cards with all-zeros or 0xFF keys are completely readable and clonable with external hardware
- **ISO 14443-4 Payment cards** use dynamic cryptograms — the APDU replay will be rejected by the acquirer backend (demonstrates the cloning vector, not a working fraud tool)
- **FeliCa transit** cards with unencrypted services expose balance/trip data
- All data is stored **locally** on-device only. No network transmission occurs.
