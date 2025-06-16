package com.codelabs.securitymodule

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.codelabs.securitymodule.ui.theme.SecurityModuleTheme
import com.ipay.securitykit.SecurityUtils

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SecurityModuleTheme {
                Scaffold {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(it),
                        contentAlignment = Alignment.Center

                    ) {
                        if (SecurityUtils(application).isRooted()) {
                            Text(text = "Hello, This Device is Detected as Rooted")
                        } else {
                            Text(text = "Hello, Welcome to Security Module")
                        }
                    }
                }
            }
        }
    }
}