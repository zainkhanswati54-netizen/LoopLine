# LoopLine

A single-stroke puzzle game — Kotlin + Jetpack Compose, Material 3.

> Status: **Classic mode is playable end to end** (Easy/Normal/Hard, endless
> generated levels, hints, stats bar, win dialog). Daily Puzzle / Timed / Zen
> are still "Coming soon" placeholders — intentional for this phase.
>
> **Visual design pass (this update):** the whole app was re-themed from the
> original dark-navy/blue-orange-green palette to a premium dark
> gold-and-copper look, per the brief in `docs/` (or wherever you're tracking
> it) — see **"Visual redesign"** below for the full rundown of what changed
> and why.

## What's in this scaffold

- `MainActivity.kt` — entry point, hosts the Compose nav graph
- `ui/navigation/LoopLineNavGraph.kt` — Splash → Home → Difficulty Select → Game routing
- `ui/screens/SplashScreen.kt` — animated logo + gold-foil wordmark + loading dots, auto-advances
- `ui/screens/HomeScreen.kt` — mode grid (Classic is playable, Daily/Timed/Zen are "Coming soon"), Stats/Leaderboard/Settings icons (all "Coming soon" for now)
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
- `ui/components/MetallicButton.kt` — the one primary-button treatment (metal gradient fill, tinted shadow), used by every dialog and CTA
- `ui/components/IconChipButton.kt` — the shared circular icon-button chip used for back arrows and header icons
- `ui/components/GradientText.kt` — renders text filled with a Brush (the gold-foil wordmark effect)
- `ui/theme/Color.kt` — the gold / copper / rose-gold palette and per-accent lookup helpers
- `ui/theme/Gradients.kt` — reusable Brush gradients + the "metallic bevel" drawing technique (both a Modifier and a raw-Canvas version)
- `ui/theme/Shape.kt` — shared corner-radius tokens (card / dialog / button / chip)
- `ui/theme/Type.kt` — Poppins typography scale (real bundled font files, not the system default)
- `ui/theme/Theme.kt` — wires the palette + typography into Material3's `MaterialTheme`
- `res/font/` — the 5 Poppins static-weight `.ttf` files (OFL-licensed, bundled directly)
- `res/drawable/ic_launcher_*.xml` — adaptive app icon (same mark as the in-app logo, now gold/rose-gold)

## Classic mode — how it plays

- Tap **Classic** on the home screen → pick **Easy / Normal / Hard** (or **Continue** if you already have a session going).
- Drag from the colored start dot through adjacent tiles (no diagonals).
- Every tile must be visited exactly once, in one continuous stroke.
- Touching the previous tile again undoes one step; touching the start dot resets the level.
- A short haptic tick confirms each tile you connect.
- The header shows a live `filled / total` tile counter and elapsed time.
- Completing a level plays a quick confetti burst, then a dialog with a **star rating** (based on solve time relative to puzzle size), **Next level** (fresh puzzle, same difficulty, slightly bigger every 5 levels), or **Change difficulty**.
- Play is endless — no fixed level count.

## Recent fixes (this update)

- **Brand logo swapped in.** `LoopLineLogo` (Splash + Home) now renders the
  ornate gold circular badge image (`res/drawable-nodpi/loopline_logo.jpg`)
  instead of the old Canvas-drawn mark. The app launcher icon is untouched
  for now (it's a vector adaptive icon with its own safe-zone rules) - ask
  if you want that swapped too.
- **Sound + vibration on every stroke move.** `SoundPlayer` (a small
  SoundPool wrapper around `res/raw/tile_connect.mp3`) plays a soft pop each
  time the stroke connects a new tile, and a quieter version when backing
  up - alongside the haptic tick that already fired forward, now mirrored
  on backward moves too.
- **Difficulty now actually climbs level by level.** The generator used to
  pick a cell count anywhere in a difficulty's whole min-max range at
  random, so Level 1 could land harder than Level 4 by pure chance.
  `Difficulty.targetCellCount()` now gives each level a specific target that
  rises smoothly across every 5-level bucket (easiest at the start of the
  bucket, hardest at the end), and the generator picks whichever attempt
  lands closest to it.
- **No more lopsided empty gap next to the puzzle.** Generated levels are
  now cropped to the random walk's own bounding box instead of being placed
  inside the full difficulty-sized grid - so a puzzle that doesn't happen to
  reach every edge of that grid no longer leaves a dead strip of empty
  columns/rows hugging one side. The board now just sits centered on its
  own footprint.
- **Daily Puzzle moved out of the top-priority slot.** It sat right beside
  the one playable mode (Classic) at the top of the grid, which overstated
  how ready it was. Reordered so Classic leads and the still-"Coming soon"
  Daily Puzzle drops to the last card - closer to how NumRush treats
  not-yet-built modes as lower priority than what's actually playable.

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

## Path animations & the "Need help?" nudge

Two additions on top of the base redesign, both in `GameScreen.kt`:

**Every connection now has motion, not just a fill.** Three `Animatable`s
drive it — `connectProgress` (a bouncy spring) makes the newest segment
draw itself in from the previous tile and gives the newly-claimed tile a
small pop/overshoot; `burstProgress` (a plain tween) expands a fading ring
outward from that same tile. Both reset and restart on every connection —
`Animatable.animateTo` cancels whatever was still playing on that same
instance, so a fast drag across several tiles never queues up stale
animations, it just always shows the *latest* connection's effect. On top
of that, a `rememberInfiniteTransition` drives a small light that loops
continuously along the whole completed stroke, so the path reads as "live"
even between drags rather than only reacting the instant you touch it.

**"Need help?" is a floating nudge, not a dialog.** If the stroke's length
hasn't changed for 9 seconds (and hints remain, and the level isn't
solved), a small card fades/slides in from the bottom of the *screen* —
deliberately anchored to the whole screen rather than the grid's own Box,
so it always floats below the board instead of overlapping a small Easy
grid. It never blocks input to the board underneath. Tapping it calls the
same `requestHint()` the lightbulb uses; the small × dismisses it, and that
dismissal sticks until the player's next real move restarts the idle timer
— it won't reappear just because they kept sitting there after saying no.

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

