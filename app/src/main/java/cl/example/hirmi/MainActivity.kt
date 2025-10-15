package cl.example.hirmi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import cl.example.hirmi.ui.HomeScreen
import cl.example.hirmi.ui.LoginScreen
import cl.example.hirmi.ui.RegisterScreen
import cl.example.hirmi.ui.WelcomeScreen
import cl.example.hirmi.ui.theme.HirMiTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HirMiTheme {
                WelcomeScreen()
            }
        }
    }
}

//Esto es solo para previsualizar la app en el IDE
@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    HirMiTheme {
        Text(text = "HirMi")
    }
}