package com.example.ui.nav

sealed class Screen(val route: String) {
    data object Design : Screen("design")
    data object Settings : Screen("settings")
    data object Formulas : Screen("formulas")
}
