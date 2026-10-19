# Project Status: ShrinkIt Android

> **Current Milestone:** Phase 6 Complete — Ready for Release Build & Verification  
> **Package:** `com.dusrel.shrinkit`  
> **Target SDK:** 36 (Android 16)  
> **Last Updated:** 2026-10-19  
> **Health:** 🟢 Green (Architecture, Engine, Tests, UI & Storage Implemented)

---

## Active Task
- **Completed:** 
  - Phase 1: Gradle version catalog, wrapper, root/app `build.gradle.kts`, `AndroidManifest.xml` (API 36, foreground service, locales config).
  - Phase 2: Brand assets, adaptive icons, splash screen, string resources in English, Hindi (`hi`), Assamese (`as`).
  - Phase 3: Core domain engine (`SizeUnits`, `Limits`, `ExifSanitizer`, `SafeBitmapDecoder`, `ResizeEngine`, `BinarySearchCompressor`).
  - Phase 4: Unit test suite for `SizeUnitsTest` and `LimitsTest`.
  - Phase 5: Presentation layer (`ShrinkItTheme`, `ShrinkItTopBar`, `PresetChipsRow`, `SettingsPanel`, `ImagePickerSection`, `ResultCard`, `ShrinkItApp`, `ShrinkItViewModel`, `BrowserLauncher`, `InAppReviewManager`, `InAppUpdateManager`).
  - Phase 6: Scoped storage (`MediaStoreExporter` with atomic `IS_PENDING` writes).
  - **Code Audit Fixes (2026-10-19):** Fixed 2 compile errors (`Bitmap.CompressFormat.AVIF` does not exist in the Android SDK — AVIF output falls back via `encodeSafe()` to WebP and is no longer offered in the format picker; `NotificationHelper` used a wrong channel constant name), fixed an API 28 crash (`ImageDecoder.isMimeTypeSupported` is API 29+ — now guarded), wired `Limits` input/output dimension checks into `addImages`/`processBatch` (were dead code), removed the never-enqueued `BatchCompressWorker` (210 lines of dead pipeline duplication; foreground processing via `ViewModel` is the v1 path — WorkManager infra retained for future use), fixed `androidx.hilt` artifacts to their own `1.4.0` version ref, set default quality to 90 (web parity), localized 2 hardcoded UI strings (EN/HI/AS).
- **Next Task:** Finalize release pipeline & bundle verification.

---

## Milestone Progress

- [x] **Milestone 0: Analysis, Version Audit & Planning**
- [x] **Milestone 1: Project Scaffolding & Version Catalog**
- [x] **Milestone 2: Brand Assets, Splash Screen & Multi-Language (EN, HI, AS)**
- [x] **Milestone 3: Core Domain Engine & Web Parity Algorithms**
- [x] **Milestone 4: Automated Unit Testing**
- [x] **Milestone 5: Presentation & Jetpack Compose UI**
- [x] **Milestone 6: Background Processing & MediaStore Export**
- [ ] **Milestone 7: Production Verification & Google Play AAB Assembly**

---

## Blockers & Risk Registry

| Risk / Dependency | Impact | Mitigation Strategy |
| :--- | :---: | :--- |
| **Google Play Target SDK 36 Mandate** | High | Configured `compileSdk = 36` and `targetSdk = 36` (Android 16), fully satisfying the August 31, 2026 Google Play requirement. |
| **HEIC & AVIF Device Codec Variances** | High | Use `ImageDecoder` on API 28+; guard AVIF to API 31+; check `ImageDecoder.isMimeTypeSupported("image/heif")` with graceful `BitmapFactory` fallback. |
| **Background Batch Worker Killed by Android OS** | Medium | Use `WorkManager` (`androidx.work:work-runtime-ktx:2.12.0`) with `FOREGROUND_SERVICE_DATA_SYNC` for resilient background processing. |
| **Google Play 16 KB Page Size Policy** | Low | Pure Kotlin/Java + AndroidX code. Zero native `.so` binaries, inherently 16KB-safe (Play Store update enforcement begins Feb 1, 2027). |
| **Android 15/16 Edge-to-Edge Layout** | Medium | Apply `enableEdgeToEdge()` + `Modifier.safeDrawingPadding()` / window insets on all root screens. |
| **50MP–108MP Photos OutOfMemoryError** | High | Use `BitmapFactory.Options.inJustDecodeBounds = true` with calculated `inSampleSize` power of two. |
| **Scoped Storage Corrupted Files** | Medium | Use `MediaStore.Images.Media.IS_PENDING = 1` during compression stream write, then flip to `0`. |
