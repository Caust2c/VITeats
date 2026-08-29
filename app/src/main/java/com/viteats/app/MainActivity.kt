package com.viteats.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.rememberNavController
import com.viteats.app.ui.navigation.NavGraph
import com.viteats.app.ui.navigation.Screen
import com.viteats.app.ui.theme.VITeatsTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VITeatsTheme {
                val navController = rememberNavController()
                val application = LocalContext.current.applicationContext as VITeatsApplication
                
                val startDestination = if (application.authRepository.isLoggedIn()) {
                    Screen.Home.route
                } else {
                    Screen.Login.route
                }

                NavGraph(navController = navController, startDestination = startDestination)
            }
        }
    }
}
