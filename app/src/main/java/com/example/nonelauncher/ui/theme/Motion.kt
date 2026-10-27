package com.example.nonelauncher.ui.theme

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically

/**
 * One motion language for surfaces that rise from the bottom edge — the dock, the sheets and the
 * 1 Thing input card. Sharing the spec keeps them animating as a single system (same spring in,
 * stiffer and faster out) instead of each carrying its own copy of the numbers.
 */
val bottomEnter: EnterTransition = slideInVertically(
    animationSpec = spring(dampingRatio = 0.8f, stiffness = 400f),
    initialOffsetY = { it }
) + fadeIn(tween(200))

val bottomExit: ExitTransition = slideOutVertically(
    animationSpec = spring(dampingRatio = 1f, stiffness = 700f),
    targetOffsetY = { it }
) + fadeOut(tween(150))

/** Gentle fade for the full-size 1 Thing display, where a bottom slide would travel the whole screen. */
val surfaceEnter: EnterTransition = fadeIn(tween(180))
val surfaceExit: ExitTransition = fadeOut(tween(120))
