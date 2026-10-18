# ShrinkIt Android 📱⚡

> **Free, privacy-first native Android image resizer, compressor, and format converter.**  
> Built with **Kotlin 2.4+ & Jetpack Compose (Material 3)**.

Companion web application: [ShrinkIt Web](https://shrinkit.dusrel.com)

---

## 🌟 Key Highlights

- **🔒 100% On-Device Processing:** Photos are never uploaded to any remote server. Your personal images, IDs, and signatures stay on your phone.
- **🎯 Deterministic File Size Targeting (KB Mode):** Need an image under strictly `20 KB` or `50 KB` for a government form or entrance exam? Our binary search quantization compressor converges exactly on your target.
- **📐 Millimeters, Centimeters & 300 DPI Print Units:** Supports `px`, `%`, `cm`, `mm`, and `inches` with honest 300 DPI preservation for official forms and printing.
- **🛡️ Instant EXIF & GPS Scrubber:** Strips camera model, serial number, GPS coordinates, and personal timestamps by default before saving or sharing.
- **✨ Android 16 (API 36) Ready:** Full Edge-to-Edge display support, predictive back gestures, and Material You themed adaptive icons.
- **🚀 Modern Android Photo Picker:** Zero invasive runtime storage permissions required to pick photos.
- **⚙️ Resilient Background Batch Crunching:** Powered by WorkManager 2.12.0 with foreground service progress notifications.
- **🌐 Multilingual:** Available in **English**, **Hindi (हिंदी)**, and **Assamese (অসমীয়া)** with Android 13+ Per-App Language Preferences support.

---

## 🏗️ Architecture & Tech Stack

- **UI & Design:** Jetpack Compose + Material 3 (Material Symbols)
- **Language:** Kotlin 2.4.10+ (Compose Compiler Gradle Plugin `org.jetbrains.kotlin.plugin.compose`)
- **Build System:** Android Gradle Plugin 9.4.0 + Gradle Version Catalog (`gradle/libs.versions.toml`)
- **Compose BOM:** `2026.09.00`
- **Architecture:** Clean Architecture + MVVM + Unidirectional Data Flow (UDF) + Type-Safe Navigation Compose
- **Image Pipeline:** Dual Decoder (`ImageDecoder` API 28+, AVIF API 31+, HEIF codec check) + `BitmapFactory` fallback + Coil 3 + AndroidX `ExifInterface`
- **Background Jobs:** WorkManager 2.12.0 (`androidx.work:work-runtime-ktx`)
- **Dependency Injection:** Dagger Hilt 2.52
- **External Links:** AndroidX Browser (Custom Tabs)

---

## 📂 Project Structure

```
shrinkit-android/
├── AGENTS.md                  # Project invariants & agent memory
├── docs/
│   ├── PRD.md                 # Product Requirements Document
│   ├── TRD.md                 # Technical Requirements Document
│   ├── PLAN.md                # 7-Phase Implementation Plan
│   ├── STATUS.md              # Milestone tracking & risk registry
│   └── MODULE-MAP.md          # Parity mapping matrix
├── app/                       # Android application module
│   ├── src/main/java/com/dusrel/shrinkit/
│   │   ├── di/                # Hilt Dependency Injection
│   │   ├── domain/engine/     # Resizer, Compressor, Safe Decoder & Units math
│   │   ├── presentation/      # Jetpack Compose UI & ViewModels
│   │   ├── worker/            # WorkManager background batch processor
│   │   └── data/storage/      # MediaStore Scoped Storage exporter
│   └── src/main/res/          # Values (EN), Values-HI, Values-AS, Drawables
└── gradle/
    └── libs.versions.toml     # Gradle Version Catalog
```

---

## 📜 License & Ownership

Copyright © 2026 Dusrel. All Rights Reserved.
