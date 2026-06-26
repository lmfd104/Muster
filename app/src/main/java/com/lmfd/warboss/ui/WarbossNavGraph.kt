package com.lmfd.warboss.ui

import android.net.Uri
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.lmfd.warboss.ui.armylist.ArmyListDetailScreen
import com.lmfd.warboss.ui.armylist.ArmyListsScreen
import com.lmfd.warboss.ui.dataimport.ImportScreen
import com.lmfd.warboss.ui.faction.FactionListScreen
import com.lmfd.warboss.ui.game.GameTrackerScreen
import com.lmfd.warboss.ui.unit.UnitDetailScreen
import com.lmfd.warboss.ui.unit.UnitListScreen

private const val ROUTE_IMPORT = "import"
private const val ROUTE_FACTIONS = "factions"
private const val ROUTE_UNIT_LIST = "units/{factionId}"
private const val ROUTE_UNIT_DETAIL = "unit/{unitId}"
private const val ROUTE_ARMY_LISTS = "army_lists"
private const val ROUTE_ARMY_LIST_DETAIL = "army_list/{listId}"
private const val ROUTE_GAME = "game"

@Composable
fun WarbossNavGraph(
    navController: NavHostController = rememberNavController(),
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val selectedTab = when (currentRoute) {
        ROUTE_IMPORT -> 0
        ROUTE_FACTIONS, ROUTE_UNIT_LIST, ROUTE_UNIT_DETAIL -> 1
        ROUTE_ARMY_LISTS, ROUTE_ARMY_LIST_DETAIL -> 2
        ROUTE_GAME -> 3
        else -> 1
    }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = {
                        navController.navigate(ROUTE_IMPORT) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    icon = { Icon(Icons.Default.Download, contentDescription = "Import") },
                    label = { Text("Import") },
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = {
                        navController.navigate(ROUTE_FACTIONS) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    icon = { Icon(Icons.Default.Search, contentDescription = "Browse") },
                    label = { Text("Browse") },
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = {
                        navController.navigate(ROUTE_ARMY_LISTS) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    icon = { Icon(Icons.Default.Bookmark, contentDescription = "My Lists") },
                    label = { Text("My Lists") },
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = {
                        navController.navigate(ROUTE_GAME) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    icon = { Icon(Icons.Default.Casino, contentDescription = "Game") },
                    label = { Text("Game") },
                )
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = ROUTE_IMPORT,
            modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding()),
        ) {
            composable(ROUTE_IMPORT) {
                ImportScreen(
                    onImportComplete = {
                        navController.navigate(ROUTE_FACTIONS) { launchSingleTop = true }
                    }
                )
            }

            composable(ROUTE_FACTIONS) {
                FactionListScreen(
                    onFactionClick = { factionId ->
                        navController.navigate("units/${Uri.encode(factionId)}")
                    },
                    onNavigateToImport = {
                        navController.navigate(ROUTE_IMPORT) { launchSingleTop = true }
                    },
                )
            }

            composable(
                route = ROUTE_UNIT_LIST,
                arguments = listOf(navArgument("factionId") { type = NavType.StringType }),
            ) {
                UnitListScreen(
                    onUnitClick = { unitId ->
                        navController.navigate("unit/${Uri.encode(unitId)}")
                    },
                    onBack = { navController.popBackStack() },
                )
            }

            composable(
                route = ROUTE_UNIT_DETAIL,
                arguments = listOf(navArgument("unitId") { type = NavType.StringType }),
            ) {
                UnitDetailScreen(onBack = { navController.popBackStack() })
            }

            composable(ROUTE_ARMY_LISTS) {
                ArmyListsScreen(
                    onListClick = { listId ->
                        navController.navigate("army_list/${Uri.encode(listId)}")
                    }
                )
            }

            composable(
                route = ROUTE_ARMY_LIST_DETAIL,
                arguments = listOf(navArgument("listId") { type = NavType.StringType }),
            ) {
                ArmyListDetailScreen(onBack = { navController.popBackStack() })
            }

            composable(ROUTE_GAME) {
                GameTrackerScreen()
            }
        }
    }
}
