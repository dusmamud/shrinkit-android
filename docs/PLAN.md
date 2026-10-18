# Master Implementation Plan: ShrinkIt Android

> **Target Directory:** [d:\0\1\shrinkit-app](file:///d:/0/1/shrinkit-app)  
> **Package / Namespace:** `com.dusrel.shrinkit`  
> **Tech Stack:** Native Kotlin 2.0.21+, Jetpack Compose Material 3, Version Catalogs, Clean Architecture  
> **Reference Web App:** [ShrinkIt Web](file:///d:/0/1/shrinkit)

---

## Phase Overview

| Phase | Description | Deliverables | Status |
| :---: | :--- | :--- | :---: |
| **1** | **Project Scaffolding & Version Catalog** | `gradle/libs.versions.toml`, root & app `build.gradle.kts`, `settings.gradle.kts`, `AndroidManifest.xml` (Edge-to-Edge, PhotoPicker, Locales) | ⏳ Pending |
| **2** | **Brand Assets & Multi-Language (i18n)** | Adaptive launcher icons (`profile-1024.png`), `locales_config.xml`, `strings.xml` for English, Hindi (`hi`), Assamese (`as`) | ⏳ Pending |
| **3** | **Core Image Engine (Domain Layer)** | Units math, Resize Modes (`stretch`, `crop`, `fit`), Binary Search Target KB, OOM safe decoder, EXIF Sanitizer | ⏳ Pending |
| **4** | **Unit Test Suite (JUnit 4/5)** | Dimension conversions, safety bounds, binary search convergence, aspect ratio math | ⏳ Pending |
| **5** | **Presentation Layer & Navigation** | Compose Material 3 Theme, Home Screen, Presets, Settings, Notification Helper, Browser Launcher (Custom Tabs) | ⏳ Pending |
| **6** | **Result & Gallery Export (Scoped Storage)** | Result Card, MediaStore saving to `Pictures/ShrinkIt` with `IS_PENDING` atomic writes, ShareSheet | ⏳ Pending |
| **7** | **Quality Gates, Lint & Build Verification** | `./gradlew test`, `./gradlew lintDebug`, `./gradlew assembleDebug` | ⏳ Pending |

---

## Detailed Task Breakdown

### Phase 1: Project Scaffolding & Configuration
- [ ] **Task 1.1:** Author `gradle/libs.versions.toml` with audited 2026 dependency catalog (AGP 8.8+, Kotlin 2.0.21, Compose BOM, Coil 3, Hilt 2.52, Custom Tabs).
- [ ] **Task 1.2:** Setup `settings.gradle.kts` and root `build.gradle.kts` with Compose compiler plugin (`org.jetbrains.kotlin.plugin.compose`).
- [ ] **Task 1.3:** Configure `app/build.gradle.kts` with `namespace = "com.dusrel.shrinkit"`, `compileSdk = 35`, and modern dependency aliases.
- [ ] **Task 1.4:** Author `app/src/main/AndroidManifest.xml` with `INTERNET`, `POST_NOTIFICATIONS`, `enableOnBackInvokedCallback="true"`, and `locales_config`.

### Phase 2: Brand Assets & Multi-Language (i18n)
- [ ] **Task 2.1:** Process [ShrinkIt 1024x1024 Master Icon](file:///d:/0/1/shrinkit/chrome-extension/icons/profile-1024.png) into Android Adaptive Icon layers (`ic_launcher`, `ic_launcher_round`, `ic_launcher_foreground`, `ic_launcher_monochrome`) across `mdpi`, `hdpi`, `xhdpi`, `xxhdpi`, `xxxhdpi`.
- [ ] **Task 2.2:** Author default English strings in `res/values/strings.xml`.
- [ ] **Task 2.3:** Author Hindi translations in `res/values-hi/strings.xml`.
- [ ] **Task 2.4:** Author Assamese translations in `res/values-as/strings.xml`.
- [ ] **Task 2.5:** Create `res/xml/locales_config.xml` and `LocaleHelper.kt` supporting in-app switching and Android 13+ `LocaleManager`.

### Phase 3: Core Domain Engine (`com.dusrel.shrinkit.domain.engine`)
- [ ] **Task 3.1:** Implement `SizeUnits.kt` mirroring [src/lib/image/units.ts](file:///d:/0/1/shrinkit/src/lib/image/units.ts) (px, %, cm, mm, inches at 300 DPI).
- [ ] **Task 3.2:** Implement `Limits.kt` mirroring [src/lib/image/limits.ts](file:///d:/0/1/shrinkit/src/lib/image/limits.ts) (dimension bounds and memory safety).
- [ ] **Task 3.3:** Implement `SafeBitmapDecoder.kt` with `inJustDecodeBounds` downsampling to prevent OOM on 50MP–108MP phone camera photos.
- [ ] **Task 3.4:** Implement `ResizeEngine.kt` supporting `Stretch`, `Crop` (center-crop), and `Fit` (letterbox with color/alpha).
- [ ] **Task 3.5:** Implement `BinarySearchCompressor.kt` for exact Target KB convergence (`50 KB`, `20 KB`, etc.).
- [ ] **Task 3.6:** Implement `ExifSanitizer.kt` using AndroidX `ExifInterface` to strip GPS, camera metadata, and date/time.

### Phase 4: Unit Test Suite
- [ ] **Task 4.1:** Author unit tests for `SizeUnitsTest.kt` verifying exact rounding and formula equivalence with web.
- [ ] **Task 4.2:** Author unit tests for `LimitsTest.kt` testing dimension boundaries.
- [ ] **Task 4.3:** Author unit tests for `ResizeEngineTest.kt` validating aspect crop rectangle calculations and fit box constraints.
- [ ] **Task 4.4:** Run `./gradlew test` and confirm 100% pass rate.

### Phase 5: Presentation Layer & Services
- [ ] **Task 5.1:** Implement `BrowserLauncher.kt` using AndroidX Custom Tabs to launch external URLs in the user's default browser.
- [ ] **Task 5.2:** Implement `NotificationHelper.kt` with `POST_NOTIFICATIONS` channel for batch background compression alerts.
- [ ] **Task 5.3:** Build `ShrinkItTheme.kt` with Light and Graphite Dark palette matching [src/styles/global.css](file:///d:/0/1/shrinkit/src/styles/global.css).
- [ ] **Task 5.4:** Build `PresetChipsRow.kt` for one-tap selection (Passport 35x45mm, Signature 20KB, Instagram, YouTube).
- [ ] **Task 5.5:** Build `SettingsPanel.kt` with numeric inputs, unit dropdown, aspect lock toggle, and Target KB / Quality slider toggle.
- [ ] **Task 5.6:** Build `ImagePickerSection.kt` utilizing the modern zero-permission Android Photo Picker (`ActivityResultContracts.PickMultipleVisualMedia`).

### Phase 6: Result & Gallery Export
- [ ] **Task 6.1:** Build `ResultCard.kt` displaying original vs processed size badges and percent reduction badge.
- [ ] **Task 6.2:** Implement `MediaStoreExporter.kt` using `IS_PENDING` atomic writes to save processed bitmaps to `Pictures/ShrinkIt` without file corruption.
- [ ] **Task 6.3:** Build native Android Share Intent integration.

### Phase 7: Production Verification
- [ ] **Task 7.1:** Run `./gradlew test` to verify all unit tests pass with exit code 0.
- [ ] **Task 7.2:** Run `./gradlew lintDebug` and resolve any compiler warnings.
- [ ] **Task 7.3:** Assemble debug APK (`./gradlew assembleDebug`) and verify clean build artifact creation.
