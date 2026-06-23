# 🛡️ Quiet

**Version 1.0 - Jaguatirica**

Android app that **blocks 100% of calls** not on your **trusted list** (whitelist). Built for the Brazilian people tired of scam calls, silent calls that record your voice, and phone spam.

> **Lifetime protection against scams.** Anyone not on your list gets **hung up on** — no ring, no missed call, no notification, no fright.

**Application ID:** `org.floatingskies.Quiet`
**Contact:** arielcloss@gmail.com

---

## ✨ What the app does

- ✅ **Trust whitelist**: only people you authorize can call you
- ✅ **Silent blocking**: the call is rejected and hung up without you even knowing
- ✅ **No missed call**: doesn't appear in the call log
- ✅ **No notification**: no "someone tried to call you" alert
- ✅ **Block hidden/private numbers** (configurable)
- ✅ **Paranoid mode**: if the list is empty, **ALL** calls are blocked
- ✅ **Blocked call log**: see who tried and how many times
- ✅ **Export CSV**: for filing complaints with Anatel (Brazilian telecom agency) or Civil Police
- ✅ **Doesn't replace Google Phone**: you only authorize Quiet to filter calls (via `ROLE_CALL_SCREENING`)
- ✅ **Lifetime activation**: 1 donation, 9-line code, works forever
- ✅ **Shareable**: the code can be sent to family members
- ✅ **Works offline**: code validation is 100% local (SHA-256)

---

## 🆓 Free version vs 💛 Lifetime version

| Feature | Free | Lifetime (donor) |
|---------|------|-------------------|
| **Whitelist contacts** | up to **11** | **∞ UNLIMITED** |
| Silent blocking | ✅ | ✅ |
| Block hidden numbers | ✅ | ✅ |
| Blocked calls log | ✅ | ✅ |
| Export CSV | ✅ | ✅ |
| Future updates | ✅ | ✅ |
| **Price** | R$ 0.00 | **R$ 4.99 (one-time lifetime donation)** |

---

## 💛 Lifetime donation — R$ 4.99

This is a **donation**, not a purchase. It helps the developer keep the project alive, pay for servers, and improve the app for all Brazilians.

### PIX keys (NUBANK — Ariel Closs)

| Type | Key |
|------|-----|
| 📞 **Phone** | `+55 69 9342-7132` |
| 📧 **Email** | `arielcloss@gmail.com` |
| 🏦 **Bank** | NUBANK |
| 👤 **Recipient** | Ariel Closs Novais |

### Activation flow

1. User pays **R$ 4.99** via PIX — either by tapping "Open bank app" inside Quiet (which opens the official Nubank payment link) or by manually using one of the keys above
2. User enters their email and the transaction ID inside the app
3. The developer (Ariel) receives the PIX notification at NUBANK
4. Generates a 9-line code with `ferramentas/gerar_codigo.py`
5. Sends the code via email **from `arielcloss@gmail.com`** to the customer
6. Customer pastes the code into the app's activation screen → done!

> ⚠️ The app itself does not send emails automatically. The developer needs to generate the code and send it manually via email. In a future version, a backend can be added (e.g., Google Apps Script + Gmail API) to automate the flow.

---

## 🌍 Compatibility

| Android | Version | Status | Blocking method |
|---------|---------|--------|-----------------|
| 5.0-5.1 | Lollipop | ✅ Works with limitations | `ITelephony.endCall()` via reflection |
| 6.0 | Marshmallow | ✅ Works | `ITelephony.endCall()` via reflection |
| 7.0-9.0 | Nougat-Pie | ✅ **100% silent** | `CallScreeningService` (direct) |
| 10-14 | Q-UpsideDownCake | ✅ **100% silent** | `CallScreeningService` (via `ROLE_CALL_SCREENING`) |

### 🎯 Does NOT replace the default dialer (Android 10+)

On Android 10+, Quiet uses **`ROLE_CALL_SCREENING`** (via `RoleManager`) to be authorized as the "call screening app". This means:

- ✅ **Google Phone (or Samsung Phone, etc.) remains the default dialer**
- ✅ You make and receive calls normally through the native dialer
- ✅ Quiet only decides whether to block each incoming call (via `CallScreeningService` callback)
- ✅ Same approach used by Truecaller, Should I Answer?, etc.

### ⚠️ Known limitations (Android 5.0-6.0)

On these older versions, Android has no official API for silent blocking. The app uses reflection to call `endCall()` from `ITelephony`. The following may occur:

