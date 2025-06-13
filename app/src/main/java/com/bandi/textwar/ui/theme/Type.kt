package com.bandi.textwar.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.bandi.textwar.R

@OptIn(ExperimentalTextApi::class)
val PretendardVariable = FontFamily(
    Font(
        R.font.pretendard_variable,
        variationSettings = FontVariation.Settings(
            FontVariation.weight(600),
            FontVariation.width(30f),
            FontVariation.slant(-6f),
        )
    )
)


// 기본 Typography 설정 (Material 3 기준)
// 여기에 PretendardVariable 폰트 패밀리를 적용
val AppTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = PretendardVariable,
        fontWeight = FontWeight.Normal,
        fontSize = 57.sp,
        lineHeight = 64.sp,
        letterSpacing = (-0.25).sp
    ),
    displayMedium = TextStyle(
        fontFamily = PretendardVariable,
        fontWeight = FontWeight.Light,
        fontSize = 45.sp,
        lineHeight = 52.sp,
        letterSpacing = 0.sp
    ),
    displaySmall = TextStyle(
        fontFamily = PretendardVariable,
        fontWeight = FontWeight.ExtraLight,
        fontSize = 36.sp,
        lineHeight = 44.sp,
        letterSpacing = 0.sp
    ),
    headlineLarge = TextStyle(
        fontFamily = PretendardVariable,
        fontWeight = FontWeight.ExtraBold, // 제목은 굵게
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = 0.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = PretendardVariable,
        fontWeight = FontWeight.Bold, // 제목은 굵게
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = PretendardVariable,
        fontWeight = FontWeight.SemiBold, // 제목은 굵게
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp
    ),
    titleLarge = TextStyle(
        fontFamily = PretendardVariable,
        fontWeight = FontWeight.ExtraBold, // 제목은 굵게
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    titleMedium = TextStyle(
        fontFamily = PretendardVariable,
        fontWeight = FontWeight.Bold, // 중간 제목도 굵게
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp
    ),
    titleSmall = TextStyle(
        fontFamily = PretendardVariable,
        fontWeight = FontWeight.SemiBold, // 작은 제목도 굵게
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = PretendardVariable,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = PretendardVariable,
        fontWeight = FontWeight.ExtraLight,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    ),
    bodySmall = TextStyle(
        fontFamily = PretendardVariable,
        fontWeight = FontWeight.Thin,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp
    ),
    labelLarge = TextStyle( // 버튼에 들어가는 텍스트
        fontFamily = PretendardVariable,
        fontWeight = FontWeight.Normal, // 라벨은 강조를 위해 굵게
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
        fontFamily = PretendardVariable,
        fontWeight = FontWeight.Light, // 라벨은 강조를 위해 굵게
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    ),
    labelSmall = TextStyle(
        fontFamily = PretendardVariable,
        fontWeight = FontWeight.ExtraLight, // 라벨은 강조를 위해 굵게
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
)