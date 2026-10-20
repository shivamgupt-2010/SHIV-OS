package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.navigation.MainNavGraph

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    handleAuthRedirect(intent)
    try {
      enableEdgeToEdge()
      setContent {
        val appContainer = (application as ShivAiApplication).container
        val currentTheme by appContainer.studioThemeEngine.currentTheme.collectAsState()
        MyApplicationTheme(theme = currentTheme) {
          MainNavGraph(appContainer = appContainer)
        }
      }
    } catch (e: Exception) {
      android.util.Log.e("MainActivity", "Fatal error in onCreate", e)
    }
  }

  override fun onNewIntent(intent: android.content.Intent) {
    super.onNewIntent(intent)
    setIntent(intent)
    handleAuthRedirect(intent)
  }

  private fun handleAuthRedirect(intent: android.content.Intent?) {
    val uri = intent?.data
    if (uri != null && uri.scheme == "shivlauncher" && uri.host == "oauth" && uri.path == "/callback") {
      com.example.auth.ShivIdentityAuth.handleRedirect(this, uri) { success, result ->
        runOnUiThread {
          if (success) {
            android.widget.Toast.makeText(this, "SHIV Identity authenticated successfully!", android.widget.Toast.LENGTH_LONG).show()
          } else {
            android.widget.Toast.makeText(this, "Authentication failed: $result", android.widget.Toast.LENGTH_LONG).show()
          }
        }
      }
    }
  }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
  Text(text = "Hello $name!", modifier = modifier)
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
  MyApplicationTheme { Greeting("Android") }
}
