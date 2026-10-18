# Project Status: ShrinkIt Android

> **Current Milestone:** Phase 1 — Project Scaffolding & Initial Architecture Setup  
> **Package:** `com.dusrel.shrinkit`  
> **Target SDK:** 36 (Android 16)  
> **Last Updated:** 2026-10-19  
> **Health:** 🟢 Green (Google Play API 36 Verification & Modern 2026 Stack Locked)

---

## Active Task
- **Current Task:** Fixed targetSdk 35 $\to$ 36 (Android 16), updated Version Catalog to 2026 stack (AGP 9.4, Kotlin 2.4, Compose BOM 2026.09, WorkManager 2.12), corrected AVIF API 31+ & HEIC device-codec check, and declared foreground service permissions.
- **Next Task:** Start Phase 1 code scaffolding (`gradle/libs.versions.toml`, `settings.gradle.kts`, `build.gradle.kts`).

---

## Milestone Progress

- [x] **Milestone 0: Analysis, Version Audit & Planning**
  - [x] Analyzed previous Android projects (`D:\01\1\melofy` Jetpack Compose, `KumotDev` Flutter).
  - [x] Selected Native Kotlin + Jetpack Compose stack.
  - [x] Researched latest stable 2026 libraries: Kotlin 2.4.10, AGP 9.4.0, Compose BOM 2026.09.00, Coil 3.6.3, Hilt 2.52, WorkManager 2.12.0, Custom Tabs, Core SplashScreen, Play Review, Play App Update.
  - [x] Audited Android 16 targetSdk 36 invariants: Edge-to-Edge mandatory, Predictive back default-on, Photo Picker, Scoped Storage `IS_PENDING`.
  - [x] Audited ImageDecoder Dual Engine: AVIF on API 31+, HEIF with `isMimeTypeSupported` check, auto EXIF orientation baking.
  - [x] Audited Google Play Store policies: Target SDK 36 compliance (August 31, 2026 mandate), 16 KB page-size support (inherently safe, Feb 1, 2027 update deadline), Data Safety questionnaire pre-fill, 12-tester / 14-day closed track.
  - [x] Updated all durable memory files (`AGENTS.md`, `PRD.md`, `TRD.md`, `PLAN.md`, `STATUS.md`, `MODULE-MAP.md`, `README.md`).
  - [x] Pushed clean commit to public GitHub repository `shrinkit-android`.
- [ ] **Milestone 1: Project Scaffolding & Version Catalog**
- [ ] **Milestone 2: Brand Assets, Splash Screen & Multi-Language (EN, HI, AS)**
- [ ] **Milestone 3: Core Domain Engine & Web Parity Algorithms**
- [ ] **Milestone 4: Automated Unit Testing**
- [ ] **Milestone 5: Presentation & Jetpack Compose UI**
- [ ] **Milestone 6: Background Processing & MediaStore Export**
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
