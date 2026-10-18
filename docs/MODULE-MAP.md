# Module Map & Cross-Project Parity Matrix

This document maps all features and modules of **ShrinkIt Android** (`com.dusrel.shrinkit`) to their reference web counterparts.

---

## Parity & Mapping Table

| Feature / Domain | Web Reference File / Endpoint | Android Target Implementation | Tests |
| :--- | :--- | :--- | :--- |
| **Unit Conversions (px, %, cm, in, mm)** | `src/lib/image/units.ts` | `domain/engine/SizeUnits.kt` | `SizeUnitsTest.kt` |
| **Safety Limits & Guards** | `src/lib/image/limits.ts` | `domain/engine/Limits.kt` | `LimitsTest.kt` |
| **Dual Image Decoding** | Browser OffscreenCanvas | `domain/engine/SafeBitmapDecoder.kt` (ImageDecoder API 28+, AVIF API 31+, HEIF check) | `SafeBitmapDecoderTest.kt` |
| **Resize Modes (Stretch, Crop, Fit)** | `src/lib/image/engine.ts` | `domain/engine/ResizeEngine.kt` | `ResizeEngineTest.kt` |
| **Target KB Binary Search Compression** | Web target size logic | `domain/engine/BinarySearchCompressor.kt` | `BinarySearchCompressorTest.kt` |
| **EXIF & Privacy Stripper** | `src/lib/image/strip.ts` | `domain/engine/ExifSanitizer.kt` | `ExifSanitizerTest.kt` |
| **Output File Naming** | `src/lib/image/filenames.ts` | `domain/util/FileNames.kt` | `FileNamesTest.kt` |
| **Theme & Color Tokens** | `src/styles/global.css` | `presentation/theme/Color.kt` & `Theme.kt` | Visual Compose Previews |
| **App Shell & State** | `ShrinkItApp.tsx` | `presentation/home/HomeScreen.kt` & `ShrinkItViewModel.kt` | Compose UI Tests |
| **Settings Controls** | `SettingsPanel.tsx` | `presentation/components/SettingsPanel.kt` | Compose UI Tests |
| **Result Card & Summary** | `ResultPanel.tsx` | `presentation/components/ResultCard.kt` | Compose UI Tests |
| **Exam & Social Presets** | Social & ID presets | `presentation/components/PresetChipsRow.kt` | Unit & UI Tests |
| **Gallery / File Storage** | JSZip memory / blob URL | `data/storage/MediaStoreExporter.kt` (`IS_PENDING`) | Scoped Storage Unit / Instrumentation |
| **External Browser Navigation** | Web external links | `presentation/util/BrowserLauncher.kt` (Android Custom Tabs) | Unit / Intent verification |
| **Background Processing** | - | `presentation/worker/BatchCompressWorker.kt` (WorkManager 2.12.0) | WorkManager Test |
| **Background Notifications** | - | `presentation/util/NotificationHelper.kt` (`POST_NOTIFICATIONS`) | Notification Channel verification |
| **Localization (EN, HI, AS)** | Web `messages/` | `res/values/`, `res/values-hi/`, `res/values-as/`, `res/xml/locales_config.xml` | Android Locale Tests |
| **App Launcher Icons** | `profile-1024.png` | `res/mipmap-*/ic_launcher.png`, `ic_launcher_round.png` | Resource inspection |
