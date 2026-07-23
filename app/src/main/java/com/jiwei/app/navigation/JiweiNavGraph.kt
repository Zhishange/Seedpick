package com.jiwei.app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.jiwei.app.ui.entry.detail.EntryDetailScreen
import com.jiwei.app.ui.entry.edit.EntryEditScreen
import com.jiwei.app.ui.home.HomeScreen
import com.jiwei.app.ui.search.SearchScreen
import com.jiwei.app.ui.tag.TagManageScreen

@Composable
fun JiweiNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToSearch = { navController.navigate(Screen.Search.route) },
                onNavigateToTags = { navController.navigate(Screen.Tags.route) },
                onNavigateToGraph = { navController.navigate(Screen.Graph.route) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                onNavigateToEntry = { id ->
                    navController.navigate(Screen.EntryDetail.createRoute(id))
                },
                onNewEntry = {
                    navController.navigate("entry/edit/null")
                }
            )
        }

        composable(Screen.Search.route) {
            SearchScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEntry = { id ->
                    navController.navigate(Screen.EntryDetail.createRoute(id))
                }
            )
        }

        composable(Screen.Tags.route) {
            TagManageScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEntry = { id ->
                    navController.navigate(Screen.EntryDetail.createRoute(id))
                }
            )
        }

        composable(Screen.Graph.route) {
            PlaceholderScreen("知识图谱")
        }

        composable(Screen.Settings.route) {
            PlaceholderScreen("设置")
        }

        composable(
            route = Screen.EntryDetail.route,
            arguments = listOf(navArgument("entryId") { type = NavType.StringType })
        ) { backStackEntry ->
            val entryId = backStackEntry.arguments?.getString("entryId") ?: return@composable
            EntryDetailScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEdit = { id ->
                    navController.navigate(Screen.EntryEdit.createRoute(id))
                },
                onNavigateToEntry = { id ->
                    navController.navigate(Screen.EntryDetail.createRoute(id))
                },
                onCreateEntry = { title ->
                    navController.navigate("entry/edit/null")
                },
                onEntryDeleted = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Screen.EntryEdit.route,
            arguments = listOf(navArgument("entryId") { type = NavType.StringType })
        ) { backStackEntry ->
            val entryId = backStackEntry.arguments?.getString("entryId")
            EntryEditScreen(
                entryId = entryId,
                onNavigateBack = { navController.popBackStack() },
                onEntrySaved = { savedId ->
                    navController.popBackStack()
                    navController.navigate(Screen.EntryDetail.createRoute(savedId))
                }
            )
        }
    }
}

@Composable
private fun PlaceholderScreen(title: String) {
    androidx.compose.material3.Text(
        text = title,
        style = androidx.compose.material3.MaterialTheme.typography.headlineMedium
    )
}
