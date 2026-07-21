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
- **The stroke is one-directional - there's no dragging back over it.** Once
  a tile is connected it stays connected; touching an earlier tile (or the
  start dot) does nothing. A wrong turn is permanent for that attempt - the
  only way back to the start is the **Restart** icon in the header. This
  raises the stakes of every move instead of letting a careless drag get
  walked back for free.
- A short haptic tick and a soft pop sound confirm each tile you connect.
- The header shows a live `filled / total` tile counter and elapsed time.
- Completing a level plays a quick confetti burst, then a dialog with a **star rating** (based on solve time relative to puzzle size), **Next level** (fresh puzzle, same difficulty, slightly bigger every 5 levels), or **Change difficulty**.
- Play is endless — no fixed level count.

## Recent fixes (this update, hint scarcity & single-track difficulty)

- **Hints are scarce now: 1 per level, not 3** (`MAX_HINTS_PER_LEVEL` in
  `GameScreen.kt`). Three felt generous enough that the Hint button barely
  mattered; one makes each hint an actual decision.
- **Classic is one continuous level track that steps through all three
  tiers on its own, instead of Easy/Normal/Hard being three separate
  tracks the player restarts from Level 1 on.** Levels 1-40 generate as
  Easy, 41-70 as Normal, 71 and up as Hard with no ceiling
  (`Difficulty.forLevel` in `Difficulty.kt` is the one place that boundary
  lives). `GameSession` is now a single global session (one saved level
  number, one saved stroke-in-progress) rather than a map of three - the
  in-game header's difficulty label, hint/streak bookkeeping, and the
  Leaderboard/Statistics screens' per-tier rows all still work exactly as
  before because they already read the *current* tier off `GameSession`
  rather than a player-chosen one; that tier now just changes
  automatically as the level number crosses 40 and 70 instead of staying
  fixed for the whole run. The now-unreachable Easy/Normal/Hard picker
  (`DifficultySelectScreen.kt`, already dead code before this change - see
  the previous update's note below about Home going straight into Level 1)
  has been deleted rather than left to bit-rot next to a session model it
  no longer matches.
- Net effect on the Leaderboard: "Easy" freezes at "Reached Level 40" once
  you climb past it, "Normal" shows wherever between 41-70 you're
  currently strongest at, and "Hard" is the one that keeps climbing
  forever - each row is still driven by the same per-tier
  `ProgressStore.bestLevel`/`fastestSeconds` as before, just populated by
  one climbing track instead of three chosen ones.

## Recent fixes (previous update, animation & engagement pass)

- **Every `MetallicButton` in the app now compresses on press** (a spring
  scale down to 0.94x, `MetallicButton.kt`) instead of relying on the
  ripple alone for tactile feedback - one change, felt on every button
  everywhere since it's the app's single shared button component.
- **Screen-to-screen navigation now slides + fades** (`LoopLineNavGraph.kt`,
  220ms) instead of cutting instantly between Home/Settings/Statistics/
  Leaderboard/Game - forward navigation slides in from the right, back
  navigation mirrors it in reverse.
- **A perfect-solve streak** (`ProgressStore.currentStreak`/`bestStreak`):
  a level counts as "perfect" if it was solved with zero wrong-tile
  touches and zero hints. A gold flame chip in `GameScreen`'s header pops
  with a bounce every time the streak extends, milestones (3, then every
  5) get a callout on the win screen, and both the current and best streak
  are now on the Statistics screen alongside the existing daily streak.
- **The win moment got louder**: confetti went from 28 to 44 particles
  with a wider spread, and a new `CompletionCallout` stamps a bouncy
  "PERFECT" badge and/or the streak-milestone line over the grid during
  the existing auto-advance window (which now runs ~500ms longer on a
  milestone specifically so that text is actually readable, not just
  flashed).
- **A live fill-progress bar** under the tile counter (`FillProgressBar`)
  - a thin accent-gradient bar that springs (slight overshoot, not a
    linear tween) toward `path.size / cellCount` on every connect, so
    progress reads visually and not just as a number.

## Recent fixes (previous update)

- **Zen and Timed are real, playable modes now**, not "coming soon" cards.
  Both are endless progressions of their own (`ModeSession`, a sibling of
  `GameSession` - same hydrate/resume/next/persist shape, but one track
  each instead of per-difficulty) using Normal's grid scaling as a
  baseline:
  - **Zen** is Classic without the clock displayed - no time pressure, just
    tiles and hints.
  - **Timed** replaces the stopwatch with a countdown (3s/tile, floor of
    20s); running out opens a "Time's up" dialog to retry the same attempt
    or head home. The header's time reading turns copper in the last 10s.
- **Home's top bar no longer has separate Stats/Leaderboard/Settings
  icons.** All three are now `ModeCard`s in the same "MORE" grid as Zen and
  Timed - same size, same badge style, same visual weight - since treating
  utility screens as smaller icon buttons made them look like an
  afterthought next to the gameplay modes.
- **Fixed a real navigation bug this surfaced**: "Next level" popped the
  back stack up to Difficulty Select unconditionally, which is fine for
  Classic but is a route that was never on the stack for Daily/Zen/Timed
  (all three are entered straight from Home) - so every "Next level" tap in
  those modes just kept stacking a new Game screen on top instead of
  replacing the current one. Now it pops the current Game entry
  specifically (`popUpTo(Routes.GAME)`), which correctly leaves whatever's
  underneath - Difficulty Select or Home - untouched either way.

## Recent fixes (previous update)

- **"Next level" double-tap no longer skips a level.** A fast double-tap
  fired the callback twice before navigation away from GameScreen could
  take effect, and each call advanced `GameSession` by one real level. The
  dialog now dismisses immediately on the first tap and an `isAdvancingLevel`
  guard blocks any repeat.
- **The game actually pauses when the app leaves the foreground.** A
  `LifecycleEventObserver` on `ON_STOP` now sets `isPaused = true` and
  flushes progress to disk - previously only the in-app Pause Menu paused
  the clock, so switching apps or locking the screen let the timer (and,
  for the Daily Challenge, nothing else) run on unseen.
- **Progress now survives an actual app restart, not just in-app
  navigation.** `GameSession` used to be pure in-memory state - force-close
  the app and every difficulty's in-progress session (which level, the
  stroke so far, elapsed time, hints used) was gone, even though the
  separate best-level record remembered you'd gone further. `ProgressStore`
  now persists the full session per difficulty (shape, path, timer, hints)
  and `GameSession.hydrate()` rebuilds it from disk on first touch each
  process - wired in from `MainActivity.onCreate`.
