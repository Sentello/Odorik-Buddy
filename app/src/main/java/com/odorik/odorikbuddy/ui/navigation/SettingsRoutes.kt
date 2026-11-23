package com.odorik.odorikbuddy.ui.navigation

object SettingsRoutes {
    const val SETTINGS_HOME = "settings_home"
    const val ROUTES_SCREEN = "routes_screen"
    const val OWN_NUMBERS_SCREEN = "own_numbers_screen"
    const val ROUTING_OPTIONS_SCREEN = "routing_options_screen"
    const val SPEED_DIALS_SCREEN = "speed_dials_screen"
    const val SIM_NUMBER_ARG = "number"
    const val SIM_SCREEN = "sim_screen/{$SIM_NUMBER_ARG}"
    fun simScreen(number: String) = "sim_screen/${android.net.Uri.encode(number)}"
}