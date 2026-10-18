# AGENTS.md — ShrinkIt Android Project Architecture & Guidelines

> **Project Mission:** Build a production-grade, privacy-first native Android application for **ShrinkIt** using **Kotlin + Jetpack Compose + Material 3**. This app mirrors the core image resizing, compression, and format conversion capabilities of the [ShrinkIt Web App](https://shrinkit.dusrel.com).

---

## 1. Project Overview & Configuration

- **App Name:** ShrinkIt
- **Package / Namespace:** `com.dusrel.shrinkit`
- **Target OS:** Android 7.0+ (`minSdk = 24`, `targetSdk = 36` [Android 16], `compileSdk = 36`)
- **Language & UI:** Kotlin (Compose Compiler Gradle Plugin `org.jetbrains.kotlin.plugin.compose`) & Jetpack Compose (Material 3)
- **Dependency Management:** Gradle Version Catalog (`gradle/libs.versions.toml`)
- **Architecture Pattern:** Clean Architecture + MVVM + Unidirectional Data Flow (UDF) + Type-Safe Navigation Compose
- **Image Pipeline Stack:** 
  - Dual Decoding: `ImageDecoder` on API 28+ (native HEIC, WebP, JPEG; AVIF on API 31+) with `BitmapFactory` fallback on API 24–27.
  - Device HEIC validation via `ImageDecoder.isMimeTypeSupported("image/heif")`.
  - Memory safe `inJustDecodeBounds` downsampling.
  - AndroidX `ExifInterface` for GPS/metadata scrubbing.
- **Permissions:**
  - `android.permission.INTERNET` (strictly for opening external documentation, terms, and website in the user's default browser)
  - `android.permission.POST_NOTIFICATIONS` (Android 13+ batch completion alerts)
  - `android.permission.FOREGROUND_SERVICE` & `android.permission.FOREGROUND_SERVICE_DATA_SYNC` (required for WorkManager background execution on Android 14+)
  - `android.permission.WRITE_EXTERNAL_STORAGE` (`maxSdkVersion = 28` for legacy Android 7–9)
  - **Zero Storage Permission for Media Selection:** Powered by modern Android Photo Picker (`PickMultipleVisualMedia`)
- **System Invariants:**
  - **Android 15/16 Edge-to-Edge:** Mandatory `enableEdgeToEdge()` + proper `WindowInsets` padding (no overlapping system bars).
  - **Predictive Back:** Default-on in Android 16 (API 36).
  - **External Browsing:** All web links MUST open in the user's default browser via AndroidX Custom Tabs (`androidx.browser:browser`).
  - **Gallery Storage Integrity:** Scoped storage write to `Pictures/ShrinkIt` with `IS_PENDING = 1` during compression, flipped to `0` upon completion.
  - **Background Work Resilience:** WorkManager (`androidx.work:work-runtime-ktx`) for resilient background batch crunching.
- **Languages / Localization:**
  - **English (Default)** $\to$ `res/values/strings.xml`
  - **Hindi (हिंदी)** $\to$ `res/values-hi/strings.xml`
  - **Assamese (অসমীয়া)** $\to$ `res/values-as/strings.xml`
  - Registered in `res/xml/locales_config.xml` for full Android 13+ Per-App Language Preferences sync.
- **Icons & Visual Language:**
  - **Launcher Icons:** Android Adaptive Icons generated from `profile-1024.png` with Material You monochrome layer.
  - **In-App Icons:** Material Symbols / Material Icons Extended (`androidx.compose.material:material-icons-extended`).
- **Reference Web Application:** [ShrinkIt Web App](https://shrinkit.dusrel.com)

---

## 2. Session Initialization & Bootstrap Protocol

Whenever opening a new session in this project, inspect files in this exact order:
1. `AGENTS.md` (this file)
2. `docs/PRD.md`
3. `docs/TRD.md`
4. `docs/PLAN.md`
5. `docs/STATUS.md`
6. `docs/MODULE-MAP.md`

---

## 3. Web Parity Invariants

The Android app must maintain strict mathematical and feature parity with the web codebase:
- **Unit Conversions (px, %, cm, in, mm):** Match pure conversion formulas @ 300 DPI.
- **Safe Dimension & Memory Limits:** Cap maximum canvas dimensions at $8192 \times 8192\text{ px}$.
- **Resize Modes (`stretch`, `crop`, `fit`):** Match aspect crop and letterbox fit behaviors.
- **Metadata Stripping & Privacy Defaults:** Wipe camera EXIF and GPS tags by default.
- **Visual Design Tokens & Palette:** Match brand palette (`#007bff`, Dark graphite, border tokens).

---

## 4. Verification Protocol (Evidence Over Assertion)

Before marking any task or feature complete:
- Run `./gradlew test` (Unit tests for pure math, engine, and binary search).
- Run `./gradlew lintDebug`.
- Build the APK / AAB: `./gradlew assembleDebug` / `./gradlew bundleRelease`.
- Verify exit code 0 before concluding.