- 🟡 The call may ring once before being hung up (depends on manufacturer)
- 🟡 May appear as "missed call" on some devices (LG, older Xiaomi)
- 🔴 May not work on rooted devices or custom ROMs

**On Android 7+, blocking is 100% silent and official via `CallScreeningService`.**

### 📱 Manufacturer support

Tested and compatible with:
- ✅ **Stock Android** (Pixel, Nexus, Motorola, Android One)
- ✅ **MIUI** (Xiaomi, Redmi, Poco) — requires "Auto-start" enabled
- ✅ **OneUI** (Samsung Galaxy) — works perfectly
- ✅ **LG UI** (LG K-series, G-series) — works
- ✅ **EMUI** (Huawei) — requires "Battery protection" disabled for the app
- ✅ **ColorOS** (Oppo, Realme) — requires "Background start" allowed

---

## 📲 How to compile the APK

### Prerequisites

1. **Android Studio Hedgehog or newer** (download: https://developer.android.com/studio)
2. **JDK 17** (recommended: bundled jbr-17 or OpenJDK 17)
3. **Android SDK Platform 34** (Android 14) — Android Studio installs it automatically
4. **Build Tools 34.0.0**
5. Internet connection to download Gradle dependencies the first time

### Step by step

```bash
# 1. Copy the BloqueadorChamadasBR folder to your computer
# 2. Open Android Studio → "Open" → select the BloqueadorChamadasBR folder

# 3. Choose the Gradle JDK: jbr-17 (preferred) or Embedded JDK
# 4. Wait for Gradle to sync (5-15 min the first time)

# 5. To build a debug APK (for testing):
#    Menu Build → Build Bundle(s)/APK(s) → Build APK(s)

# 6. To build a release APK (for distribution):
#    Menu Build → Generate Signed Bundle / APK → APK
#    Create a keystore (first time) and fill in the details
```

The APK will be generated at:
- Debug: `app/build/outputs/apk/debug/app-debug.apk`
- Release: `app/build/outputs/apk/release/app-release.apk`

### Compile via command line (alternative)

```bash
# Linux/Mac
./gradlew assembleRelease

# Windows
gradlew.bat assembleRelease
```

---

## 🔑 Activation system (9-line codes)

### How it works

1. The app generates a 9-line code in the format `XXXX-XXXX-XXXX` per line
2. The first 8 lines are random (the body)
3. The 9th line is the **signature** = SHA-256(body + secret) → 12 chars → formatted
4. The app validates offline: recalculates the signature and compares it with the 9th line
5. **No internet required** to validate

### Generating codes for donor customers

Use the Python script in the `ferramentas/` folder:

```bash
cd ferramentas

# Generate 1 code
python3 gerar_codigo.py

# Generate 5 codes
python3 gerar_codigo.py -n 5

# Generate 10 codes and save to a file
python3 gerar_codigo.py -n 10 -o codigos_clientes.txt

# Compact format (with pipes | for sending via WhatsApp)
python3 gerar_codigo.py --compacto
```

**Example generated code:**
```
0Z1R-7SR1-Y0U0
CUQF-L80H-QUSI
2EWN-BF7C-P21Z
96UU-X0ZV-MZZE
4O8R-3ERD-OYOT
TXDY-E03L-K3RV
6D55-BPVC-DRFQ
AY70-Y8QI-DL4L
6435-4860-50F0
```

### Recommended flow for sending the code

1. Customer pays **R$ 4.99** via PIX (keys: `+55 69 9342-7132` or `arielcloss@gmail.com`)
2. Customer enters their email inside the app
3. You (Ariel) receive the PIX notification at NUBANK
4. Generate the code: `python3 ferramentas/gerar_codigo.py -n 1`
5. Copy the code
6. Send an email from `arielcloss@gmail.com` to the customer with:
   - Subject: "Your lifetime Quiet code 🛡️"
   - Body: the 9-line code + instructions to paste it into the app
7. Customer pastes the code into the activation screen → lifetime unlocked!

> **Important**: The secret `BloqueadorBR-2024-Lifetime-Protect-Key` is embedded in the APK. A technical user could extract it (via reverse engineering) and generate their own codes. For maximum security, in a future version, consider adding online validation (backend) that checks the code against a database of issued codes.

---

## 🏗️ Project structure

```
BloqueadorChamadasBR/
├── app/
│   ├── build.gradle                          # Module config + dependencies
│   ├── proguard-rules.pro                    # Obfuscation rules
│   └── src/main/
│       ├── AndroidManifest.xml               # Permissions + service declarations
│       ├── java/org/floatingskies/quiet/
│       │   ├── App.kt                        # Application class (init)
│       │   ├── MainActivity.kt               # Main dashboard
│       │   ├── data/                         # Room layer (Whitelist + BlockedCalls)
│       │   ├── service/
│       │   │   └── CallBlockerService.kt     # ⭐ CallScreeningService (silent blocking)
│       │   ├── receiver/
│       │   │   ├── CallReceiver.kt           # Legacy receiver (Android 5-6)
│       │   │   └── BootReceiver.kt           # Re-activates on boot
│       │   ├── ui/
│       │   │   ├── dialer/
│       │   │   │   └── ProxyDialerActivity.kt # Fallback for default dialer
│       │   │   ├── onboarding/               # Initial screen + permissions
│       │   │   ├── whitelist/                # Trust list
│       │   │   ├── payment/                  # PIX donation
│       │   │   ├── activation/               # Code activation
│       │   │   ├── blocked/                  # Blocked calls log
│       │   │   └── settings/                # Settings
│       │   └── util/
│       │       ├── PhoneUtils.kt             # BR phone number normalization
│       │       ├── ActivationValidator.kt    # SHA-256 validation
│       │       ├── PermissionHelper.kt       # Permissions + ROLE_CALL_SCREENING
│       │       └── PrefsManager.kt           # SharedPreferences
│       └── res/
│           ├── layout/                       # XML screens
│           ├── values/                       # Colors, strings (PT-BR), styles
│           ├── drawable/                     # Vector icons + PNG logo
│           └── mipmap-*/                     # Launcher icon in 5 densities
├── ferramentas/
│   └── gerar_codigo.py                       # Activation code generator
├── build.gradle                              # Top-level
├── settings.gradle
└── gradle.properties
```

---

## 🔧 Technologies

- **Language**: Kotlin 1.9.20
- **minSdk**: 21 (Android 5.0 Lollipop)
- **targetSdk**: 34 (Android 14)
- **UI**: Material Design 3 (dark, accessibility-focused)
- **DB**: Room 2.6.1
- **Build**: Gradle 8.2 + Android Gradle Plugin 8.1.4
- **Logo**: `org.floatingskies.Quiet.png` (PNG 512x512 RGBA, in all densities)

---

## 📜 Versioning

| Version | Codename | Description |
|---------|----------|-------------|
| 1.0 | **Jaguatirica** | Initial public release. Silent blocking via CallScreeningService, whitelist, R$ 4.99 lifetime donation, role-based call screening. |

---

## ⚖️ Legal notice

This app is a **personal protection tool**. It does not replace:

- 🚨 **Formal complaints** to Anatel (https://www.anatel.gov.br/consumidor)
- 🚨 **Police reports** at the Civil Police in case of an actual scam
- 🚨 **Notifications** to Procon in case of improper charges

The app blocks calls based **solely on the phone number**. It does not identify the caller's intent. It is up to the user to keep their whitelist updated with legitimate numbers (bank, doctor, family, etc.).

> **Tip**: When adding your bank's number to the whitelist, confirm the official number on the bank's website or app. Never add numbers received via suspicious SMS or WhatsApp.

---

## 🆘 Troubleshooting

| Problem | Solution |
|---------|----------|
| "The app doesn't block anything" | Make sure you granted **ROLE_CALL_SCREENING** (Android 10+) on the initial screen |
| "The call filtering dialog doesn't appear" | Tap "Call filtering app" on the app's initial screen |
| "Missed call still appears" | You're on Android 5.0-6.0 (system limitation). On 7+ it works 100% silently |
| "The app stops working after a while" | Enable "Ignore battery optimization" in the permissions |
| "MIUI kills the app in the background" | Enable "Auto-start" in MIUI settings → Apps → Quiet |
| "I can't receive calls from my bank" | Add the bank's number to the whitelist (use the official number, not the one shown in SMS) |
| "I donated but didn't receive the code" | Check your email spam folder. The sender is `arielcloss@gmail.com`. If it hasn't arrived in 30 min, contact the developer |

---

## 📞 Support

- **Developer email:** arielcloss@gmail.com
To report bugs or suggest improvements, email the developer.

---

**Made with ❤️ for Brazil.**
*Version 1.0 - Jaguatirica*
