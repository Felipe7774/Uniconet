package com.utadeo.uniconnect

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.google.firebase.analytics.FirebaseAnalytics
import com.utadeo.uniconnect.data.model.navigation.AppNavigation
import com.utadeo.uniconnect.ui.theme.UniConnectTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // 🔹 Configurar Firebase Analytics (fuera de setContent)
        val analytics = FirebaseAnalytics.getInstance(this)
        val bundle = Bundle().apply {
            putString("message", "Integración de Firebase completa")
        }
        analytics.logEvent("InitScreen", bundle)

        // 🔹 Interfaz principal
        setContent {
            UniConnectTheme {
                AppNavigation()
            }
        }
    }
}
