# Project Status: ShrinkIt Android

> **Current Milestone:** Phase 1 — Project Scaffolding & Initial Architecture Setup  
> **Package:** `com.dusrel.shrinkit`  
> **Last Updated:** 2026-10-19  
> **Health:** 🟢 Green (Google Play Verification, Version Catalog Audit & Planning 100% Complete)

---

## Active Task
- **Current Task:** Deep Exa cross-verification complete (WorkManager, ImageDecoder Dual Engine, Google Play In-App Updates, 12-Tester Closed Track compliance).
- **Next Task:** Start Phase 1 code scaffolding (`gradle/libs.versions.toml`, `settings.gradle.kts`, `build.gradle.kts`).

---

## Milestone Progress

- [x] **Milestone 0: Analysis, Version Audit & Planning**
  - [x] Analyzed previous Android projects (`D:\01\1\melofy` Jetpack Compose, `KumotDev` Flutter).
  - [x] Selected Native Kotlin + Jetpack Compose stack.
  - [x] Researched latest stable libraries: Kotlin 2.0+ Compose plugin, Compose BOM, Coil 3, Hilt 2.52, WorkManager 2.10, Custom Tabs, Core SplashScreen, Play Review, Play App Update.
  - [x] Audited Android 15 targetSdk 35 invariants: Edge-to-Edge, OOM defense, Photo Picker, Scoped Storage `IS_PENDING`.
  - [x] Audited ImageDecoder API 28+ native HEIC / AVIF decoding with auto EXIF orientation baking.
  - [x] Audited Google Play Store policies: Target SDK 35 compliance, 16 KB page-size support (verified zero native NDK bloat), Data Safety questionnaire pre-fill, 12-tester / 14-day closed track.
  - [x] Updated all durable memory files (`AGENTS.md`, `PRD.md`, `TRD.md`, `PLAN.md`, `STATUS.md`, `MODULE-MAP.md`).
  - [x] Created public GitHub repository and pushed `origin/main` with topics/tags.
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
| **HEIC / AVIF Decoding Failure on Modern Phones** | Critical | Use `ImageDecoder` on API 28+ (native HEIC/AVIF + auto EXIF orientation) with `BitmapFactory` fallback on API 24–27. |
| **Background Batch Worker Killed by Android OS** | High | Use `WorkManager` (`androidx.work:work-runtime-ktx`) with foreground notification service for resilient background crunching. |
| **Google Play Target SDK Policy** | High | Configured `targetSdk = 35` and `compileSdk = 35` meeting August 2025/2026 Play Store requirements. |
| **Google Play 16 KB Page Size Mandate** | High | Pure Kotlin/Java + AndroidX code. Verified zero native `.so` binaries, inherently 16KB safe. |
| **Android 15 Edge-to-Edge Obscuring UI** | High | Apply `enableEdgeToEdge()` + `Modifier.safeDrawingPadding()` / window insets on all root screens. |
| **50MP–108MP Photos OutOfMemoryError** | High | Use `BitmapFactory.Options.inJustDecodeBounds = true` with calculated `inSampleSize` power of two. |
| **Scoped Storage Corrupted Files** | Medium | Use `MediaStore.Images.Media.IS_PENDING = 1` during compression stream write, then flip to `0`. |
| **User Privacy Concerns on Photo Access** | Medium | Use modern zero-permission Android Photo Picker (`PickMultipleVisualMedia`). |
