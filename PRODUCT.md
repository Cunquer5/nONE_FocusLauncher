# Product

<!-- impeccable:product-schema 1 -->

## Platform

android

## Users

A person who makes their own phone's home screen the first thing they see many times a day, and who wants that screen to state the day before it offers an app drawer. The redesign interview confirmed: they keep their own wallpaper and consider the single task and the ordered day to be the product.

## Product Purpose

nONE FocusLauncher replaces the Android home screen with a deliberately small set of surfaces: an ordered day flow (Structure), one "1 Thing" task, and fast search across apps, contacts and files. Success means the home screen answers "what is next" before it answers "what can I open."

## Positioning

The only home screen whose primary content is one task and one ordered day flow, with app access demoted to search and the dock rather than a grid.

## Operating Context

Installed as the default Home app on Android 9+. It sits over the user's own wallpaper, is used in short glances throughout the day, and is also the launch point for every app. Focus mode grays non-essential apps. Widgets, wallpaper selection, intention logging, settings, and first-run/feedback prompts are part of the shipped product.

## Capabilities and Constraints

- Kotlin + Jetpack Compose, min SDK 28, target SDK 35.
- Surfaces that must exist: home, Structure editor, 1 Thing input and task display, universal search, settings, intention history, intention dialog, wallpaper picker, welcome and feedback prompts.
- Home gestures currently: tap bottom dot opens dock; swipe left opens Structure; swipe right opens Search; swipe up near the bottom reveals dock. These are not confirmed as fixed — the interaction model may be redesigned.
- Focus mode, pinned/essential apps, frequent apps, widget host, and intention persistence are existing behavior to preserve.
- Manual dependency injection, MVVM with StateFlow; no Hilt.

## Brand Commitments

- Orange `#FF4D00` is the brand accent and stays. Interview confirmed it. It is reserved for actions and active state, never for fields or ground.
- The user's own wallpaper stays visible. Interview confirmed it.
- One task and one ordered day are the product core. Interview confirmed it.
- Type is quiet and neutral; hierarchy comes from size, weight and spacing, not from a characterful display face.

## Evidence on Hand

A working, shipping launcher codebase with all surfaces implemented. No marketing claims, benchmarks, or customer evidence exist and none should be invented. The redesign brief is "too generic — needs a distinct identity only nONE could ship."

## Product Principles

1. State the day before the app drawer.
2. One primary action per surface.
3. Information over chrome.
4. The wallpaper is the ground; the interface is a layer on it.
5. Orange is a signal, not decoration.

## Accessibility & Inclusion

48dp minimum touch targets, TalkBack descriptions and actions for gesture-only features, white-on-dark text contrast on wallpaper-backed surfaces.
