# nONE FocusLauncher — design system

The launcher is a layer of instrument glass on the user's own wallpaper. Nothing fills the screen that
the user did not put there; surfaces rise from the bottom edge, name themselves, and retract.

## Ground

- The **device wallpaper** is the page, drawn by the app itself in `WallpaperBackground` and composed first,
  under everything; that whole layer is the Haze source.
- The reason is categorical, not cosmetic: a wallpaper painted by the system lives outside the composition,
  where nothing can sample it, and every "blur" degrades to a flat tint. Holding the ground inside the tree
  is what lets glass actually blur the user's own image.
- The bitmap is permission-gated: `WallpaperManager.getDrawable()` needs photo access
  (`READ_MEDIA_IMAGES` on 13/14, all-files on newer). The welcome dialog asks for it once. The window itself
  stays transparent with `FLAG_SHOW_WALLPAPER`, so until the bitmap is readable the *system's* wallpaper
  shows through behind the tree — the same image, just unsampled. No stand-in ground is painted; an opaque
  fallback would hide the user's wallpaper instead of showing it.
- One `HazeState` per window, created in `LauncherScreen`, attached to the wallpaper and passed down (also
  via `LocalHazeState`). A surface with a private state has no backdrop; do not create one.
- The ground carries a light vertical dim (black 34% at the status edge, 4% mid, 42% at the bottom) *inside*
  the source, so glass and eye see the same ground when the bitmap is available. The dim lives in the tree
  only; the transparent-window fallback shows the wallpaper as the system painted it.
- Changing the wallpaper re-reads the bitmap via an exported `ACTION_WALLPAPER_CHANGED` receiver (a
  not-exported receiver never sees a system broadcast), and a permission grant bumps `wallpaperRefresh`.
- Sheets are inset 8dp from the sides and sit 120dp above the navigation bar, leaving the dial's zone
  clear. With the keyboard up that gap collapses to 16dp (`sheetPad`) so the card does not ride off-screen.

## Liquid glass

`ui/theme/LiquidGlass.kt` is the only glass. A surface is a blurred sample of the ground, a low dark tint
(`#0A0A0B`), a radial sheen from above, and a 1dp specular rim that fades toward the lower-right; larger
surfaces also cast a black shadow so they sit above the page.

| Style | Blur | Tint | Rim | Used for |
|---|---|---|---|---|
| `Thin` | 16dp | 30% | 38% | chip, 1 Thing island |
| `Regular` | 24dp | 40% | 46% | sheets, search, dialog panels |
| `Heavy` | 30dp | 48% | 50% | full-height search, modal panels |
| `Control` | — | 46% | 52% | close buttons on an existing surface |
| `Seat` / `SeatApex` | — | 52 / 64% | 38 / 54% | dial discs |

- Below Android 12 there is no backdrop blur to sample; the tint deepens by 12 points so white type keeps
  its contrast. That is the platform's limit, not a second design.
- Anything resting *on* glass (close buttons, rows inside a sheet, swatches inside a dialog) does not get a
  second backdrop pass: pass `sampling = false`, which keeps the tint, sheen and rim.
- Overlays that must show the wallpaper through glass are composed **in the home window** (scrim + panel),
  never in a `Dialog`: a dialog owns a separate window and cannot see the home's Haze source.
- Press feedback is a 14% white wash and 0.97 scale (`glassPress`), never a ripple; a Material ripple on
  translucent black reads as a grey box.

## Colour roles

| Role | Value | Used for |
|---|---|---|
| Ground | wallpaper (in-composition) | home background |
| Glass | `#0A0A0B` @ 30–64% | every disc, sheet, chip, dial seat |
| Hairline | `white` @ 14–54% | specular rims, arc track, scale ticks |
| Type | `white` (secondary @ 70–92%) | all copy |
| Accent | `#FF4D00` (`OrangePrimary`) | selection, the index tick, the "1" mark, the primary button |

Orange is a **selection and action** colour, never a surface fill at rest: the apex index tick, the 1 Thing
island numeral, the selected dial label's mark, the wallpaper and focus buttons. If a screen shows more than
one orange element, one of them is doing a job it should not.

Dark is the only scheme. This is a launcher viewed at night as often as at noon; the wallpaper supplies the
light, glass supplies the contrast.

## Type

- System face (Roboto), Material 3 scale: `titleSmall`/`bodyLarge` for names, `bodyMedium` for secondary
  copy, `labelSmall`/`labelMedium` for counters and chips.
