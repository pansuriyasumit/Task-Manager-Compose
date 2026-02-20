package com.svp.taskhelpercomposemvi.view.ui.theme

import android.app.Activity
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat

private val lightSchemeTheme = AppColorScheme(
    primary = primaryLight,
    onPrimary = onPrimaryLight,
    secondary = secondaryLight,
    onSecondary = onSecondaryLight,
    background = backgroundLight,
    onBackground = onBackgroundLight,
    onPrimaryContainer = onPrimaryContainerLight,
    primaryContainer = primaryContainerLight,
    secondaryContainer = secondaryContainerLight,
    surface = surfaceLight,
    onSurfaceVariant = onSurfaceVariantLight,
    onSurface = onSurfaceLight,
    onTertiaryContainer = onTertiaryContainerLight,
    tertiaryContainer = tertiaryContainerLight,
    outLine = outlineLight,
    outLineVariant = outlineVariantLight,
    error = errorLight,
    errorContainer = errorContainerLight,
    onErrorContainer = onErrorContainerLight
)

private val darkSchemeTheme = AppColorScheme(
    primary = primaryDark,
    onPrimary = onPrimaryDark,
    secondary = secondaryDark,
    onSecondary = onSecondaryDark,
    background = backgroundDark,
    onBackground = onBackgroundDark,
    onPrimaryContainer = onPrimaryContainerDark,
    primaryContainer = primaryContainerDark,
    secondaryContainer = secondaryContainerDark,
    surface = surfaceDark,
    onSurfaceVariant = onSurfaceVariantDark,
    onSurface = onSurfaceDark,
    tertiaryContainer = tertiaryContainerDark,
    onTertiaryContainer = onTertiaryContainerDark,
    outLine = outlineDark,
    outLineVariant = outlineVariantDark,
    error = errorDark,
    errorContainer = errorContainerDark,
    onErrorContainer = onErrorContainerDark
)

private val typography = AppTypography(
    titleLarge = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp
    ),
    titleNormal = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp
    ),
    body = TextStyle(
        fontFamily = AppFontFamily,
        fontSize = 16.sp
    ),
    labelLarge = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp
    ),
    labelNormal = TextStyle(
        fontFamily = AppFontFamily,
        fontSize = 14.sp
    ),
    labelSmall = TextStyle(
        fontFamily = AppFontFamily,
        fontSize = 12.sp
    )
)

private val shape = AppShape(
    container = RoundedCornerShape(12.dp),
    button = RoundedCornerShape(50)
)

private val size = AppSize(
    large = 24.dp,
    medium = 16.dp,
    normal = 12.dp,
    small = 8.dp
)

@Composable
fun TaskHelperComposeMVITheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable() () -> Unit,
) {

    val colorScheme = if (darkTheme) darkSchemeTheme else lightSchemeTheme
    val rippleIndication = ripple()

    CompositionLocalProvider(
        LocalAppColorScheme provides colorScheme,
        LocalAppTypography provides typography,
        LocalAppShape provides shape,
        LocalAppSize provides size,
        LocalIndication provides rippleIndication,
        content = content
    )

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val activity  = view.context as Activity
            activity.window.navigationBarColor = colorScheme.primary.copy(alpha = 0.08f).compositeOver(colorScheme.surface.copy()).toArgb()
            activity.window.statusBarColor = colorScheme.primary.toArgb()

            //Those two lines takes care of the icons in the navigation and status bar.
            WindowCompat.getInsetsController(activity.window, view).isAppearanceLightStatusBars = !darkTheme
            WindowCompat.getInsetsController(activity.window, view).isAppearanceLightNavigationBars = !darkTheme
        }
    }
}

object MyAppTheme {
    val colorScheme: AppColorScheme
        @Composable get() = LocalAppColorScheme.current

    val typography: AppTypography
        @Composable get() = LocalAppTypography.current

    val shape: AppShape
        @Composable get() = LocalAppShape.current

    val size: AppSize
        @Composable get() = LocalAppSize.current
}