package com.lmfd.warboss

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.lmfd.warboss.ui.WarbossNavGraph
import com.lmfd.warboss.ui.theme.WarbossTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WarbossTheme {
                val navController = rememberNavController()
                WarbossNavGraph(navController = navController)
            }
        }
    }
}
