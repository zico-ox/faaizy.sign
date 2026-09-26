# EXEO EX58P - Android Print Bridge & Web POS Architecture

This guide explains how to connect your **web-based billing application** to an **EXEO EX58P (58mm)** thermal receipt printer on an Android mobile phone without ever opening the browser print dialog.

---

## 1. The Core Hardware Reality of EXEO EX58P on Android

* **Primary Protocol:** Like most 58mm portable POS printers, the **EXEO EX58P** uses **Bluetooth Classic (RFCOMM / SPP - Serial Port Profile)** for Android communication.
* **Why Chrome Web Bluetooth alone is insufficient:** 
  * The Web Bluetooth API in Google Chrome (`navigator.bluetooth`) strictly supports **BLE (Bluetooth Low Energy / GATT)** only.
  * Chrome **cannot** open Bluetooth Classic SPP sockets directly.
  * If your EXEO EX58P unit's BLE advertisement is inactive, Chrome will not even discover it.
* **The Solution:** We provide two seamless pathways:
  1. **Immediate Zero-Code Testing:** Open `billex.html` in Chrome on Android and use the **Android Intent Bridge (RawBT)**.
  2. **Dedicated Production App:** Use the **Smallest Android WebView Wrapper** provided in this directory.

---

## Option 1: Immediate Test via Android Intent (No App Compilation Needed)

If you want to test printing right now without compiling an Android Studio APK:

1. **Pair your Printer in Android Settings:**
   * Turn ON your EXEO EX58P.
   * Open **Android Settings > Bluetooth**.
   * Tap on **EX58P** (or *MPT-II* / *Printer*).
   * Enter PIN: `0000` or `1234`.
2. **Install RawBT Print Service:**
   * Open Google Play Store on Android and install **RawBT Print Service** (free).
   * Open RawBT, choose **Bluetooth**, and select your paired **EXEO EX58P**.
3. **Open `billex.html` in Chrome:**
   * Select **Android Intent (RawBT / SPP)** mode.
   * Click **PRINT BILL (₹580)**.
   * Android immediately forwards the raw ESC/POS bytes to the EXEO EX58P over Bluetooth Classic and prints the receipt in ~1 second with no preview dialog!

---

## Option 2: The Smallest Android WebView Wrapper (Custom Native Bridge)

If you want your own branded, standalone Android app with **1-tap silent printing**:

### How It Works:
1. The app is a lightweight **Android WebView** (< 2MB APK).
2. It hosts your web billing app (`billex.html` or your online URL).
3. The native code injects a bridge: `window.AndroidPrinter.print(base64EscPosData)`.
4. When the user taps **PRINT BILL**, the web app passes the raw ESC/POS bytes to `window.AndroidPrinter.print()`.
5. The Android native code writes the bytes directly to the paired EXEO EX58P via an RFCOMM BluetoothSocket (`00001101-0000-1000-8000-00805F9B34FB`).

### Project Files:
* [`MainActivity.kt`](./MainActivity.kt) - The complete ~60-line Kotlin activity containing the WebView and the Bluetooth SPP bridge.
* [`AndroidManifest.xml`](./AndroidManifest.xml) - Manifest with Bluetooth Classic & Android 12+ Bluetooth Connect permissions.
* [`activity_main.xml`](./activity_main.xml) - Fullscreen WebView layout.

### Building in Android Studio:
1. Create a "New Project" > "Empty Views Activity" with package `com.faaizy.billex`.
2. Replace `MainActivity.kt`, `AndroidManifest.xml`, and `activity_main.xml` with the files in this folder.
3. Place `billex.html` in `app/src/main/assets/billex.html` (or set the URL in `MainActivity.kt` to your hosted web app).
4. Build APK & Run on your Android phone!

---

## 58mm ESC/POS Formatting Specifications

* **Paper Width:** 58mm (approx. 48mm printable width).
* **Resolution:** 203 DPI (8 dots/mm = 384 dots per line).
* **Columns:** 32 standard characters per line (Font A: 12×24 dots).
* **Currency Symbol:** Standard thermal printers use CodePage 437 by default. To prevent garbled text (`â‚¹`), the builder formats the currency as `Rs.` on the thermal paper while keeping `₹` in the web preview.
* **Manual Cutter Clearance:** Feeds 4 blank lines (`LF × 4`) at the end of the receipt so the total and footer can be cleanly torn against the serrated cutter bar without cutting off any text.
