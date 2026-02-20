package com.svp.taskhelpercomposemvi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.svp.taskhelpercomposemvi.presentation.TaskScreen
import com.svp.taskhelpercomposemvi.view.ui.theme.TaskHelperComposeMVITheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TaskHelperComposeMVITheme {
                TaskScreen()
            }
        }
    }
}