# Product Requirements Document (PRD) — ShrinkIt Android

> **Companion Web App:** [ShrinkIt Web](file:///d:/0/1/shrinkit) (`https://shrinkit.dusrel.com`)  
> **App Package / Namespace:** `com.dusrel.shrinkit`  
> **Target Platform:** Android 7.0–15+ (API 24 to 35)  
> **Status:** Approved for Implementation

---

## 1. Executive Summary & Value Proposition

ShrinkIt Android is a **privacy-first, zero-bloat image resizer, compressor, and format converter** built with Native Kotlin and Jetpack Compose under the package `com.dusrel.shrinkit`.

### Core Value Pillars:
1. **Deterministic File Size Compression:** Binary search quality targeting allows users to hit exact constraints (e.g., strictly `20 KB`, `50 KB`, `100 KB`) for government forms and exams.
2. **Professional Dimensional Precision:** Exact support for millimeters, centimeters, inches, and pixels with honest 300 DPI preservation for printing and official form submissions.
3. **Zero-Permission Media Selection:** Uses modern Android Photo Picker (`PickMultipleVisualMedia`) requiring zero invasive storage permission dialogs.
4. **Android 15 Native Experience:** Built for targetSdk 35 with mandatory edge-to-edge rendering, predictive back gestures, and Material You themed adaptive icons.
5. **Background Batch Processing & Notifications:** Compress dozens of photos simultaneously in the background with progress notifications (`POST_NOTIFICATIONS`) alerting the user when done.
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
  - Foreground: Centered white ShrinkIt compressor mark sourced from [profile-1024.png](file:///d:/0/1/shrinkit/chrome-extension/icons/profile-1024.png) and [favicon.svg](file:///d:/0/1/shrinkit/public/favicon.svg).
  - Background: ShrinkIt Brand Blue (`#007bff`).
  - Monochrome Layer: Tinted for Android 13+ Material You themed home screens.
- **In-App Icons:** Hybrid architecture pairing Phosphor Icons (for 1:1 web parity on tools) with Material 3 Outlined Icons (for Android system actions).

### 3.3. External Browser Link Handling
- Any click on Terms, Privacy, Dusrel website, or Support launches the user's default browser using Android Custom Tabs (`androidx.browser:browser`) or standard `ACTION_VIEW` intent with fallback.

### 3.4. Permissions Manifesto
- `android.permission.INTERNET`: Required for web link navigation and external update pings.
- `android.permission.POST_NOTIFICATIONS`: Android 13+ (API 33+) permission for background batch processing alerts.
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

### Journey 2: Background Batch Processing with Notification
1. User selects 25 high-resolution photos to shrink by 50%.
2. Taps "Process All".
3. Background Worker / Coroutine executes on `Dispatchers.Default`.
4. User can switch apps; upon completion, a notification appears: *"ShrinkIt: 25 images compressed! Saved 82MB."*
5. Tapping notification opens the app directly to the results screen.
