package com.lmfd.warboss.ui

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.lmfd.warboss.ui.faction.FactionListScreen
import com.lmfd.warboss.ui.dataimport.ImportScreen
import com.lmfd.warboss.ui.unit.UnitDetailScreen
import com.lmfd.warboss.ui.unit.UnitListScreen

private const val ROUTE_IMPORT = "import"
private const val ROUTE_FACTIONS = "factions"
private const val ROUTE_UNIT_LIST = "units/{factionId}"
private const val ROUTE_UNIT_DETAIL = "unit/{unitId}"

@Composable
fun WarbossNavGraph(
    navController: NavHostController = rememberNavController(),
) {
    NavHost(navController = navController, startDestination = ROUTE_IMPORT) {
        composable(ROUTE_IMPORT) {
            ImportScreen(
                onImportComplete = {
                    navController.navigate(ROUTE_FACTIONS) {
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(ROUTE_FACTIONS) {
            FactionListScreen(
                onFactionClick = { factionId ->
                    val encoded = Uri.encode(factionId)
                    navController.navigate("units/$encoded")
                },
                onNavigateToImport = {
                    navController.popBackStack(ROUTE_IMPORT, inclusive = false)
                }
            )
        }

        composable(
            route = ROUTE_UNIT_LIST,
            arguments = listOf(navArgument("factionId") { type = NavType.StringType }),
        ) {
            UnitListScreen(
                onUnitClick = { unitId ->
                    val encoded = Uri.encode(unitId)
                    navController.navigate("unit/$encoded")
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
    }
}
