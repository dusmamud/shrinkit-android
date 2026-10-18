# Module Map & Cross-Project Parity Matrix

This document maps all features and modules of **ShrinkIt Android** (`com.dusrel.shrinkit`) to their reference web counterparts in [d:\0\1\shrinkit](file:///d:/0/1/shrinkit).

---

## Parity & Mapping Table

| Feature / Domain | Web Reference File | Android Target Implementation | Tests |
| :--- | :--- | :--- | :--- |
| **Unit Conversions (px, %, cm, in, mm)** | [src/lib/image/units.ts](file:///d:/0/1/shrinkit/src/lib/image/units.ts) | `domain/engine/SizeUnits.kt` | `SizeUnitsTest.kt` (Matches [units.test.ts](file:///d:/0/1/shrinkit/tests/unit/units.test.ts)) |
| **Safety Limits & Guards** | [src/lib/image/limits.ts](file:///d:/0/1/shrinkit/src/lib/image/limits.ts) | `domain/engine/Limits.kt` | `LimitsTest.kt` (Matches [limits.test.ts](file:///d:/0/1/shrinkit/tests/unit/limits.test.ts)) |
| **Safe OOM Bitmap Decoding** | Browser OffscreenCanvas | `domain/engine/SafeBitmapDecoder.kt` | `SafeBitmapDecoderTest.kt` |
| **Resize Modes (Stretch, Crop, Fit)** | [src/lib/image/engine.ts](file:///d:/0/1/shrinkit/src/lib/image/engine.ts) | `domain/engine/ResizeEngine.kt` | `ResizeEngineTest.kt` (Matches [resize-modes.test.ts](file:///d:/0/1/shrinkit/tests/unit/resize-modes.test.ts)) |
| **Target KB Binary Search Compression** | [docs/PLAN.md](file:///d:/0/1/shrinkit/docs/PLAN.md#L48-L59) | `domain/engine/BinarySearchCompressor.kt` | `BinarySearchCompressorTest.kt` |
| **EXIF & Privacy Stripper** | [src/lib/image/strip.ts](file:///d:/0/1/shrinkit/src/lib/image/strip.ts) | `domain/engine/ExifSanitizer.kt` | `ExifSanitizerTest.kt` (Matches [strip.test.ts](file:///d:/0/1/shrinkit/tests/unit/strip.test.ts)) |
| **Output File Naming** | [src/lib/image/filenames.ts](file:///d:/0/1/shrinkit/src/lib/image/filenames.ts) | `domain/util/FileNames.kt` | `FileNamesTest.kt` (Matches [filenames.test.ts](file:///d:/0/1/shrinkit/tests/unit/filenames.test.ts)) |
| **Theme & Color Tokens** | [src/styles/global.css](file:///d:/0/1/shrinkit/src/styles/global.css) | `presentation/theme/Color.kt` & `Theme.kt` | Visual Compose Previews |
| **App Shell & State** | [ShrinkItApp.tsx](file:///d:/0/1/shrinkit/src/components/react/ShrinkItApp.tsx) | `presentation/home/HomeScreen.kt` & `ShrinkItViewModel.kt` | Compose UI Tests |
| **Settings Controls** | [SettingsPanel.tsx](file:///d:/0/1/shrinkit/src/components/react/SettingsPanel.tsx) | `presentation/components/SettingsPanel.kt` | Compose UI Tests |
| **Result Card & Summary** | [ResultPanel.tsx](file:///d:/0/1/shrinkit/src/components/react/ResultPanel.tsx) | `presentation/components/ResultCard.kt` | Compose UI Tests |
| **Exam & Social Presets** | [tools/social](file:///d:/0/1/shrinkit/src/pages/tools/social-media-resizer.astro) | `presentation/components/PresetChipsRow.kt` | Unit & UI Tests |
| **Gallery / File Storage** | JSZip memory / blob URL | `data/storage/MediaStoreExporter.kt` (`IS_PENDING`) | Scoped Storage Unit / Instrumentation |
| **External Browser Navigation** | Web external `<a>` tags | `presentation/util/BrowserLauncher.kt` (Android Custom Tabs) | Unit / Intent verification |
| **Background Notifications** | - | `presentation/util/NotificationHelper.kt` (`POST_NOTIFICATIONS`) | Notification Channel verification |
| **Localization (EN, HI, AS)** | [messages/](file:///d:/0/1/shrinkit/messages) | `res/values/`, `res/values-hi/`, `res/values-as/`, `res/xml/locales_config.xml` | Android Locale Tests |
| **App Launcher Icons** | [profile-1024.png](file:///d:/0/1/shrinkit/chrome-extension/icons/profile-1024.png) | `res/mipmap-*/ic_launcher.png`, `ic_launcher_round.png` | Resource inspection |
