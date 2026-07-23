package com.jiwei.app.navigation

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Search : Screen("search")
    data object Tags : Screen("tags")
    data object Graph : Screen("graph")
    data object Settings : Screen("settings")

    data object EntryDetail : Screen("entry/{entryId}") {
        fun createRoute(entryId: String) = "entry/$entryId"
    }

    data object EntryEdit : Screen("entry/edit/{entryId}") {
        fun createRoute(entryId: String) = "entry/edit/$entryId"
    }
}
