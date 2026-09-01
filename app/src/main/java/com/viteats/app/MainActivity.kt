package com.viteats.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
            val application = LocalContext.current.applicationContext as VITeatsApplication
            val isDarkMode by application.themeManager.isDarkMode.collectAsState()

            VITeatsTheme(darkTheme = isDarkMode) {
                val navController = rememberNavController()
                
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
