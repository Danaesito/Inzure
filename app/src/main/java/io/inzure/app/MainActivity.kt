package io.inzure.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.runtime.Composable
<<<<<<< HEAD
import androidx.compose.ui.graphics.Color
=======
<<<<<<< HEAD
>>>>>>> 3e9fb1b (users crud mvvm)
import io.inzure.app.ui.views.CarInsuranceView
import io.inzure.app.ui.views.ProfileView
=======
import io.inzure.app.ui.views.UsersView
>>>>>>> 4397f5c (users crud mvvm)
import io.inzure.app.ui.views.MainView

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(scrim = 0, darkScrim = 0), // Íconos oscuros en barra de estado
            navigationBarStyle = SystemBarStyle.light(scrim = 0, darkScrim = 0) // Íconos oscuros en barra de navegación
        )
        setContent {
            InzureTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.White // Fondo blanco en toda la pantalla
                ) {
                    MainView(
<<<<<<< HEAD
                        onNavigateToProfile = {
                            val intent = Intent(this@MainActivity, ProfileView::class.java)
                            startActivity(intent)
                        },
                        onNavigateToCarInsurance = {
                            val intent = Intent(this@MainActivity, CarInsuranceView::class.java)
=======
                        onNavigateToLogin = {
                            val intent = Intent(this@MainActivity, UsersView::class.java)
>>>>>>> 4397f5c (users crud mvvm)
                            startActivity(intent)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun InzureTheme(content: @Composable () -> Unit) {
    MaterialTheme {
        content()
    }
}