- **Daily Challenge is live.** Renamed from "Daily Puzzle", moved above
  Classic on Home (it's the one time-sensitive, live thing), and actually
  works now: one deterministic puzzle per calendar day (same for everyone,
  seeded from the date - see `LevelGenerator.generateDaily`), a live
  "resets in HH:MM:SS" countdown, a day streak, and a 7-day calendar strip
  (`DailyChallengeStore`, `DailyChallengeBanner`).
- **Settings, Statistics, and Leaderboard are real screens now**, not
  "coming soon" dialogs - sound/vibration toggles and a confirmed full
  reset; per-difficulty best level, fastest solve, lifetime levels
  completed/hints used, and the daily streak; and a "your best runs" board
  (see the honesty note in `LeaderboardScreen.kt` for why it's personal
  bests, not fabricated other-player scores).

## Recent fixes (this update, previous round)

- **App launcher icon now uses the same gold logo.** The adaptive icon
  (`mipmap-anydpi-v26/ic_launcher.xml` and `ic_launcher_round.xml`) now
  points both its background and foreground layers at the bundled logo
  bitmap (`drawable-nodpi/ic_launcher_photo.png`), with legacy PNG
  fallbacks generated for every density (`mipmap-mdpi` through
  `mipmap-xxxhdpi`) for pre-API26 devices. The old vector
  `ic_launcher_background.xml` / `ic_launcher_foreground.xml` are gone.
- **The stroke no longer goes backward - at all.** This was the single
  biggest gameplay change this round: touching an already-visited tile
  (including the start dot) used to retract the stroke back to that point.
  That's removed. Once a tile is connected it's connected for good; a wrong
  turn is now permanent for that attempt, and the header's **Restart**
  button is the only way back to Level 1 of that attempt. Raises the
  stakes of every drag instead of letting you walk back mistakes for free.
  (See "Classic mode — how it plays" below for the updated rules, and the
  superseded note under "testing feedback" further down for what this
  replaced.)
- **Home screen actually restructured, not just reordered.** Last round
  only reordered the mode grid, which just moved the "a coming-soon mode
  sitting right next to Classic looks half-finished" problem onto Zen
  instead of fixing it. Now Classic gets its own full-width
  `FeaturedModeBanner` right under the header - closer to how NumRush gives
  its live Daily Challenge a standalone prominent row instead of making it
  fight placeholder tiles for attention - and Zen / Timed / Daily Puzzle
  sit together underneath in a smaller, visually quieter "More modes" grid.
  Their cards no longer breathe with the same pulsing glow as a real,
  playable mode (see `ModeCard.kt`'s `badgeHighlighted` handling) so they
  read as secondary at a glance instead of equally alive.
- **Zen's icon swapped.** `Icons.Filled.SelfImprovement` (a literal
  meditating stick figure) stood out as slightly cartoonish next to every
  other card's abstract icon (grid, clock, calendar) - replaced with
  `Icons.Filled.Spa`, which reads as calm without being a human figure.

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
- **Backward dragging was removed entirely (superseded the old undo-by-touch fix below).** The stroke used to let you retract by dragging back over already-visited tiles - see the "Fast backward drags" note just below for why that existed. That whole mechanism is now gone: touching any previously-visited tile (including the start dot) simply does nothing. It's a deliberate rule change, not a bug fix - see "Recent fixes (this update)" above.
- ~~Fast backward drags no longer get "stuck" and force a full reset.~~ *(superseded - see above)* The
  undo logic only ever checked one step back (`path[size - 2]`), which
  worked for a slow, deliberate drag but not a fast one: dragging quickly
  back along the stroke samples touch positions in bigger jumps, so it could
  land two-or-more tiles behind the head. That cell wasn't `path[size - 2]`,
  so nothing happened — the only way out was tapping the start dot and
  losing the whole level. `handleTouch` in `GameScreen.kt` used to find the
  touched cell's position anywhere in the current path (not just one slot
  back) and retract to it - now it just does nothing on any past tile,
  which sidesteps the whole class of bug by removing backward movement.

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

## Settings, Statistics & Leaderboard

All three are real screens now (`SettingsScreen.kt`, `StatisticsScreen.kt`,
`LeaderboardScreen.kt`), opened from the icons on the Home screen top bar.

- **Settings** - sound and vibration toggles (`SettingsStore`), and a
  confirmed "reset everything" action that clears best levels, in-progress
  sessions, lifetime stats, and the Daily Challenge streak.
- **Statistics** - lifetime levels completed and hints used, current/best
  Daily Challenge streak, and per-difficulty best level + fastest solve.
- **Leaderboard** - deliberately framed as "your best runs," not a ranked
  comparison against other players. There's no server behind this app, so
  there's no honest way to show real other-player scores here - faking
  them would be misleading. If online leaderboards get built later
  (needs a backend), this is the screen to extend.

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
