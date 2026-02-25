package com.miataru

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.miataru.ui.main.MainViewModel
import com.miataru.ui.main.MiataruApp
import com.miataru.ui.theme.MiataruTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val mainViewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mainViewModel.handleDeepLink(intent?.data)

        setContent {
            MiataruTheme {
                MiataruApp(mainViewModel = mainViewModel)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        mainViewModel.handleDeepLink(intent.data)
    }
}
