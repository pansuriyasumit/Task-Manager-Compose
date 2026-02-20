package com.svp.taskhelpercomposemvi.view.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp

//Colors
data class AppColorScheme(
    val background: Color,
    val onBackground: Color,
    val primary: Color,
    val onPrimary: Color,
    val secondary: Color,
    val onSecondary: Color,
    val onPrimaryContainer: Color,
    val primaryContainer: Color,
    val secondaryContainer: Color,
    val surface: Color,
    val onSurfaceVariant: Color,
    val onSurface: Color,
    val tertiaryContainer: Color,
    val onTertiaryContainer: Color,
    val outLine: Color,
    val outLineVariant: Color,
    val error: Color,
    val errorContainer: Color,
    val onErrorContainer: Color,
)

//typography
data class AppTypography(
    val titleLarge: TextStyle,
    val titleNormal: TextStyle,
    val body: TextStyle,
    val labelLarge: TextStyle,
    val labelNormal: TextStyle,
    val labelSmall: TextStyle,
)

//shape
data class AppShape(
    val container: Shape,
    val button: Shape,
)

//size
data class AppSize(
    val large: Dp,
    val medium: Dp,
    val small: Dp,
    val normal: Dp,
)

val LocalAppColorScheme = staticCompositionLocalOf {
    AppColorScheme(
        background = Color.Unspecified,
        onBackground = Color.Unspecified,
        primary = Color.Unspecified,
        onPrimary = Color.Unspecified,
        secondary = Color.Unspecified,
        onSecondary = Color.Unspecified,
        onPrimaryContainer = Color.Unspecified,
        primaryContainer = Color.Unspecified,
        secondaryContainer = Color.Unspecified,
        surface = Color.Unspecified,
        onSurfaceVariant = Color.Unspecified,
        onSurface = Color.Unspecified,
        tertiaryContainer = Color.Unspecified,
        onTertiaryContainer = Color.Unspecified,
        outLine = Color.Unspecified,
        outLineVariant = Color.Unspecified,
        error = Color.Unspecified,
        errorContainer = Color.Unspecified,
        onErrorContainer = Color.Unspecified,
    )
}

val LocalAppTypography = staticCompositionLocalOf {
    AppTypography(
        titleNormal = TextStyle.Default,
        titleLarge = TextStyle.Default,
        body = TextStyle.Default,
        labelLarge = TextStyle.Default,
        labelNormal = TextStyle.Default,
        labelSmall = TextStyle.Default
    )
}

val LocalAppShape = staticCompositionLocalOf {
    AppShape(
        container = RectangleShape,
        button = RectangleShape
    )
}

val LocalAppSize = staticCompositionLocalOf {
    AppSize(
        large = Dp.Unspecified,
        medium = Dp.Unspecified,
        small = Dp.Unspecified,
        normal = Dp.Unspecified
    )
}

