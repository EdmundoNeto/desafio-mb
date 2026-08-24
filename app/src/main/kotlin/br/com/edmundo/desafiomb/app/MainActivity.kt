package br.com.edmundo.desafiomb.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import br.com.edmundo.desafiomb.app.navigation.CmcNavHost
import br.com.edmundo.desafiomb.core.ui.theme.CmcTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            CmcTheme {
                CmcNavHost()
            }
        }
    }
}
