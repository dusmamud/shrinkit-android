# ShrinkIt Android 📱⚡

> **Free, privacy-first native Android image resizer, compressor, and format converter.**
> Built with **Kotlin 2.4+ & Jetpack Compose (Material 3)**.

Companion web application: [ShrinkIt Web](https://shrinkit.dusrel.com)

> 🚧 **Status:** Planning phase — architecture & Play Store compliance docs are complete. Code scaffolding (Phase 1) starts next.

---

## 🌟 Planned Highlights

- **🔒 100% On-Device Processing:** Photos never leave the phone. No uploads, no analytics.
- **🎯 Deterministic File Size Targeting:** Binary-search compressor converges on exact targets (`20 KB`, `50 KB`) for government forms and exams.
- **📐 Print-Ready Units:** `px`, `%`, `cm`, `mm`, `inches` with honest 300 DPI handling.
- **🛡️ EXIF & GPS Scrubber:** Strips camera metadata and location by default.
- **✨ Android 16 (API 36) Ready:** Edge-to-edge, predictive back, Material You themed icons.
- **🚀 Zero-Permission Photo Picker:** Modern Android Photo Picker, no storage permission dialogs.
- **⚙️ Resilient Background Batches:** WorkManager with foreground-service progress notifications.
- **🌐 Multilingual:** English, Hindi (हिंदी), Assamese (অসমীয়া) with per-app language preferences.

---

## 🏗️ Tech Stack

- **UI:** Jetpack Compose + Material 3 (BOM `2026.09.00`)
- **Language:** Kotlin 2.4.10 (Compose Compiler Gradle plugin)
- **Build:** Android Gradle Plugin 9.4.0 + Gradle Version Catalog
- **Image Pipeline:** `ImageDecoder` (API 28+, AVIF on API 31+, HEIF codec check) + `BitmapFactory` fallback + Coil 3
- **DI:** Dagger Hilt (KSP codegen)
- **Background:** WorkManager 2.12.0
- **Architecture:** Clean Architecture + MVVM + UDF + Type-safe Navigation

---

## 📂 Repository Layout

```
shrinkit-android/
├── AGENTS.md          # Project invariants & conventions
├── docs/
│   ├── PRD.md         # Product Requirements Document
│   ├── TRD.md         # Technical Requirements Document
│   ├── PLAN.md        # 7-Phase Implementation Plan
│   ├── STATUS.md      # Milestone tracking & risk registry
│   └── MODULE-MAP.md  # Web ↔ Android parity mapping
└── app/               # Android application module (Phase 1)
```

---

## 📜 License & Ownership

Copyright © 2026 Dusrel. All Rights Reserved.
