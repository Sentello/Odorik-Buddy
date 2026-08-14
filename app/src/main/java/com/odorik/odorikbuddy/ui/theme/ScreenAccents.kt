package com.odorik.odorikbuddy.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import com.odorik.odorikbuddy.data.local.AppTheme

@Immutable
data class ScreenAccent(
    val light: Color,
    val lightSecondary: Color,
    val dark: Color,
    val darkSecondary: Color,

    val collapsesInOdorik: Boolean = true
) {
    @Composable
    fun main(): Color {
        if (collapsesInOdorik && LocalAppTheme.current == AppTheme.ODORIK) {
            return if (LocalIsAppDark.current) OdorikAccentMainDark else OdorikAccentMainLight
        }
        return if (LocalIsAppDark.current) dark else light
    }

    @Composable
    fun secondary(): Color {
        if (collapsesInOdorik && LocalAppTheme.current == AppTheme.ODORIK) {
            return if (LocalIsAppDark.current) OdorikAccentSecondaryDark else OdorikAccentSecondaryLight
        }
        return if (LocalIsAppDark.current) darkSecondary else lightSecondary
    }
}

object ScreenAccents {
    val Sms = ScreenAccent(
        light = Color(0xFF3B82F6),
        lightSecondary = Color(0xFF7EB6F6),
        dark = Color(0xFF8AB8F8),
        darkSecondary = Color(0xFF5A8FD6)
    )
    val Calls = ScreenAccent(
        light = Color(0xFFFF5722),
        lightSecondary = Color(0xFFFF8A65),
        dark = Color(0xFFFF8A65),
        darkSecondary = Color(0xFFD96B47)
    )
    val History = ScreenAccent(
        light = Color(0xFF7C3AED),
        lightSecondary = Color(0xFFA78BFA),
        dark = Color(0xFFA78BFA),
        darkSecondary = Color(0xFF8B6BD6)
    )
    val Settings = ScreenAccent(
        light = Color(0xFF0D9488),
        lightSecondary = Color(0xFF5EEAD4),
        dark = Color(0xFF4FD1C0),
        darkSecondary = Color(0xFF2FA898)
    )
    val Dashboard = ScreenAccent(
        light = Color(0xFFF59E0B),
        lightSecondary = Color(0xFFFCD34D),
        dark = Color(0xFFFBBF24),
        darkSecondary = Color(0xFFD9A320)
    )

    val CallIncoming = ScreenAccent(
        light = CallIncomingLight,
        lightSecondary = CallIncomingLightSecondary,
        dark = CallIncomingDark,
        darkSecondary = CallIncomingDarkSecondary,
        collapsesInOdorik = false
    )
    val CallOutgoing = ScreenAccent(
        light = CallOutgoingLight,
        lightSecondary = CallOutgoingLightSecondary,
        dark = CallOutgoingDark,
        darkSecondary = CallOutgoingDarkSecondary,
        collapsesInOdorik = false
    )
    val CallRedirected = ScreenAccent(
        light = CallRedirectedLight,
        lightSecondary = CallRedirectedLightSecondary,
        dark = CallRedirectedDark,
        darkSecondary = CallRedirectedDarkSecondary,
        collapsesInOdorik = false
    )

    val CounterGreen = ScreenAccent(
        light = CounterGreenLight,
        lightSecondary = CounterGreenLight,
        dark = CounterGreenDark,
        darkSecondary = CounterGreenDark,
        collapsesInOdorik = false
    )
    val CounterOrange = ScreenAccent(
        light = CounterOrangeLight,
        lightSecondary = CounterOrangeLight,
        dark = CounterOrangeDark,
        darkSecondary = CounterOrangeDark,
        collapsesInOdorik = false
    )
    val CounterRed = ScreenAccent(
        light = CounterRedLight,
        lightSecondary = CounterRedLight,
        dark = CounterRedDark,
        darkSecondary = CounterRedDark,
        collapsesInOdorik = false
    )
}