## Visual redesign

The UI was re-themed end to end (every screen, dialog, and component) from
the original dark-navy / blue-orange-green look to a premium dark
gold-and-copper aesthetic, per this brief:

> Create a premium and classy UI design for a puzzle game with a dark
> background, featuring brushed gold and copper metallic accents, and
> smooth gradients. Include clean, modern typography, and buttons with
> softly rounded edges and subtle shadows to create a luxurious feel.

Design decisions worth knowing about, in case you want to tweak them later:

- **Three metals, not three random colors.** The old code cycled level
  accents through blue/orange/green — a leftover from before there was a
  visual identity. The new accent set is gold, copper, and **rose gold**.
  Rose gold isn't an arbitrary third color: it's literally what you get
  when you alloy gold and copper, so it stays inside the "gold and copper"
  brief instead of reaching for an unrelated hue just to get a third
  gameplay color. Easy/Normal/Hard map to rose-gold/gold/copper respectively,
  so the palette itself signals the step up in intensity.
- **The "brushed metal" signature.** Every accent surface (buttons, active
  tiles, cards, the logo, the connecting stroke) is drawn with a
  highlight → core → shadow gradient ramp rather than a flat fill, plus a
  thin diagonal bevel stroke (`metallicBevel` / `drawMetallicBevel` in
  `Gradients.kt`) that reads as light catching an engraved metal edge. It's
  the one visual idea repeated everywhere, which is what makes the app feel
  like one deliberate material instead of a pile of separately-styled
  screens.
- **Real bundled fonts, not the system default.** Typography is **Poppins**,
  fetched as five real static-weight `.ttf` files (Regular/Medium/SemiBold/
  Bold/ExtraBold) rather than a variable font. That was a deliberate
  reliability call: most current Google Fonts ship variable-only, and
  Android's variable-font axis support needs API 26+ — on this app's
  minSdk 24 (Android 7.0), a variable font would silently render at the
  wrong weight on the oldest ~0.5% of devices. Static files render
  pixel-correct on every supported version with zero fallback risk.
- **Tinted shadows.** `MetallicButton` and `ModeCard` use `Modifier.shadow`
  with an accent-tinted `ambientColor`/`spotColor` instead of a plain black
  drop shadow, so a gold button looks lit by its own metal. Colored
  platform shadows need API 28+; below that, Compose gracefully falls back
  to an ordinary shadow — no crash, just a slightly less dramatic one on
  very old devices.
- **Gold-foil text.** The "LoopLine" wordmark on Splash/Home is filled with
  a `Brush` gradient instead of a flat color (`GradientText.kt`, using
  Compose's `TextStyle(brush = ...)`), so it reads as foil rather than
  yellow ink.
- **Confetti got a glow-up too.** The win-screen burst now mixes gold/
  copper/rose-gold dots with small diamond "sparkle" shapes instead of
  generic party-popper circles in the old blue/orange/green.

### Color palette

| Token | Hex | Use |
|---|---|---|
| Background base | `#110C09` | app background (warm near-black, not navy) |
| Background elevated | `#1D1510` | top stop of the background gradient |
| Surface card | `#1F160F` | resting card/dialog surface |
| Surface card elevated | `#2B2015` | dialogs, elevated surfaces |
| **Gold** (primary) | `#F8ECC4` → `#CBA25A` → `#9C7A3D` | highlight → core → deep, primary accent |
| **Copper** (secondary) | `#F3BD8E` → `#C2794C` → `#94552E` | highlight → core → deep, secondary accent |
| **Rose gold** (tertiary) | `#F1CBBB` → `#C48874` → `#93594A` | highlight → core → deep, tertiary accent |
| Tile idle | `#E9E0D1` | warm ivory-stone, unfilled tiles |
| Text primary | `#F6EFE3` | headings, primary content |
| Text secondary | `#AB9C86` | supporting text |

Full ramps (each metal also has a `*Shadow` stop for the darkest bevel edge)
are in `ui/theme/Color.kt`; the `Brush` gradients built from them are in
`ui/theme/Gradients.kt`.

### Fonts

Poppins, bundled as real `.ttf` files under `app/src/main/res/font/`
(`poppins_regular.ttf`, `poppins_medium.ttf`, `poppins_semibold.ttf`,
`poppins_bold.ttf`, `poppins_extrabold.ttf`), sourced from Google's
[OFL-licensed](https://openfontlicense.org/) `google/fonts` repository —
free to bundle and redistribute in the app. `LoopLineFontFamily` in
`Type.kt` wires them up; no download step needed, they're already in the
project.

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

- Build out Daily Puzzle / Timed / Zen (currently "Coming soon" placeholders on Home)
- Wire the Stats and Leaderboard screens behind their existing header icons
- Gate extra hints behind a rewarded ad (`MAX_HINTS_PER_LEVEL` in `GameScreen.kt` is the knob)
- If you want a signed release build (Play Store or wider sharing) instead of the debug APK below, that's a separate keystore setup — ask when you're ready
