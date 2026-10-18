# Product Requirements Document (PRD) — ShrinkIt Android

> **Companion Web App:** [ShrinkIt Web](https://shrinkit.dusrel.com)  
> **App Package / Namespace:** `com.dusrel.shrinkit`  
> **Target Platform:** Android 7.0–16+ (API 24 to 36)  
> **Status:** Approved for Implementation

---

## 1. Executive Summary & Value Proposition

ShrinkIt Android is a **privacy-first, zero-bloat image resizer, compressor, and format converter** built with Native Kotlin and Jetpack Compose under the package `com.dusrel.shrinkit`.

### Core Value Pillars:
1. **Deterministic File Size Compression:** Binary search quality targeting allows users to hit exact constraints (e.g., strictly `20 KB`, `50 KB`, `100 KB`) for government forms and exams.
2. **Professional Dimensional Precision:** Exact support for millimeters, centimeters, inches, and pixels with honest 300 DPI preservation for printing and official form submissions.
3. **Zero-Permission Media Selection:** Uses modern Android Photo Picker (`PickMultipleVisualMedia`) requiring zero invasive storage permission dialogs.
4. **Android 16 Native Experience:** Built for targetSdk 36 with mandatory edge-to-edge rendering, predictive back gestures, and Material You themed adaptive icons.
5. **Resilient Background Batch Processing:** Process batches of photos in the background via WorkManager with progress notifications (`POST_NOTIFICATIONS`) alerting the user when done.
6. **Seamless External Browsing:** External links strictly launch the user's default browser (via Android Custom Tabs), avoiding locked in-app web views.
7. **Multilingual Access:** Native support for **English (Default)**, **Hindi (हिंदी)**, and **Assamese (অসমীয়া)** with Android 13+ Per-App Language Preferences.

---

## 2. Target Audience & Personas

1. **Job Applicants & Students (Government Forms & Entrance Exams):**
   - *Problem:* SSC, UPSC, State PSC, NEET, and university portals reject uploads exceeding 20KB or 50KB, or images not matching exact 35×45mm passport dimensions.
   - *Solution:* One-tap presets for "Exam Signature (20KB)" and "Passport Photo (35×45mm @ 300 DPI)" localized in English, Hindi, and Assamese.
2. **Privacy-Conscious Individuals:**
   - *Problem:* Sharing photos without leaking home location GPS coordinates, camera serial numbers, and personal timestamps.
   - *Solution:* Automatic EXIF scrubbing on every processed photo.
3. **Content Creators & Social Media Managers:**
   - *Problem:* Images being blurry or badly cropped by Instagram, YouTube, X, or LinkedIn algorithms.
   - *Solution:* Native aspect-fit and center-crop presets for Instagram (1:1, 4:5, 9:16), YouTube thumbnails (16:9), and banners.
4. **General Smartphone Users:**
   - *Problem:* Phone storage full; need to quickly shrink high-resolution 10MB phone camera photos to under 500KB to send via email or chat apps.

---

## 3. Product Specifications & Features

### 3.1. Localization (i18n)
- **English (`en`)**: System default fallback in `res/values/strings.xml`.
- **Hindi (`hi`)**: Complete UI translation for all buttons, settings, presets, and errors in `res/values-hi/strings.xml`.
- **Assamese (`as`)**: Complete UI translation in `res/values-as/strings.xml`.
- Registered in `res/xml/locales_config.xml` for full Android 13+ Per-App Language Preferences sync.

### 3.2. App Icon & Visual Identity
- **Adaptive Launcher Icon (`ic_launcher.xml`):**
  - Foreground: Centered white ShrinkIt compressor mark.
  - Background: ShrinkIt Brand Blue (`#007bff`).
  - Monochrome Layer: Tinted for Android 13+ Material You themed home screens.
- **In-App Icons:** Material Symbols / Material Icons Extended (`androidx.compose.material:material-icons-extended`).

### 3.3. External Browser Link Handling
- Any click on Terms, Privacy, Dusrel website, or Support launches the user's default browser using Android Custom Tabs (`androidx.browser:browser`) or standard `ACTION_VIEW` intent with fallback.

### 3.4. Permissions Manifesto
- `android.permission.INTERNET`: Strictly used for opening external links (Privacy Policy, Terms, Support) in the user's default browser. The app itself never transmits user images or analytics.
- `android.permission.POST_NOTIFICATIONS`: Android 13+ (API 33+) permission for background batch processing alerts.
- `android.permission.FOREGROUND_SERVICE` & `android.permission.FOREGROUND_SERVICE_DATA_SYNC`: WorkManager foreground worker on Android 14+.
- `android.permission.WRITE_EXTERNAL_STORAGE`: Legacy support for Android 7.0–9.0 (`maxSdkVersion=28`). Android 10+ uses Scoped Storage without permission.

---

## 4. User Journeys & Core Flows

### Journey 1: Exact File Size Target (e.g., 20KB Signature for Exam)
1. User taps "Pick Images" or uses Android Photo Picker to select signature photo.
2. Selects **Target Size (KB)** mode and enters `20`.
3. Selects format: **JPG**.
4. Taps **Process Image**.
5. Engine runs binary search on JPEG quantization table.
6. Result Card displays: Original `1.8 MB` $\to$ Compressed `19.4 KB` (-98.9%), dimensions preserved.
7. User taps **Save to Gallery** or **Share**.

### Journey 2: Resilient Background Batch Processing
1. User selects 25 high-resolution photos to shrink by 50%.
2. Taps "Process All".
3. WorkManager executes batch compression in the background with an ongoing status notification.
4. User can switch apps or lock the phone; upon completion, a notification appears: *"ShrinkIt: 25 images compressed! Saved 82MB."*
5. Tapping notification opens the app directly to the results screen.
