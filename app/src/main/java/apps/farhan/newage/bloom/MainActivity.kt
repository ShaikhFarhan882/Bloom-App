package apps.farhan.newage.bloom

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import apps.farhan.newage.bloom.ui.navigation.BloomNavHost
import apps.farhan.newage.bloom.ui.theme.BloomTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BloomTheme {
                BloomNavHost()
            }
        }
    }
}