# AGENTS.md — ShrinkIt Android Project Architecture & Guidelines

> **Project Mission:** Build a production-grade, privacy-first native Android application for **ShrinkIt** using **Kotlin 2.0+ + Jetpack Compose + Material 3**. This app mirrors and natively implements the full image processing engine and design system of the [ShrinkIt Web App](file:///d:/0/1/shrinkit).

---

## 1. Project Overview & Configuration

- **App Name:** ShrinkIt
- **Package / Namespace:** `com.dusrel.shrinkit`
- **Target OS:** Android 7.0+ (`minSdk = 24`, `targetSdk = 35` [Android 15], `compileSdk = 35`)
- **Language & UI:** Kotlin 2.0.21+ & Jetpack Compose (Material 3 Expressive)
- **Compose Compiler:** `org.jetbrains.kotlin.plugin.compose` (Kotlin 2.0+ bundled plugin — no legacy extension versions)
- **Dependency Management:** Gradle Version Catalog (`gradle/libs.versions.toml`) — zero hardcoded versions in build scripts
- **Architecture Pattern:** Clean Architecture + MVVM + Unidirectional Data Flow (UDF) + Type-Safe Navigation Compose 2.8+
- **Image Pipeline Stack:** Native Android `BitmapFactory` with `inJustDecodeBounds` memory guards + `Coil 3` (`io.coil-kt.coil3:coil-compose`) + AndroidX `ExifInterface`
- **Permissions:**
  - `android.permission.INTERNET` (external docs & update links)
  - `android.permission.POST_NOTIFICATIONS` (Android 13+ background batch completion alerts)
  - `android.permission.WRITE_EXTERNAL_STORAGE` (`maxSdkVersion = 28` for legacy Android 7–9)
  - **Zero Storage Permission for Image Selection:** Powered by modern Android Photo Picker (`PickMultipleVisualMedia`)
- **System Invariants:**
  - **Android 15 Edge-to-Edge:** Mandatory `enableEdgeToEdge()` + proper `WindowInsets` padding (no overlapping status/navigation bars).
  - **Predictive Back:** `android:enableOnBackInvokedCallback="true"`.
  - **External Browsing:** All web links MUST open in the user's default browser via AndroidX Custom Tabs (`androidx.browser:browser`).
  - **Gallery Storage Integrity:** Scoped storage write to `Pictures/ShrinkIt` with `IS_PENDING = 1` during compression, flipped to `0` upon completion.
- **Languages / Localization:**
  - **English (Default)** $\to$ `res/values/strings.xml`
  - **Hindi (हिंदी)** $\to$ `res/values-hi/strings.xml`
  - **Assamese (অসমীয়া)** $\to$ `res/values-as/strings.xml`
  - Registered in `res/xml/locales_config.xml` for full Android 13+ Per-App Language Preferences sync.
- **Icons & Visual Language:**
  - **Launcher Icons:** Android Adaptive Icons generated from [1024x1024 Master Icon](file:///d:/0/1/shrinkit/chrome-extension/icons/profile-1024.png) and [favicon.svg](file:///d:/0/1/shrinkit/public/favicon.svg) with Material You monochrome layer.
  - **In-App Icons:** Phosphor Icons (for 1:1 web tool parity) + Material 3 Outlined Icons (for standard Android actions).
- **Reference Web Application:** [ShrinkIt Web App](file:///d:/0/1/shrinkit)

---

## 2. Session Initialization & Bootstrap Protocol

Whenever opening a new session in this project, inspect files in this exact order:
1. `AGENTS.md` (this file)
2. [docs/PRD.md](file:///d:/0/1/shrinkit-app/docs/PRD.md)
3. [docs/TRD.md](file:///d:/0/1/shrinkit-app/docs/TRD.md)
4. [docs/PLAN.md](file:///d:/0/1/shrinkit-app/docs/PLAN.md)
5. [docs/STATUS.md](file:///d:/0/1/shrinkit-app/docs/STATUS.md)
6. [docs/MODULE-MAP.md](file:///d:/0/1/shrinkit-app/docs/MODULE-MAP.md)

---

## 3. Web Parity & Link Invariants

The Android app must maintain strict behavioral and mathematical parity with the web codebase:
- **Unit Conversions (px, %, cm, in, mm):** Match [src/lib/image/units.ts](file:///d:/0/1/shrinkit/src/lib/image/units.ts).
- **Safe Dimension & Memory Limits:** Match [src/lib/image/limits.ts](file:///d:/0/1/shrinkit/src/lib/image/limits.ts).
- **Resize Modes (`stretch`, `crop`, `fit`):** Match [src/lib/image/engine.ts](file:///d:/0/1/shrinkit/src/lib/image/engine.ts).
- **Metadata Stripping & Privacy Defaults:** Match [src/lib/image/strip.ts](file:///d:/0/1/shrinkit/src/lib/image/strip.ts).
- **Naming & Formatting:** Match [src/lib/image/filenames.ts](file:///d:/0/1/shrinkit/src/lib/image/filenames.ts).
- **Visual Design Tokens & Palette:** Match [src/styles/global.css](file:///d:/0/1/shrinkit/src/styles/global.css) (`#007bff`, Dark graphite, border tokens).

---

## 4. Verification Protocol (Evidence Over Assertion)

Before marking any task or feature complete:
- Run `./gradlew test` (Unit tests for pure math, engine, and binary search).
- Run `./gradlew lintDebug`.
- Build the APK: `./gradlew assembleDebug`.
- Verify exit code 0 before concluding.
