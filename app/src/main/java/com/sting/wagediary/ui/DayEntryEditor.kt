package com.sting.wagediary.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sting.wagediary.WageDraft
import com.sting.wagediary.data.RecentInputs
import com.sting.wagediary.data.Settings
import java.time.LocalDate

/**
 * 每天输入编辑器
 *  - 草稿模式:不维护 local var,所有字段通过 WageDraft + onDraftChange 提给父级
 *  - 历史选择:点输入框弹底部 ModalBottomSheet
 *  - 手动填 / 选历史 = 行为完全一致(都只改 draft + markDirty)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DayEntryEditor(
    entryDate: LocalDate,
    settings: Settings,
    draft: WageDraft,
    onDraftChange: (WageDraft) -> Unit,
    onMarkDirty: (LocalDate) -> Unit,
    onCommit: (WageDraft) -> Unit,
    onClear: (LocalDate) -> Unit
) {
    val context = LocalContext.current

    val extraHistory = remember { RecentInputs.getExtraOvertime(context) }
    val noteHistory = remember { RecentInputs.getDayNote(context) }

    var showClearDialog by rememberSaveable { mutableStateOf(false) }
    var historySheet by remember { mutableStateOf<HistoryField?>(null) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // 1. 启用日薪
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = draft.dailyEnabled,
                    onCheckedChange = {
                        onDraftChange(draft.copy(dailyEnabled = it))
                        onMarkDirty(entryDate)
                    }
                )
                Text(
                    "启用日薪",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.clickable {
                        onDraftChange(draft.copy(dailyEnabled = !draft.dailyEnabled))
                        onMarkDirty(entryDate)
                    }
                )
            }
            if (draft.dailyEnabled) {
                NumberField(
                    label = "日薪(¥/天)",
                    value = draft.dailyText,
                    onValueChange = {
                        onDraftChange(draft.copy(dailyText = it))
                        onMarkDirty(entryDate)
                    }
                )
            } else {
                Text(
                    "未启用日薪(今天只有加班 / 额外加班工资)",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    modifier = Modifier.padding(start = 16.dp, top = 2.dp, bottom = 8.dp)
                )
            }

            // 2. 加班时薪
            NumberField(
                label = "加班时薪(¥/小时)",
                value = draft.hourlyText,
                onValueChange = {
                    onDraftChange(draft.copy(hourlyText = it))
                    onMarkDirty(entryDate)
                }
            )

            // 3. 倍数 + 小时
            Row(modifier = Modifier.fillMaxWidth()) {
                Box(modifier = Modifier.weight(1f)) {
                    NumberField(
                        label = "加班倍数",
                        value = draft.multiplierText,
                        onValueChange = {
                            onDraftChange(draft.copy(multiplierText = it))
                            onMarkDirty(entryDate)
                        }
                    )
                }
                Spacer(Modifier.width(12.dp))
                Box(modifier = Modifier.weight(1f)) {
                    NumberField(
                        label = "加班小时数",
                        value = draft.hoursText,
                        onValueChange = {
                            onDraftChange(draft.copy(hoursText = it))
                            onMarkDirty(entryDate)
                        }
                    )
                }
            }
            Text(
                "周末 ${settings.weekendMultiplier} 倍(可在设置改)",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                modifier = Modifier.padding(top = 2.dp, bottom = 8.dp)
            )

            Spacer(Modifier.height(4.dp))
            Divider()
            Spacer(Modifier.height(8.dp))

            // 4. 额外加班 — 点输入框弹历史(有历史时,默认只读)
            if (extraHistory.isNotEmpty()) {
                // 有历史 → 显示 "上次输入: ¥xxx" + 可点编辑按钮切换输入模式
                HistoryInputRow(
                    label = "额外加班(¥,可选)",
                    history = extraHistory,
                    currentValue = draft.extraText,
                    onPickFromHistory = { picked ->
                        onDraftChange(draft.copy(extraText = picked))
                        onMarkDirty(entryDate)
                    },
                    onStartEdit = {
                        // 切到普通输入:用当前值覆盖(让用户继续编辑)
                    }
                )
                // 也允许手动输入(用 OutlinedTextField)
                NumberField(
                    label = "或手动输入额外加班",
                    value = draft.extraText,
                    onValueChange = {
                        onDraftChange(draft.copy(extraText = it))
                        onMarkDirty(entryDate)
                    }
                )
            } else {
                // 没历史 → 普通输入
                NumberField(
                    label = "额外加班(¥,可选)",
                    value = draft.extraText,
                    onValueChange = {
                        onDraftChange(draft.copy(extraText = it))
                        onMarkDirty(entryDate)
                    }
                )
            }
            Spacer(Modifier.height(4.dp))

            // 5. 备注 — 同样处理
            if (noteHistory.isNotEmpty()) {
                HistoryInputRow(
                    label = "备注(对当天工资的说明)",
                    history = noteHistory,
                    currentValue = draft.noteText,
                    onPickFromHistory = { picked ->
                        onDraftChange(draft.copy(noteText = picked))
                        onMarkDirty(entryDate)
                    },
                    onStartEdit = {}
                )
                OutlinedTextField(
                    value = draft.noteText,
                    onValueChange = {
                        onDraftChange(draft.copy(noteText = it))
                        onMarkDirty(entryDate)
                    },
                    label = { Text("或手动输入备注") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            } else {
                OutlinedTextField(
                    value = draft.noteText,
                    onValueChange = {
                        onDraftChange(draft.copy(noteText = it))
                        onMarkDirty(entryDate)
                    },
                    label = { Text("备注(对当天工资的说明)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            Spacer(Modifier.height(12.dp))

            // 当天小计
            val preview = draft.previewTotal(settings)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("当天工资小计", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                Text(
                    "¥ %.2f".format(preview),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(Modifier.height(12.dp))

            // 清空 + 保存
            Row(modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(
                    onClick = { showClearDialog = true },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("清空")
                }
                Spacer(Modifier.width(12.dp))
                Button(
                    onClick = { onCommit(draft) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("保存")
                }
            }
        }
    }

    // 清空确认弹框
    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("清空当天数据") },
            text = { Text("确定要清空 $entryDate 的所有工资数据吗?此操作不可撤销。") },
            confirmButton = {
                TextButton(onClick = {
                    showClearDialog = false
                    onClear(entryDate)
                }) {
                    Text("确定清空", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("取消")
                }
            }
        )
    }

    // 历史选择底部抽屉
    if (historySheet != null) {
        val field = historySheet!!
        val items = when (field) {
            HistoryField.ExtraOvertime -> extraHistory
            HistoryField.DayNote -> noteHistory
        }
        ModalBottomSheet(
            onDismissRequest = { historySheet = null }
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = when (field) {
                        HistoryField.ExtraOvertime -> "选择历史额外加班金额"
                        HistoryField.DayNote -> "选择历史备注"
                    },
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(Modifier.height(12.dp))
                if (items.isEmpty()) {
                    Text(
                        "暂无历史记录",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                } else {
                    items.forEach { value ->
                        ListItem(
                            headlineContent = {
                                Text(
                                    when (field) {
                                        HistoryField.ExtraOvertime -> "¥ $value"
                                        HistoryField.DayNote -> value
                                    },
                                    fontSize = 15.sp
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    val newDraft = when (field) {
                                        HistoryField.ExtraOvertime -> draft.copy(extraText = value)
                                        HistoryField.DayNote -> draft.copy(noteText = value)
                                    }
                                    onDraftChange(newDraft)
                                    onMarkDirty(entryDate)
                                    historySheet = null
                                }
                        )
                        Divider()
                    }
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

private enum class HistoryField { ExtraOvertime, DayNote }

/**
 * 显示当前值 + 历史下拉选择按钮
 * - 显示:当前值(label + value)
 * - "选择历史 ▼" 按钮:点开 ModalBottomSheet 列最近 3 条
 * - 选完自动关闭 + 调 onPickFromHistory
 * - 用户可直接编辑下方的"或手动输入"输入框
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryInputRow(
    label: String,
    history: List<String>,
    currentValue: String,
    onPickFromHistory: (String) -> Unit,
    onStartEdit: () -> Unit
) {
    var showSheet by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                label,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            Text(
                text = if (currentValue.isNotEmpty()) currentValue else "(空)",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = if (currentValue.isNotEmpty()) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                }
            )
        }
        OutlinedButton(
            onClick = { showSheet = true }
        ) {
            Icon(
                Icons.Default.History,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(4.dp))
            Text("选历史", fontSize = 13.sp)
        }
    }

    if (showSheet) {
        ModalBottomSheet(onDismissRequest = { showSheet = false }) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "从最近 3 条历史选(点空白处取消)",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                Spacer(Modifier.height(8.dp))
                history.forEach { value ->
                    ListItem(
                        headlineContent = {
                            Text(
                                if (currentValue.toDoubleOrNull() != null) "¥ $value" else value,
                                fontSize = 15.sp
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onPickFromHistory(value)
                                showSheet = false
                            }
                    )
                    Divider()
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NumberField(
    label: String,
    value: String,
    placeholder: String? = null,
    onValueChange: (String) -> Unit,
    onClickIfHistory: (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = if (placeholder != null) {
            { Text(placeholder, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)) }
        } else null,
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClickIfHistory != null) Modifier.clickable(onClick = onClickIfHistory) else Modifier),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        singleLine = true,
        trailingIcon = trailingIcon
    )
}
