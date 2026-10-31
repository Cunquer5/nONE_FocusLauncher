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
- Sheets span both side edges and are welded to the bottom edge: the glass runs under the navigation
  bar and only the content lifts clear of it (`navigationBarsPadding`). They rise from the bottom and
  stop 14% of the screen height short of the top (`sheetTopGap`), so wallpaper frames the glass. The
  top two corners close on 32dp (`GlassContainer`); the bottom corners are square because they leave
  the screen. With the keyboard up the sheet rides the root's `imePadding` and settles on its top edge.
- Every sheet carries `DraggableSheet`: a 36×4dp grabber in the first 32dp of the card. Pulling it
  down tracks the finger, a short pull springs home (`spring(0.9, 500)`), and past 18% of the card's
  height (or 120dp) it slides off in 180ms and dismisses. Scrollable content is untouched — the drag
  lives only in the grabber strip, never over the list.

## Liquid glass

`ui/theme/LiquidGlass.kt` is the only glass. A surface is a blurred sample of the ground, a low dark tint
(`#0A0A0B`), a radial sheen from above, and a 1.5dp specular rim that fades toward the lower-right; larger
surfaces also cast a black shadow so they sit above the page.

| Style | Blur | Tint | Rim | Used for |
|---|---|---|---|---|
| `Thin` | 18dp | 28% | 50% | chip, 1 Thing island |
| `Regular` | 26dp | 38% | 55% | sheets, search, dialog panels |
| `Heavy` | 32dp | 46% | 58% | full-height search, modal panels |
| `Control` | — | 46% | 55% | close buttons on an existing surface |
| `Seat` / `SeatApex` | 22dp | 30 / 40% | 50 / 62% | dial discs |

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
- One exception: the dial's apex label is 14sp Medium, letter-spacing 0.2sp — sized to sit above an 80dp disc
  without competing with it.
- All copy is sentence case. Counters ride the text they count ("eat 10/13"), never a separate column.

## Shape

- Discs and the collapsed handle are circles.
- App icons use the launcher's squircle: `RoundedCornerShape(size × 0.2237)` (`SquircleShapes`), applied to
  the icon bitmap inside its disc at 66% of the disc's diameter.
- Chips size to their content, never to the screen: the step chip is a 20dp pill at the top centre, the
  1 Thing island a 18dp pill 72dp above the navigation bar, and the destination chips are pills
  (`RoundedCornerShape(50)`). A chip stretched edge to edge stops being a chip and becomes a second bar;
  only surfaces that hold content span the width.
- **Sheets are full-width bottom panels** (`GlassContainer`), flush with the bottom edge — square
  bottom corners, 32dp on the top two. They rise from the bottom and stop 14% short of the top, so the
  wallpaper above them is the frame and the card reads as laid over the page. Nothing else touches the
  top edge; there is no bezel plate.

## The dial

The home's app surface. It replaces the dock row; it is not a second way into the same grid.

- **Seats**: `1 Thing`, `Structure`, `Search`, `Settings`, `Add apps`, then the user's pinned apps in their
  pinned order. Each seat is a glass disc: 80dp at the apex, 68dp at one step; alpha 1 / 0.82, fading to
  zero at two steps. A row is three discs — the rest wait past the edge.
- **Seat glass**: seats sample the wallpaper through the same `HazeState` as every sheet — the discs are
  part of the one glass material, not tinted pucks on top of it. A 68–80dp disc cannot carry a panel's
  tint without reading as smoked plastic, so the tint is thinner than a sheet's (30%, 40% at the apex) and
  a long blur (22dp) does the legibility work; the rim and inner shade hold the lensing edge. The seat's
  glyphs are white on the blurred ground, and each app seat carries its own icon, so no type sits directly
  on a bare tint.
- **Geometry**: apex 160dp above the navigation bar; pitch 30°; radius `min(320dp, 0.56 × width / sin 60°)`
  (≈233dp on 360dp) so the seat either side of the apex stays fully on screen while the next one starts
  past the edge. The hairline track bridges **only the gaps between seats** — a line behind a tinted disc
  shows through as a scratch, so it stops at each rim. Scale ticks sit in those same gaps at −15° and +15°.
- **Accent**: a 4×12dp orange index tick 46–58dp inside the arc below the apex, plus the apex label. The
  `1 Thing` glyph is orange only when it is the apex; on a side seat it is white, because an orange mark
  away from the selection competes with it. Nothing else on the dial is orange.
- **Label**: the selected entry's name, 14sp, 56dp above the apex centre so it clears the 80dp disc,
  crossfaded on selection.
- **Reveal**: seats translate outward from the apex together (`spring(0.82, 650)`), alpha and disc scale
  follow. Retract is `spring(1.0, 1000)` — faster, no bounce. A drag snaps to the nearest seat with
  `spring(0.84, 330)` and picks up the fling velocity.
- **Idle**: 3s without touch retracts the dial (`onRequestHide`); 15s when Touch Exploration is on, so
  TalkBack is never erased mid-read.
- **Touch**: 68–80dp discs all exceed 48dp targets; every action is also a semantics node with an
  `Open`/`Select` action and the dial exposes `Previous item` / `Next item` custom actions.

## Bottom edge

- **Dot** (20dp, white @ 55%, soft drop shadow) is the only resting control at the bottom centre, 16dp above
  the navigation bar. The shadow is what keeps it legible on a bright photo without turning it into a
  button. Tap opens the dial. Horizontal drag arms a destination chip: left → Structure, right → Search, past
  40dp it turns orange, ticks, and commits on release; shorter drags spring home.
- **Step chip** 12dp under the status bar: the day's structure step, sized to its label, tap advances,
  swipe left/right steps ±1.
- **1 Thing island** 72dp above the navigation bar: numeral, task, done counter. Hidden while a sheet,
  search, or the dial is open.
- **Sheets** run under the navigation bar rather than hovering above it; the grabber is the only
  control at the card's top edge, and a pulled sheet slides off this bottom edge to dismiss.

## Top edge

The top edge is wallpaper. The status bar's icons sit straight on the ground's own top dim (34% black at
the status edge, inside the source), so they stay legible without a plate laid over the page. Nothing on
home touches the top edge: the step chip clears the status bar by 12dp, and every sheet stops 14% of the
screen height short of it.

## Motion

One language, from `ui/theme/Motion.kt`: bottom surfaces enter on `spring(0.8, 400)` with a 200ms fade and
exit on `spring(1.0, 700)` with a 150ms fade. The dial follows the same shape with its own numbers because
it is a wheel, not a panel. A hand-dragged sheet is direct manipulation, not an entrance: it follows the
finger 1:1, springs home when released short, and leaves on `tween(180, FastOutLinearIn)` when pulled past
the threshold. Haptics mark seat boundaries (`TextHandleMove`) and apex activation (`LongPress`). No motion
exceeds the distance of the element itself; nothing scrolls the wallpaper.

## States

- Empty search shows recent/frequent apps; empty structure shows the chip only; 1 Thing unset reads
  "Set your 1 Thing" and opens the input card.
- Non-essential apps pass through the intention dialog before launch; essentials, Settings, and the
  launcher itself launch directly.
- Loading states are avoided by keeping home data in memory (`HomeViewModel`); no skeletons.
- Dark is fixed; large font scales grow copy, and sheets stop 14% short of the top and run flush to the
  bottom edge with their content clear of the navigation bar, so controls stay reachable.

## Non-goals

No app grid, no dock row, no widgets on home, no second dark/light theme, no decoration that is not a
surface, a label, or the one accent.
