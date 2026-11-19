# EvoTempFix

A lightweight Xposed / LSPosed module providing temporary SystemUI fixes for **Evolution X 12.3** on **POCO F5 (`marble`)** until official ROM updates resolve them upstream.

---

## Current Fixes (`v2.9`)

* **Biometric Unlock Ripple Animation:** Restores the side-mounted fingerprint unlock ripple effect in `com.android.systemui` by overriding the uninitialized `(0, 0)` sensor coordinate with `Point(1080, 1025)`, forcing proper view bounds layout on `AuthRippleView`, and triggering `AuthRippleController` directly on `KeyguardUpdateMonitor.onFingerprintAuthenticated`.

---

## Requirements

* **Device:** POCO F5 (`marble`)
* **ROM:** Evolution X 12.3 (Android 16)
* **Framework:** LSPosed (Zygisk / KernelSU / KSU Next)

---

## Installation

1. Download the latest `EvoTempFix-v2.9.apk` from the [Releases](../../releases) page.
2. Install the APK on your device.
3. Open **LSPosed Manager** → **Modules** → Enable **EvoTempFix**.
4. Ensure **System UI (`com.android.systemui`)** is checked in the module scope.
5. Restart SystemUI (`su -c "killall com.android.systemui"`) or reboot your device.

---

## Branches

* `master` — Stable, verified fixes ready for daily use.
* `dev` — Experimental branch for testing upcoming Evolution X workarounds.

---

## Author

* **Sahil Ansari** — [@imsahilansarii](https://t.me/imsahilansarii)
