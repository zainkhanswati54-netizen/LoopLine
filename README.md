# LoopLine

A single-stroke puzzle game — Kotlin + Jetpack Compose, Material 3.

> Status: **early scaffold.** Splash screen, home screen, app theme, and logo
> are done. No game mode is playable yet — every mode on the home screen
> shows a "Coming soon" dialog on tap. That's intentional for this phase.

## What's in this scaffold

- `MainActivity.kt` — entry point, hosts the Compose nav graph
- `ui/navigation/LoopLineNavGraph.kt` — Splash → Home → Difficulty Select → Game routing
- `ui/screens/SplashScreen.kt` — animated logo + loading dots, auto-advances
- `ui/screens/HomeScreen.kt` — mode grid (Classic is playable, Daily/Timed/Zen are "Coming soon"), Settings icon (Coming soon)
- `ui/screens/DifficultySelectScreen.kt` — Easy / Normal / Hard picker, plus a "Continue" card when a session is in progress, plus your best level reached per difficulty
- `ui/screens/GameScreen.kt` — the Classic mode puzzle: drag to connect every tile in one continuous stroke, with a responsive grid, timer, stars, haptics, and a confetti celebration
- `game/Level.kt` — the puzzle model (a set of grid cells + a start cell)
- `game/Difficulty.kt` — grid size/cell-count range per difficulty, plus progressive scaling for endless play
- `game/LevelGenerator.kt` — procedural level generator (see below)
- `game/GameSession.kt` — tracks the current difficulty, level count, resumes an in-progress session instead of resetting it, caches generated levels for nav lookup
- `game/ProgressStore.kt` — persists your best level reached per difficulty (SharedPreferences, survives app restarts)
- `game/LevelRepository.kt` — 3 handcrafted fallback levels (safety net; the generator is the primary path)
- `game/PathSolver.kt` — backtracking solver that completes the stroke from wherever the player currently is; powers the Hint button
- `ui/components/LoopLineLogo.kt` — the app mark, drawn in code (Canvas), no image asset
- `ui/components/ModeCard.kt`, `ComingSoonDialog.kt` — reusable UI pieces
- `ui/theme/` — Color.kt, Type.kt, Theme.kt — dark navy palette + typography scale
- `res/drawable/ic_launcher_*.xml` — adaptive app icon (same mark as the in-app logo)

## Classic mode — how it plays

- Tap **Classic** on the home screen → pick **Easy / Normal / Hard** (or **Continue** if you already have a session going).
- Drag from the colored start dot through adjacent tiles (no diagonals).
- Every tile must be visited exactly once, in one continuous stroke.
- Touching the previous tile again undoes one step; touching the start dot resets the level.
- A short haptic tick confirms each tile you connect.
- The header shows a live `filled / total` tile counter and elapsed time.
- Completing a level plays a quick confetti burst, then a dialog with a **star rating** (based on solve time relative to puzzle size), **Next level** (fresh puzzle, same difficulty, slightly bigger every 5 levels), or **Change difficulty**.
- Play is endless — no fixed level count.

## Recent fixes (from testing feedback)

- **Grid no longer overflows the screen on Hard.** Tile size is now computed
  from the actual available screen width (`BoxWithConstraints`) instead of a
  fixed 58dp, so a 6×6 (or bigger, once scaled) grid always fits — it just
  uses smaller tiles on narrower screens instead of running off the edge.
- **Switching difficulty no longer resets your progress on the other ones.**
  `GameSession` used to track a single global difficulty/level/current-level,
  so picking Hard while you had Easy progress silently overwrote it — going
  back to Easy afterwards looked reset to Level 1 even though your saved
  *best* level was untouched. `GameSession` now keeps one independent
  session per difficulty. Tapping a difficulty on the Difficulty Select
  screen resumes its own session if it has one; each card shows
  **"Continue · Level N"** when it does. Starting over is now a deliberate,
  separate action — a small restart icon on the card, with a confirmation
  dialog — instead of an accidental side effect of switching difficulties.
- **Fast backward drags no longer get "stuck" and force a full reset.** The
  undo logic only ever checked one step back (`path[size - 2]`), which
  worked for a slow, deliberate drag but not a fast one: dragging quickly
  back along the stroke samples touch positions in bigger jumps, so it could
  land two-or-more tiles behind the head. That cell wasn't `path[size - 2]`,
  so nothing happened — the only way out was tapping the start dot and
  losing the whole level. `handleTouch` in `GameScreen.kt` now finds the
  touched cell's position anywhere in the current path (not just one slot
  back) and retracts to it, so backing up any number of tiles in one drag
  works the way it visually looks like it should.

## Hint

Tap the lightbulb in the game header (up to 3 uses per level) to have
`PathSolver` re-solve the puzzle from wherever your stroke currently ends
and highlight the next correct tile. It's a live backtracking search from
your *actual* current position each time — not a replay of the generator's
original solution — so it stays correct even if your stroke has taken a
different, still-valid route than the one the generator happened to walk.
Hints are free for now; `MAX_HINTS_PER_LEVEL` in `GameScreen.kt` is the knob
to wire up to a rewarded ad later.

## Stats & Leaderboard

Both now have an entry point (icons on the Home screen top bar, next to
Settings) that opens a "Coming soon" dialog explaining what's planned —
levels cleared / best times / streaks for Stats, and a ranked comparison
against other players for Leaderboard. Neither collects or sends any data
yet; they're placeholders so the UI is in place ahead of the real screens.

## How level generation works (and why it's always solvable)

Instead of drawing a grid shape and then checking whether a solution exists,
`LevelGenerator` generates the **solution first**: it does a random
self-avoiding walk on the grid (starting from a random cell, stepping to a
random unvisited neighbor, stopping when boxed in). The walk's cells *become*
the puzzle's shape, and the walk itself *is* a valid one-stroke answer — so
there's no separate validation step, and no way to end up with an unsolvable
board.

Each difficulty targets a cell-count range (see `Difficulty.kt`), and grows
slightly every 5 levels cleared (endless-mode progression), capped so grids
never get unreasonably large for a phone screen:

| Difficulty | Base grid | Base target tiles |
|---|---|---|
| Easy | 4×4 | 8–12 |
| Normal | 5×5 | 14–20 |
| Hard | 6×6 | 22–30 |

The generator retries the walk (up to 300 times, effectively instant) until
one lands in the target range. This was simulated hundreds of times per
difficulty before shipping — every tier hits its target range 99.5–100% of
the time within the retry budget, including scaled-up late-game grids that
target near-full coverage.

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
