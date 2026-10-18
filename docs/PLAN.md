# Master Implementation Plan: ShrinkIt Android

> **Repository:** [shrinkit-android](https://github.com/dusmamud/shrinkit-android)  
> **Package / Namespace:** `com.dusrel.shrinkit`  
> **Tech Stack:** Native Kotlin 2.4.x+, Jetpack Compose Material 3, WorkManager 2.12.0, Version Catalogs, Clean Architecture  
> **Reference Web App:** [ShrinkIt Web](https://shrinkit.dusrel.com)

---

## Phase Overview

| Phase | Description | Deliverables | Status |
| :---: | :--- | :--- | :---: |
| **1** | **Project Scaffolding & Version Catalog** | `gradle/libs.versions.toml`, root & app `build.gradle.kts`, `settings.gradle.kts`, `AndroidManifest.xml` (API 36, Edge-to-Edge, PhotoPicker, Locales, Splash, Foreground Services) | ⏳ Pending |
| **2** | **Brand Assets, Splash & Multi-Language (i18n)** | Adaptive launcher icons (`profile-1024.png`), Splash screen theme, `locales_config.xml`, `strings.xml` for English, Hindi (`hi`), Assamese (`as`) | ⏳ Pending |
| **3** | **Core Image Engine (Domain Layer)** | Units math, Resize Modes (`stretch`, `crop`, `fit`), Binary Search Target KB, Dual Decoder (ImageDecoder + BitmapFactory, AVIF API 31+, HEIF check), EXIF Sanitizer | ⏳ Pending |
| **4** | **Unit Test Suite (JUnit 4/5)** | Dimension conversions, safety bounds, binary search convergence, aspect ratio math | ⏳ Pending |
| **5** | **Presentation Layer & Navigation** | Compose Material 3 Theme, Home Screen (with Drag & Drop), Presets, Settings, Notification Helper, Browser Launcher, In-App Review, In-App Updates | ⏳ Pending |
| **6** | **Background Processing & Scoped Storage** | WorkManager `BatchCompressWorker` (Foreground dataSync), MediaStore saving to `Pictures/ShrinkIt` with `IS_PENDING` atomic writes, ShareSheet | ⏳ Pending |
| **7** | **Quality Gates, R8 & Production AAB** | `./gradlew test`, `./gradlew lintDebug`, R8 ProGuard shrink, `./gradlew bundleRelease` (AAB for Google Play) | ⏳ Pending |

---

## Detailed Task Breakdown

### Phase 1: Project Scaffolding & Configuration
- [ ] **Task 1.1:** Author `gradle/libs.versions.toml` with audited 2026 dependency catalog (AGP 9.4.0, Kotlin 2.4.10, Compose BOM 2026.09.00, Coil 3.6.3, Hilt 2.52, WorkManager 2.12.0, Custom Tabs, Core SplashScreen, Play Review, Play App Update).
- [ ] **Task 1.2:** Setup `settings.gradle.kts` and root `build.gradle.kts` with Compose compiler plugin (`org.jetbrains.kotlin.plugin.compose`).
- [ ] **Task 1.3:** Configure `app/build.gradle.kts` with `namespace = "com.dusrel.shrinkit"`, `compileSdk = 36`, `targetSdk = 36`, and modern dependency aliases.
- [ ] **Task 1.4:** Author `app/src/main/AndroidManifest.xml` with `INTERNET`, `POST_NOTIFICATIONS`, `FOREGROUND_SERVICE`, `FOREGROUND_SERVICE_DATA_SYNC`, `SystemForegroundService` declaration, and `locales_config`.

### Phase 2: Brand Assets, Splash Screen & Multi-Language (i18n)
- [ ] **Task 2.1:** Process ShrinkIt 1024x1024 Master Icon into Android Adaptive Icon layers (`ic_launcher`, `ic_launcher_round`, `ic_launcher_foreground`, `ic_launcher_monochrome`) across `mdpi`, `hdpi`, `xhdpi`, `xxhdpi`, `xxxhdpi`.
- [ ] **Task 2.2:** Setup `Theme.ShrinkIt.Splash` using `androidx.core:core-splashscreen` to eliminate blank screen cold boots.
- [ ] **Task 2.3:** Author default English strings in `res/values/strings.xml`.
- [ ] **Task 2.4:** Author Hindi translations in `res/values-hi/strings.xml`.
- [ ] **Task 2.5:** Author Assamese translations in `res/values-as/strings.xml`.
- [ ] **Task 2.6:** Create `res/xml/locales_config.xml` and `LocaleHelper.kt` supporting in-app switching and Android 13+ `LocaleManager`.

### Phase 3: Core Domain Engine (`com.dusrel.shrinkit.domain.engine`)
- [ ] **Task 3.1:** Implement `SizeUnits.kt` (px, %, cm, mm, inches at 300 DPI).
- [ ] **Task 3.2:** Implement `Limits.kt` (dimension bounds and memory safety).
- [ ] **Task 3.3:** Implement `SafeBitmapDecoder.kt` with **Dual Engine**: `ImageDecoder` on API 28+ (native HEIC with `isMimeTypeSupported` check, AVIF on API 31+, WebP, JPEG + auto EXIF orientation) and `BitmapFactory` fallback on API 24–27.
- [ ] **Task 3.4:** Implement `ResizeEngine.kt` supporting `Stretch`, `Crop` (center-crop), and `Fit` (letterbox with color/alpha).
- [ ] **Task 3.5:** Implement `BinarySearchCompressor.kt` for exact Target KB convergence (`50 KB`, `20 KB`, etc.).
- [ ] **Task 3.6:** Implement `ExifSanitizer.kt` using AndroidX `ExifInterface` to strip GPS, camera metadata, and date/time.

### Phase 4: Unit Test Suite
- [ ] **Task 4.1:** Author unit tests for `SizeUnitsTest.kt` verifying exact rounding and formula equivalence.
- [ ] **Task 4.2:** Author unit tests for `LimitsTest.kt` testing dimension boundaries.
- [ ] **Task 4.3:** Author unit tests for `ResizeEngineTest.kt` validating aspect crop rectangle calculations and fit box constraints.
- [ ] **Task 4.4:** Run `./gradlew test` and confirm 100% pass rate.

### Phase 5: Presentation Layer & Services
- [ ] **Task 5.1:** Implement `BrowserLauncher.kt` using AndroidX Custom Tabs to launch external URLs in the user's default browser.
- [ ] **Task 5.2:** Implement `NotificationHelper.kt` with `POST_NOTIFICATIONS` channel for batch background compression alerts.
- [ ] **Task 5.3:** Build `ShrinkItTheme.kt` with Light and Graphite Dark palette matching web design tokens.
- [ ] **Task 5.4:** Build `PresetChipsRow.kt` for one-tap selection (Passport 35x45mm, Signature 20KB, Instagram, YouTube).
- [ ] **Task 5.5:** Build `SettingsPanel.kt` with numeric inputs, unit dropdown, aspect lock toggle, and Target KB / Quality slider toggle.
- [ ] **Task 5.6:** Build `ImagePickerSection.kt` with modern zero-permission Android Photo Picker and Tablet/Foldable Drag-and-Drop (`Modifier.dragAndDropTarget`).
- [ ] **Task 5.7:** Integrate `InAppReviewManager.kt` via `com.google.android.play:review-ktx` triggered after successful batches.
- [ ] **Task 5.8:** Integrate `InAppUpdateManager.kt` via `com.google.android.play:app-update-ktx` for flexible updates.

### Phase 6: Background Processing & Scoped Storage
- [ ] **Task 6.1:** Implement `BatchCompressWorker.kt` via WorkManager (`androidx.work:work-runtime-ktx:2.12.0`) with foreground service (`dataSync`) for resilient background batch crunching.
- [ ] **Task 6.2:** Build `ResultCard.kt` displaying original vs processed size badges and percent reduction badge.
- [ ] **Task 6.3:** Implement `MediaStoreExporter.kt` using `IS_PENDING` atomic writes to save processed bitmaps to `Pictures/ShrinkIt` without file corruption.
- [ ] **Task 6.4:** Build native Android Share Intent integration.

### Phase 7: Quality Gates & Google Play Production Release
- [ ] **Task 7.1:** Run `./gradlew test` to verify all unit tests pass with exit code 0.
- [ ] **Task 7.2:** Run `./gradlew lintDebug` and resolve any compiler warnings.
- [ ] **Task 7.3:** Configure release build targeting API 36 with ProGuard/R8 code shrinking and resource shrinking (`isMinifyEnabled = true`, `isShrinkResources = true`).
- [ ] **Task 7.4:** Assemble production Android App Bundle (`./gradlew bundleRelease`) ready for Google Play Console upload.
