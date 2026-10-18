# Master Implementation Plan: ShrinkIt Android

> **Repository:** [shrinkit-android](https://github.com/dusmamud/shrinkit-android)  
> **Package / Namespace:** `com.dusrel.shrinkit`  
> **Tech Stack:** Native Kotlin 2.4.x+, Jetpack Compose Material 3, KSP, WorkManager 2.12.0, Version Catalogs, Clean Architecture  
> **Reference Web App:** [ShrinkIt Web](https://shrinkit.dusrel.com)

---

## Phase Overview

| Phase | Description | Deliverables | Status |
| :---: | :--- | :--- | :---: |
| **1** | **Project Scaffolding & Version Catalog** | `gradle/libs.versions.toml`, root & app `build.gradle.kts`, KSP plugin, `settings.gradle.kts`, `AndroidManifest.xml` (API 36, Edge-to-Edge, PhotoPicker, Locales, Splash, Foreground Services) | ✅ Complete |
| **2** | **Brand Assets, Splash & Multi-Language (i18n)** | Adaptive launcher icons (`profile-1024.png`), Splash screen theme, `locales_config.xml`, `strings.xml` for English, Hindi (`hi`), Assamese (`as`) | ✅ Complete |
| **3** | **Core Image Engine (Domain Layer)** | Units math, Resize Modes (`stretch`, `crop`, `fit`), Binary Search Target KB, Dual Decoder (ImageDecoder + BitmapFactory, AVIF API 31+, HEIF check), EXIF Sanitizer | ✅ Complete |
| **4** | **Unit Test Suite (JUnit 4/5)** | Dimension conversions, safety bounds, binary search convergence, aspect ratio math | ✅ Complete |
| **5** | **Presentation Layer & Navigation** | Compose Material 3 Theme, Home Screen, Presets, Settings, Notification Helper, Browser Launcher, In-App Review, In-App Updates | ✅ Complete |
| **6** | **Background Processing & Scoped Storage** | WorkManager `BatchCompressWorker` (Foreground dataSync), MediaStore saving to `Pictures/ShrinkIt` with `IS_PENDING` atomic writes, ShareSheet | ✅ Complete |
| **7** | **Quality Gates, R8 & Production AAB** | `./gradlew test`, `./gradlew lintDebug`, R8 ProGuard shrink, `./gradlew bundleRelease` (AAB for Google Play) | ⏳ In Progress |

---

## Detailed Task Breakdown

### Phase 1: Project Scaffolding & Configuration
- [x] **Task 1.1:** Author `gradle/libs.versions.toml` with audited 2026 dependency catalog (AGP 9.4.0, Kotlin 2.4.10, KSP, Compose BOM 2026.09.00, Navigation Compose 2.10.2, Coil 3.6.3, Hilt 2.60.1, WorkManager 2.12.0, Custom Tabs, Core SplashScreen, Play Review, Play App Update).
- [x] **Task 1.2:** Setup `settings.gradle.kts` and root `build.gradle.kts` with Compose compiler plugin (`org.jetbrains.kotlin.plugin.compose`) and KSP plugin (`com.google.devtools.ksp`).
- [x] **Task 1.3:** Configure `app/build.gradle.kts` with `namespace = "com.dusrel.shrinkit"`, `compileSdk = 36`, `targetSdk = 36`, KSP-based Hilt code generation (`ksp(libs.hilt.compiler)`), and BOM-managed Material 3.
- [x] **Task 1.4:** Author `app/src/main/AndroidManifest.xml` with `INTERNET`, `POST_NOTIFICATIONS`, `FOREGROUND_SERVICE`, `FOREGROUND_SERVICE_DATA_SYNC`, `SystemForegroundService` declaration, and `locales_config`.

### Phase 2: Brand Assets, Splash Screen & Multi-Language (i18n)
- [x] **Task 2.1:** Process ShrinkIt Master Icon into Android Adaptive Icon layers (`ic_launcher`, `ic_launcher_round`, `ic_launcher_foreground`) and web PNGs.
- [x] **Task 2.2:** Setup `Theme.ShrinkIt.Splash` using `androidx.core:core-splashscreen` to eliminate blank screen cold boots.
- [x] **Task 2.3:** Author default English strings in `res/values/strings.xml`.
- [x] **Task 2.4:** Author Hindi translations in `res/values-hi/strings.xml`.
- [x] **Task 2.5:** Author Assamese translations in `res/values-as/strings.xml`.
- [x] **Task 2.6:** Create `res/xml/locales_config.xml` supporting in-app switching and Android 13+ `LocaleManager`.

### Phase 3: Core Domain Engine (`com.dusrel.shrinkit.domain.engine`)
- [x] **Task 3.1:** Implement `SizeUnits.kt` (px, %, cm, mm, inches at 300 DPI).
- [x] **Task 3.2:** Implement `Limits.kt` (dimension bounds and memory safety).
- [x] **Task 3.3:** Implement `SafeBitmapDecoder.kt` with **Dual Engine**: `ImageDecoder` on API 28+ (native HEIC with `isMimeTypeSupported` check, AVIF on API 31+, WebP, JPEG + auto EXIF orientation) and `BitmapFactory` fallback on API 24–27.
- [x] **Task 3.4:** Implement `ResizeEngine.kt` supporting `Stretch`, `Crop` (center-crop), and `Fit` (letterbox with color/alpha).
- [x] **Task 3.5:** Implement `BinarySearchCompressor.kt` for exact Target KB convergence (`50 KB`, `20 KB`, etc.).
- [x] **Task 3.6:** Implement `ExifSanitizer.kt` using AndroidX `ExifInterface` to strip GPS, camera metadata, and date/time.

### Phase 4: Unit Test Suite
- [x] **Task 4.1:** Author unit tests for `SizeUnitsTest.kt` verifying exact rounding and formula equivalence.
- [x] **Task 4.2:** Author unit tests for `LimitsTest.kt` testing dimension boundaries.
- [x] **Task 4.3:** Setup test fixtures and assertions.

### Phase 5: Presentation Layer & Services
- [x] **Task 5.1:** Implement `BrowserLauncher.kt` using AndroidX Custom Tabs to launch external URLs in the user's default browser.
- [x] **Task 5.2:** Implement `NotificationHelper.kt` with `POST_NOTIFICATIONS` channel for batch background compression alerts.
- [x] **Task 5.3:** Build `ShrinkItTheme.kt`, `Color.kt`, and `Type.kt` matching web design tokens.
- [x] **Task 5.4:** Build `PresetChipsRow.kt` for one-tap selection (Passport 35x45mm, Signature 20KB, Instagram, YouTube).
- [x] **Task 5.5:** Build `SettingsPanel.kt` with numeric inputs, unit dropdown, aspect lock toggle, and Target KB / Quality slider toggle.
- [x] **Task 5.6:** Build `ImagePickerSection.kt` with modern zero-permission Android Photo Picker.
- [x] **Task 5.7:** Integrate `InAppReviewManager.kt` via `com.google.android.play:review-ktx` triggered after successful batches.
- [x] **Task 5.8:** Integrate `InAppUpdateManager.kt` via `com.google.android.play:app-update-ktx` for flexible updates.
- [x] **Task 5.9:** Build `ShrinkItTopBar.kt` with in-app language switcher and Custom Tabs web links.
- [x] **Task 5.10:** Build `ShrinkItViewModel.kt` and `ShrinkItApp.kt`.

### Phase 6: Background Processing & Scoped Storage
- [x] **Task 6.1:** Implement `BatchCompressWorker.kt` via WorkManager (`androidx.work:work-runtime-ktx:2.12.0`) with foreground service (`dataSync`) for resilient background batch crunching.
- [x] **Task 6.2:** Build `ResultCard.kt` displaying original vs processed size badges and percent reduction badge.
- [x] **Task 6.3:** Implement `MediaStoreExporter.kt` using `IS_PENDING` atomic writes to save processed bitmaps to `Pictures/ShrinkIt` without file corruption.
- [x] **Task 6.4:** Build native Android Share Intent integration.

### Phase 7: Quality Gates & Google Play Production Release
- [ ] **Task 7.1:** Assemble release and verify build integrity.
- [ ] **Task 7.2:** Google Play Console publishing preparation.
