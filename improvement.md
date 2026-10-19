# ShrinkIt Android — UI Improvement Plan (Fonts & Icons)

> Status: RESOURCES ADDED, code changes PENDING (user approval required).
> This doc is the cross-verified plan. No Kotlin/XML code has been changed yet.

## 1. What was added (2026-10-19)

### Fonts — `app/src/main/res/font/`
| File | Source | License |
|---|---|---|
| `poppins_regular.ttf` (156 KB) | google/fonts, Poppins 400 | SIL OFL 1.1 — commercial OK |
| `poppins_medium.ttf` (155 KB) | google/fonts, Poppins 500 | SIL OFL 1.1 — commercial OK |
| `poppins_semibold.ttf` (154 KB) | google/fonts, Poppins 600 | SIL OFL 1.1 — commercial OK |
| `poppins_bold.ttf` (152 KB) | google/fonts, Poppins 700 | SIL OFL 1.1 — commercial OK |
| `material_symbols_rounded.ttf` (66 KB) | google/material-design-icons, variable font subset to 21 used icons, FILL=0 (outlined/regular) | Apache 2.0 — commercial OK |
| `material_symbols_rounded_filled.ttf` (65 KB) | same source, FILL=1 instance, subset to 21 used icons | Apache 2.0 — commercial OK |

Subset method: `fontTools.subset` on the official variable TTF, keeping only the
21 icon codepoints used by the app (15.2 MB → ~65 KB each). Verified: glyphs present
at their codepoints (e.g. home U+E9B2, settings U+E8B8).

### Why these
- **Poppins**: user-confirmed choice. OFL 1.1 = free for commercial apps, no paid
  license needed. Same family already self-hosted on shrinkit.dusrel.com (brand consistency).
- **Material Symbols Rounded**: Google's current icon system. Per developer.android.com,
  the old `material-icons-extended` artifact (currently used: `Icons.Filled.*`) is
  **no longer maintained or recommended** — "older look and feel" + slows builds.
  Official recommendation: Material Symbols. Rounded style = the soft look the user asked for.

## 2. Poppins typography plan (Material 3 scale)

| M3 style | Poppins weight | Usage in app |
|---|---|---|
| displayLarge/Medium | SemiBold 600 | Onboarding headlines |
| headlineSmall | SemiBold 600 | Screen titles |
| titleLarge | Medium 500 | Section headers, dialog titles |
| titleMedium | Medium 500 | Card titles, list headers |
| bodyLarge/Medium | Regular 400 | Body text, descriptions |
| labelLarge | Medium 500 | Buttons |
| labelSmall | Regular 400 | Captions, helper text |

Implementation (later): `res/font/poppins.xml` font-family + custom `Typography` in
`presentation/theme/Theme.kt` replacing `Typography()` defaults.

## 3. Material Symbols Rounded — usage approach

Icons render via the icon font (codepoint text), NOT `Icons.Filled.*`:

```kotlin
// Outlined (regular)
Text(
    text = "\uE9B2", // home
    fontFamily = FontFamily(Font(R.font.material_symbols_rounded)),
    ...
)
// Filled
Text(
    text = "\uE9B2",
    fontFamily = FontFamily(Font(R.font.material_symbols_rounded_filled)),
    ...
)
```

(Later: wrap in a `ShrinkItIcon(name)` composable mapping names → codepoints,
so call sites stay readable.)

Codepoint map (from official `.codepoints` file, verified present in both subsets):
arrow_back E5C4, bolt EA0B, camera_alt E412, check E668, chevron_right E5CC,
close E5CD, delete E92E, history E8B3, home E9B2, info E88E, language EA07,
palette E40A, photo_library E413, photo_size_select_large E433, picture_as_pdf E415,
settings E8B8, share E80D, shield E9E0, speed E9E4, star F09A, translate E8E2.

## 4. Filled vs Regular — per-icon mapping (M3 cross-verified)

Rules (m3.material.io):
- **Navigation bar**: filled = active destination, outlined = inactive destinations.
- **Everything else**: outlined (regular) is the default — "light, clean style that
  works well in dense UIs". Filled only for primary emphasis / active states.

| Icon | Where | Style | Rationale |
|---|---|---|---|
| home | Bottom tab | filled when selected / outlined when not | M3 nav rule |
| history | Bottom tab | filled when selected / outlined when not | M3 nav rule |
| settings | Bottom tab | filled when selected / outlined when not | M3 nav rule |
| arrow_back | TopBar navigation | outlined | M3: nav icon, not an active state |
| language | TopBar action | outlined | secondary action |
| share | History item | outlined | secondary action |
| delete | History item | outlined | secondary action |
| check | CropEditor confirm | **filled** | primary action emphasis |
| close | CropEditor cancel | outlined | secondary action |
| chevron_right | Settings row trailing | outlined | navigation hint, not active |
| palette | Settings theme row | outlined | list leading icon |
| info | Settings about row | outlined | list leading icon |
| star | Settings rate row | outlined | list leading icon |
| bolt | Onboarding page 3 | **filled** | feature illustration, large |
| photo_size_select_large | Onboarding page 2 | **filled** | feature illustration, large |
| shield | Onboarding page 1 + Home feature | **filled** (onboarding) / outlined (feature list) | illustration vs dense UI |
| photo_library | Home quick action | outlined | action tile |
| camera_alt | Home quick action | outlined | action tile |
| picture_as_pdf | Home quick action | outlined | action tile |
| history | Home quick action | outlined | action tile |
| speed | Home "why" feature | outlined | dense feature list |
| translate | Home "why" feature | outlined | dense feature list |

## 5. Implementation steps (DO NOT START without user go-ahead)

1. `res/font/poppins.xml` — FontFamily (400/500/600/700).
2. `presentation/theme/Theme.kt` — Poppins `Typography`; dark/light colors stay as-is.
3. `ShrinkItIcon` composable — name→codepoint map for both fonts (regular + filled).
4. Replace all 25 `Icons.Filled.*` call sites per the table above
   (MainScreen tabs need selected/unselected switching).
5. Remove `androidx.compose.material:material-icons-extended` dependency
   (Google: unmaintained; also cuts build time).
6. Re-run CI (unit tests + lint + assemble) to green.
7. OFL/Apache license texts: add `res/raw/` or About-screen attribution
   (OFL requires the license text to ship with the font).

## 6. Open questions for the user

- Bottom-tab active indicator: keep M3 default pill, or custom?
- Onboarding illustrations: keep filled icons, or custom artwork later?
