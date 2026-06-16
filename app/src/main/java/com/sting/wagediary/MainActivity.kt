package com.sting.wagediary

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sting.wagediary.ui.HomeScreen
import com.sting.wagediary.ui.SettingsScreen
import com.sting.wagediary.ui.WageTheme
import java.io.File

/** App 入口(极简:ComponentActivity + AppRoot) */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // 全局异常防御 + 持久化到文件
        val prevHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                val msg = "未捕获异常 thread=${thread.name}\n${android.util.Log.getStackTraceString(throwable)}"
                Log.e("WageDiary", msg)
                val crashFile = File(filesDir, "last_crash.log")
                crashFile.writeText(msg)
                Toast.makeText(applicationContext, "崩了:${throwable.javaClass.simpleName}:${throwable.message}", Toast.LENGTH_LONG).show()
            } catch (_: Throwable) {}
            prevHandler?.uncaughtException(thread, throwable)
        }

        // 启动时检查上次崩溃日志(由上一轮 uncaughtException 写入)
        val crashFile = File(filesDir, "last_crash.log")
        val initialCrash = if (crashFile.exists()) {
            try {
                crashFile.readText().also { crashFile.delete() }
            } catch (_: Throwable) { null }
        } else null

        setContent {
            WageTheme {
                var crashShown by remember { mutableStateOf(initialCrash) }
                if (crashShown != null) {
                    val ctx = this@MainActivity
                    AlertDialog(
                        onDismissRequest = { crashShown = null },
                        title = { Text("⚠️ 上次崩溃记录") },
                        text = {
                            Column(
                                modifier = androidx.compose.ui.Modifier.verticalScroll(rememberScrollState())
                            ) {
                                Text(
                                    crashShown!!,
                                    fontSize = 11.sp,
                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                                )
                            }
                        },
                        confirmButton = {
                            TextButton(onClick = {
                                val cm = ctx.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                cm.setPrimaryClip(ClipData.newPlainText("crash log", crashShown))
                                Toast.makeText(ctx, "已复制到剪贴板,可粘贴到微信发我", Toast.LENGTH_LONG).show()
                                crashShown = null
                            }) {
                                Text("📋 复制到剪贴板")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { crashShown = null }) {
                                Text("关闭")
                            }
                        }
                    )
                }
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
