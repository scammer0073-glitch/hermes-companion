package com.m57.hermescontrol.theme.presets

import androidx.compose.ui.graphics.Color
import com.m57.hermescontrol.theme.HermesStatusColors
import com.m57.hermescontrol.theme.PaletteColors
import com.m57.hermescontrol.theme.buildThemeDarkOnly

// ---------------------------------------------------------------------
// Nemasys Black — signature AMOLED. True black canvas (pixels off),
// warm grays, teal primary, amber warning. This IS the product.
// Monochrome is the fallback; this is the hero.
// ---------------------------------------------------------------------

private val NBlack = Color(0xFF000000)
private val NCard = Color(0xFF0D0F12)        // cards, sheets
private val NCardHover = Color(0xFF13171C)   // hover / high
private val NBorder = Color(0xFF1A232F)      // subtle stroke
private val NBorderStrong = Color(0xFF243146)
private val NMuted = Color(0xFF7A8AA1)       // secondary text
private val NDim = Color(0xFF9FB0C6)         // tertiary
private val NText = Color(0xFFE6EDF3)        // primary text
private val NTeal = Color(0xFF2DD4BF)        // interactive, primary
private val NTealDim = Color(0xFF1E9E8E)     // pressed
private val NTealSoft = Color(0xFF14302C)    // container
private val NAmber = Color(0xFFFACC15)       // warning / highlight
private val NAmberSoft = Color(0xFF2E2608)
private val NRed = Color(0xFFFF4D4D)         // error — slightly desaturated
private val NRedSoft = Color(0xFF2A1212)
private val NGreen = Color(0xFF34D399)       // success
private val NGreenSoft = Color(0xFF0F2A1F)
private val NBlue = Color(0xFF38BDF8)        // info

val AmoledTheme = buildThemeDarkOnly(
    dark = PaletteColors(
        primary = NTeal, onPrimary = Color(0xFF001410),
        primaryContainer = NTealSoft, onPrimaryContainer = Color(0xFFA7F3E6),
        secondary = NDim, onSecondary = NBlack,
        secondaryContainer = NCardHover, onSecondaryContainer = NDim,
        tertiary = NAmber, onTertiary = Color(0xFF1A1400),
        tertiaryContainer = NAmberSoft, onTertiaryContainer = NAmber,
        background = NBlack, onBackground = NText,
        surface = NBlack, onSurface = NText,
        surfaceVariant = NCardHover, onSurfaceVariant = NMuted,
        surfaceContainerLowest = NBlack, surfaceContainerLow = NBlack,
        surfaceContainer = NCard, surfaceContainerHigh = NCardHover,
        surfaceContainerHighest = Color(0xFF1A212C),
        inverseSurface = NText, inverseOnSurface = NBlack,
        inversePrimary = Color(0xFF0D2A24),
        outline = NBorderStrong, outlineVariant = NBorder,
        scrim = Color.Black,
        status = HermesStatusColors(
            success = NGreen, successContainer = NGreenSoft, onSuccess = Color(0xFF001410),
            warning = NAmber, warningContainer = NAmberSoft, onWarning = Color(0xFF1A1400),
            error = NRed, errorContainer = NRedSoft, onError = NBlack, onErrorContainer = Color(0xFFFFC9C9),
            info = NBlue, infoContainer = Color(0xFF0F2433), onInfo = NBlack,
        )
    )
)
