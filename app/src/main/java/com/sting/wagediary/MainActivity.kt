package com.sting.wagediary

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sting.wagediary.ui.HomeScreen
import com.sting.wagediary.ui.SettingsScreen
import com.sting.wagediary.ui.WageTheme

/** App 入口(极简:ComponentActivity + AppRoot) */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // 全局异常防御:避免 Compose/Icon 抛 IllegalStateException 时把 Activity 整个 finish
        val prevHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            Log.e("WageDiary", "未捕获异常 thread=${thread.name}", throwable)
            // 调用上一个 handler(保留系统默认行为)
            prevHandler?.uncaughtException(thread, throwable)
        }

        setContent {
            WageTheme {
                AppRoot()
            }
        }
    }
}

@Composable
fun AppRoot() {
    val viewModel: WageViewModel = viewModel()
    val state by viewModel.state.collectAsState()
    var showSettings by remember { mutableStateOf(false) }

    if (showSettings) {
        SettingsScreen(
            settings = state.settings,
            onSettingsChange = viewModel::updateSettings,
            onBack = { showSettings = false }
        )
    } else {
        HomeScreen(
            state = state,
            onPrevMonth = viewModel::prevMonth,
            onNextMonth = viewModel::nextMonth,
            onSelectDate = viewModel::selectDate,
            onCreateEntry = viewModel::createEntryFor,
            onUpdateEntry = viewModel::updateEntry,
            onClearEntry = viewModel::clearEntry,
            onMarkDirty = viewModel::markDirty,
            onMarkManyClean = viewModel::markManyClean,
            onOpenSettings = { showSettings = true }
        )
    }
}
