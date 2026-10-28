package com.example.sheff

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.sheff.data.session.SessionManager
import com.example.sheff.ui.navigation.SheffNavHost
import com.example.sheff.ui.theme.SheffTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Восстанавливаем вход до отрисовки экранов,
        // чтобы профиль сразу знал, кто вошёл.
        SessionManager.init(applicationContext)
        enableEdgeToEdge()
        setContent {
            SheffTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    SheffNavHost()
                }
            }
        }
    }
}
