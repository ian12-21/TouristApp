package com.touristapp.core.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

/**
 * Blurs [content] and shows a centered spinner while a language switch is reloading, then reveals
 * the new language. Replaces the old `recreate()`-driven switch, which flashed a black screen.
 *
 * The overlay lingers for [LINGER_AFTER_LOAD_MS] after the reload completes so the new language
 * settles in under the blur, and is driven entirely in-Compose so no Activity/window teardown is
 * needed.
 *
 * Note: [androidx.compose.ui.draw.blur] is a no-op below API 31; on those devices the scrim and
 * elevated spinner card still read as an intentional loading state.
 */
@Composable
fun LanguageSwitchOverlay(
    isSwitching: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    var show by remember { mutableStateOf(false) }
    LaunchedEffect(isSwitching) {
        if (isSwitching) {
            show = true
        } else if (show) {
            // Linger after the reload completes so the new language settles in under the blur.
            delay(LINGER_AFTER_LOAD_MS)
            show = false
        }
    }

    val blurRadius by animateDpAsState(
        targetValue = if (show) 36.dp else 0.dp,
        animationSpec = tween(durationMillis = 300),
        label = "switchBlur"
    )

    Box(modifier = modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxSize().blur(blurRadius)) {
            content()
        }

        AnimatedVisibility(
            visible = show,
            enter = fadeIn(tween(220)),
            exit = fadeOut(tween(220))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.28f)),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = MaterialTheme.shapes.large,
                    tonalElevation = 6.dp,
                    color = MaterialTheme.colorScheme.surface
                ) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(24.dp)
                    )
                }
            }
        }
    }
}

/** How long the blur + spinner lingers after the reload finishes before fading out. */
private const val LINGER_AFTER_LOAD_MS = 1000L
