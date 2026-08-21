package com.hearablemusic.player.ui.common.design.typography

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.hearablemusic.player.ui.generated.resources.Res
import com.hearablemusic.player.ui.generated.resources.harmonyos_sans_black
import com.hearablemusic.player.ui.generated.resources.harmonyos_sans_bold
import com.hearablemusic.player.ui.generated.resources.harmonyos_sans_light
import com.hearablemusic.player.ui.generated.resources.harmonyos_sans_medium
import com.hearablemusic.player.ui.generated.resources.harmonyos_sans_regular
import com.hearablemusic.player.ui.generated.resources.harmonyos_sans_thin
import org.jetbrains.compose.resources.Font

object TypographyTokens {
    // CMP 资源 Font() 为 @Composable API（A2：R.font → Res.font），故以 @Composable getter 提供
    val MyFontFamily: FontFamily
        @Composable get() = FontFamily(
            Font(Res.font.harmonyos_sans_black, FontWeight.Black),
            Font(Res.font.harmonyos_sans_bold, FontWeight.Bold),
            Font(Res.font.harmonyos_sans_medium, FontWeight.Medium),
            Font(Res.font.harmonyos_sans_regular, FontWeight.Normal),
            Font(Res.font.harmonyos_sans_light, FontWeight.Light),
            Font(Res.font.harmonyos_sans_thin, FontWeight.Thin)
        )

    // 定义自定义 Typography
    val Typography: Typography
        @Composable get() = Typography(
        displayLarge = TextStyle(
            fontFamily = MyFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 40.sp,
            lineHeight = 40.sp,
            letterSpacing = 0.25.sp
        ),
        displayMedium = TextStyle(
            fontFamily = MyFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            lineHeight = 32.sp,
            letterSpacing = 0.25.sp
        ),
        displaySmall = TextStyle(
            fontFamily = MyFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            lineHeight = 28.sp,
            letterSpacing = 0.25.sp
        ),
        headlineLarge = TextStyle(
            fontFamily = MyFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            lineHeight = 32.sp,
            letterSpacing = 0.sp
        ),
        headlineMedium = TextStyle(
            fontFamily = MyFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            lineHeight = 28.sp,
            letterSpacing = 0.sp
        ),
        headlineSmall = TextStyle(
            fontFamily = MyFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            lineHeight = 24.sp,
            letterSpacing = 0.sp
        ),
        titleLarge = TextStyle(
            fontFamily = MyFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            lineHeight = 28.sp,
            letterSpacing = 0.sp
        ),
        titleMedium = TextStyle(
            fontFamily = MyFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 16.sp,
            lineHeight = 24.sp,
            letterSpacing = 0.25.sp
        ),
        titleSmall = TextStyle(
            fontFamily = MyFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            letterSpacing = 0.25.sp
        ),
        bodyLarge = TextStyle(
            fontFamily = MyFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 16.sp,
            lineHeight = 24.sp,
            letterSpacing = 0.5.sp
        ),
        bodyMedium = TextStyle(
            fontFamily = MyFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            letterSpacing = 0.25.sp
        ),
        bodySmall = TextStyle(
            fontFamily = MyFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            letterSpacing = 0.4.sp
        ),
        labelLarge = TextStyle(
            fontFamily = MyFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            letterSpacing = 0.1.sp
        ),
        labelMedium = TextStyle(
            fontFamily = MyFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            letterSpacing = 0.5.sp
        ),
        labelSmall = TextStyle(
            fontFamily = MyFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 11.sp,
            lineHeight = 16.sp,
            letterSpacing = 0.5.sp
        )
    )
}
