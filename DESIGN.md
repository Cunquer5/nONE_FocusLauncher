# DESIGN.md — nONE FocusLauncher

## World

A rail concourse departures board, reduced to what a phone can hold. The wallpaper stays
visible; the day floats above it as one matte console of split-flap cells. Nothing glows,
nothing blurs. Letters are painted onto flaps, times are monospace, and the only saturated
color is the orange that tells you where to act or where you are.

The board is evidence of a plan, not decoration: the row order is the day's order, the white
band is the one departure that matters now.

## Material

| Token | Value | Use |
|---|---|---|
| `Board.Ink` | `#0C0C0C` | Panel body, dock |
| `Board.InkSoft` | `#151719` | Quiet cells on a panel |
| `Board.Flap` | `#1A1D1F` | Split-flap letter cells, pressed rows |
| `Board.Well` | `#050505` | Text fields, icon wells, footer rail |
| `Board.Paint` | `#F4F1EA` | Primary lettering, the 1 Thing band |
| `Board.PaintDim` | paint @ 55% | Secondary lettering, history |
| `Board.Steel` | `#83868A` | Column labels, metadata, inactive dock labels |
| `Board.Hair` | `#2B2F32` | Cell and panel borders |
| `Board.HairFaint` | `#1B1E21` | Row seams |
| `Board.Action` | `#FF4D00` | One action per surface, the current row |

Panels are one flat matte slab — `#0C0C0C`, a 1dp `#2B2F32` hairline, 2dp corners, and a single
26dp soft shadow that lifts it off the wallpaper. No gradient, no top highlight. Cells use a
2dp radius. The board and its white band are painted with vertical pinstripes 24dp apart (white
4.5% on ink, black 5% on paint), the flap-rail texture.

App icons, contact avatars and file glyphs sit in the same opaque wells with hairline frames —
user content changes inside the well, never the well's shape.

## Type

Three self-hosted faces (`app/src/main/res/font`, variable, weights via axis settings):

- `BoardDisplayFamily` — **Oswald**, 500–600. Step names, the 1 Thing title, sheet titles.
  Uppercase, 15–20sp, 0.5sp tracking. This is the board's voice.
- `BoardMonoFamily` — **Roboto Mono**, 400–500. Clock, step numbers, counts, footer data.
- `BoardSansFamily` — **Inter**, 400/600. Labels, body copy, controls.

- `BoardDisplayStyle` — Oswald 600, 16sp/19sp.
- `BoardLabel` — Inter 600, 11sp/14sp, 1.5sp tracking, uppercase.
- `BoardMonoStyle` — Roboto Mono 500, 15sp/18sp.
- `BoardBodyStyle` — Inter 400, 15sp/19sp.

User-entered steps and tasks render uppercase on the board. Letters are paint, not UI gloss.

## Signature move

**The row restyles in place.** Advancing the day never reorders or scrolls the board. The
current row lights its figure orange and takes a 5% orange berth tint; its STATUS cell is boxed
in orange. Rows above it dim to 28% and strike through; the next row's STATUS is boxed in white;
the rest read SCHEDULED in steel. Tapping any row moves the berth there.

The second move is the **white paint band** — the 1 Thing is the one high-contrast field on the
screen, keyed "THE ONE THING" like a departure, with the only orange action berth running the
strip's full height. It is the board's "now departing".

## Layout

Home, top to bottom inside one 12dp-margin frame: a 44dp header (NONE · date · clock ·
remaining) → the 70dp white band with its 82dp full-height orange berth → a 25dp column rail
(NO. / STEP / STATUS, figures 74dp, statuses 92dp) → 62dp pinstriped rows, four visible before
the list scrolls → a 34dp `#050505` footer ("N steps · N done" / "TAP A ROW"). The board sits
10dp below the status bar.

Below it, the gesture caption ("SWIPE LEFT · STRUCTURE   SWIPE RIGHT · SEARCH") and the lamp
handle: a 16dp white circle with a soft halo, which leans toward the surface a drag is aimed at
and turns orange once armed. A completed 1 Thing restyles the band in place ("DONE TODAY",
struck title); the task persists until the user starts a new one.

Opening a surface keeps this panel composed under a 50% scrim: the board is the context, not
a screen that unmounts. Tapping the scrim closes the top surface.

The dock is a 58dp three-cell strip (Phone / 1 Thing / Settings) with steel seams, hidden
until the lamp is tapped, swiped up, or a sheet opens. The active dock cell marks its bottom
edge orange.

Sheets and the search card rise from the bottom on the shared spring, 8dp side margins, parked
above the dock (120dp), collapsing to 16dp when the keyboard is up.

## Controls

Standard, not themed into novelty: text fields with square wells and orange cursors, full-width
action cells, quiet hairline buttons, a 34dp square close cell. One orange action per surface;
"Cancel/Later" are never orange. Action lettering is board ink on orange (6.3:1), not white
(3.3:1).

## Motion

- Sheet enter/exit: `bottomEnter` / `bottomExit` (existing springs).
- Row change: tint, figure colour, strike, and status cell animate over 220ms; rows never move
  in the grid.
- Lamp: spring back at 0.9 damping, haptic tick when armed; the halo swells with the drag.
- No parallax, no glow beyond the lamp halo, no continuous animation. The clock ticks, the board
  holds still.

## Voice

Short, procedural, untitled. Labels name the thing: "The One Thing", "Set today's one thing",
"4 remaining", "Tap a row". No exclamation marks, no encouragement copy.

## Don't

- Don't reintroduce glass, blur, pills, gradients on text, or floating chips.
- Don't use orange for decoration, links inside body text, or inactive states.
- Don't invent clock times for structure steps; they have no timestamps, so the first column
  is the step number.
- Don't move rows to show progress; restyle them where they are.
- Don't hide the wallpaper behind a full-screen scrim.