- One exception: the dial's apex label is 14sp Medium, letter-spacing 0.2sp — sized to sit above a 64dp disc
  without competing with it.
- All copy is sentence case. Counters ride the text they count ("eat 10/13"), never a separate column.

## Shape

- Discs and the collapsed handle are circles.
- App icons use the launcher's squircle: `RoundedCornerShape(size × 0.2237)` (`SquircleShapes`), applied to
  the icon bitmap inside its disc at 66% of the disc's diameter.
- Chips and the island are `RoundedCornerShape(18–20dp)`; the destination chips are pill (`RoundedCornerShape(50)`).

## The dial

The home's app surface. It replaces the dock row; it is not a second way into the same grid.

- **Seats**: `1 Thing`, `Structure`, `Search`, `Settings`, `Add apps`, then the user's pinned apps in their
  pinned order. Each seat is a glass disc: 64dp at the apex, 55dp at one step, 46dp at two; alpha
  1 / 0.78 / 0.56 and zero beyond 2.6 steps.
- **Seat glass**: seats are dense, **non-sampling** glass (`sampling = false`): the `Seat` / `SeatApex` tint,
  sheen and rim over the dial's own backdrop. The wheel redraws every dragged frame; a blurred child per
  seat costs a backdrop pass per disc and leaves its layer burned in on some GPUs when the dial retracts.
  The tint still carries the wallpaper's colour, so the discs read as the same material as every sheet.
- **Geometry**: apex 160dp above the navigation bar; pitch 20°; radius `min(320dp, 0.44 × width / sin 40°)`
  (≈246dp on 360dp) so the second seat either side stays fully on screen at any density. The hairline track
  spans −50°…+50° and scale ticks sit **between** seats at −2.5…+2.5 steps, never under a disc.
- **Accent**: a 4×12dp orange index tick just inside the arc below the apex, plus the apex label. Nothing
  else on the dial is orange unless that seat is the selected one.
- **Label**: the selected entry's name, 14sp, 18dp above the apex disc, crossfaded on selection.
- **Reveal**: seats translate outward from the apex together (`spring(0.82, 650)`), alpha and disc scale
  follow. Retract is `spring(1.0, 1000)` — faster, no bounce. A drag snaps to the nearest seat with
  `spring(0.84, 330)` and picks up the fling velocity.
- **Idle**: 3s without touch retracts the dial (`onRequestHide`); 15s when Touch Exploration is on, so
  TalkBack is never erased mid-read.
- **Touch**: 46–64dp discs all exceed 48dp targets except the 46dp outer seats, which are decorative
  duplicates of reachable neighbours; every action is also a semantics node with an `Open`/`Select` action
  and the dial exposes `Previous item` / `Next item` custom actions.

## Bottom edge

- **Dot** (20dp, white @ 45%) is the only resting control at the bottom centre, 16dp above the navigation
  bar. Tap opens the dial. Horizontal drag arms a destination chip: left → Structure, right → Search, past
  40dp it turns orange, ticks, and commits on release; shorter drags spring home.
- **Step chip** at the top centre: the day's structure step, tap advances, swipe left/right steps ±1.
- **1 Thing island** 72dp above the navigation bar: numeral, task, done counter. Hidden while a sheet,
  search, or the dial is open.

## Motion

One language, from `ui/theme/Motion.kt`: bottom surfaces enter on `spring(0.8, 400)` with a 200ms fade and
exit on `spring(1.0, 700)` with a 150ms fade. The dial follows the same shape with its own numbers because
it is a wheel, not a panel. Haptics mark seat boundaries (`TextHandleMove`) and apex activation
(`LongPress`). No motion exceeds the distance of the element itself; nothing scrolls the wallpaper.

## States

- Empty search shows recent/frequent apps; empty structure shows the chip only; 1 Thing unset reads
  "Set your 1 Thing" and opens the input card.
- Non-essential apps pass through the intention dialog before launch; essentials, Settings, and the
  launcher itself launch directly.
- Loading states are avoided by keeping home data in memory (`HomeViewModel`); no skeletons.
- Dark is fixed; large font scales grow copy, and sheets cap at 80–85% height so controls stay reachable.

## Non-goals

No app grid, no dock row, no widgets on home, no second dark/light theme, no decoration that is not a
surface, a label, or the one accent.
