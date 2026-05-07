package com.turismo.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.turismo.app.ui.TurismoRoot
import com.turismo.app.ui.TurismoViewModel
import com.turismo.app.ui.theme.TurismoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TurismoTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val vm: TurismoViewModel = viewModel()
                    TurismoRoot(viewModel = vm)
                }
            }
        }
    }
}
