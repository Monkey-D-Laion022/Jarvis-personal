package com.jarvis.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.jarvis.android.core.ui.JarvisTheme
import com.jarvis.android.feature.chat.ChatScreen
import com.jarvis.android.feature.chat.ChatViewModel
import com.jarvis.android.feature.chat.ChatViewModelFactory

class MainActivity : ComponentActivity() {
    private val viewModel: ChatViewModel by viewModels {
        ChatViewModelFactory((application as JarvisApp).container)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            JarvisTheme {
                ChatScreen(viewModel)
            }
        }
    }
}
