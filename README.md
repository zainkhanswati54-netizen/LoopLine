# LoopLine

A single-stroke puzzle game — Kotlin + Jetpack Compose, Material 3.

> Status: **early scaffold.** Splash screen, home screen, app theme, and logo
> are done. No game mode is playable yet — every mode on the home screen
> shows a "Coming soon" dialog on tap. That's intentional for this phase.

## What's in this scaffold

- `MainActivity.kt` — entry point, hosts the Compose nav graph
- `ui/navigation/LoopLineNavGraph.kt` — Splash → Home routing
- `ui/screens/SplashScreen.kt` — animated logo + loading dots, auto-advances
- `ui/screens/HomeScreen.kt` — mode grid (Classic / Daily / Timed / Zen), all "Coming soon"
- `ui/components/LoopLineLogo.kt` — the app mark, drawn in code (Canvas), no image asset
- `ui/components/ModeCard.kt`, `ComingSoonDialog.kt` — reusable UI pieces
- `ui/theme/` — Color.kt, Type.kt, Theme.kt — dark navy palette + typography scale
- `res/drawable/ic_launcher_*.xml` — adaptive app icon (same mark as the in-app logo)

## Color palette

| Token | Hex | Use |
|---|---|---|
| Background | `#14152B` | app background |
| Surface | `#1E1F3B` | cards |
| Tile outline | `#3A3B5C` | grid tiles in the logo |
| Accent blue | `#2D9CF0` | primary accent |
| Accent orange | `#F6A623` | secondary accent |
| Accent green | `#4CAF50` | tertiary accent |

## Fonts

Typography currently uses the system default font with the reference app's
weight/size scale. If you want the exact rounded look from the reference
screenshots, download **Poppins**, **Nunito**, or **DM Sans** from
[fonts.google.com](https://fonts.google.com), drop the `.ttf` files into
`app/src/main/res/font/`, and point `LoopLineFontFamily` in `Type.kt` at them.

## Opening the project

1. Open Android Studio (Koala or newer recommended) → **Open** → select the `LoopLine` folder.
2. Let Gradle sync. Android Studio will generate the Gradle wrapper (`gradlew`) automatically on first sync if it's missing.
3. Run on an emulator or device (minSdk 24 / Android 7.0+).

## Pushing to GitHub

```bash
cd LoopLine
git init
git add .
git commit -m "Initial commit: splash, home screen, theme, logo"
git branch -M main
git remote add origin https://github.com/<your-username>/<your-repo>.git
git push -u origin main
```

## Building an APK via GitHub (no Android Studio needed)

This repo includes a GitHub Actions workflow (`.github/workflows/build-apk.yml`)
that builds a debug APK automatically.

1. Push the repo to GitHub (see steps above).
2. Go to your repo on GitHub → **Actions** tab. The workflow runs
   automatically on every push to `main` (or trigger it manually: Actions →
   **Build APK** → **Run workflow**).
3. Once the run finishes (green check), open it → scroll to
   **Artifacts** → download **LoopLine-debug-apk** (a zip containing the `.apk`).
4. Unzip it, copy the `.apk` to your phone, and install it. You'll need to
   allow "install from unknown sources" for whichever app you use to open it
   — this is a normal debug build, self-signed by the Android build tools,
   not a Play Store release.

This debug APK is fine for testing on your own device. For a real release
(Play Store or sharing widely) you'd eventually want a signed release build
with your own keystore — ask when you're ready to set that up.

## Suggested next steps

- Build the `Classic` mode board (grid state, path-drawing gesture, win check)
- Swap the placeholder icon set colors if you land on a different mode-count
- Add level data (JSON or a simple Kotlin data class list) once Classic is playable
- Wire real navigation from a mode card into a level-select screen instead of the Coming Soon dialog
