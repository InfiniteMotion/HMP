package com.example.hearablemusicplayer.ui.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun TimerDialog(
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    // 原始预设时间选项
    val timeOptions = listOf(0, 15, 30, 45, 60, 90)

    // 新增状态：记录用户输入的自定义分钟数
    var selectedMinutes by remember { mutableIntStateOf(0) }
    var customMinutes by remember { mutableIntStateOf(0) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("定时关闭") },
        text = {
            // 使用Column组合预设选项和自定义输入
            Column(modifier = Modifier.fillMaxWidth()) {
                // 预设时间选项网格
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 200.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    userScrollEnabled = false
                ) {
                    items(timeOptions) { minutes ->
                        Column(
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .padding(4.dp)
                                .fillMaxSize()
                        ) {
                            RadioButton(
                                selected = selectedMinutes == minutes,
                                onClick = { selectedMinutes = minutes }
                            )
                            Text(
                                text = if (minutes == 0) "关闭定时" else "$minutes 分钟",
                                modifier = Modifier.padding(start = 4.dp)
                            )
                        }
                    }
                }

                // 自定义输入区域
                Spacer(modifier = Modifier.height(24.dp)) // 添加间距
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Text("或", color = Color.Gray)

                    // 数字输入框
                    OutlinedTextField(
                        value = customMinutes.toString(),
                        onValueChange = { input ->
                            // 尝试解析输入值为整数
                            try {
                                val minutes = input.toIntOrNull() ?: return@OutlinedTextField
                                if (minutes >= 0) {
                                    customMinutes = minutes
                                    selectedMinutes = minutes // 同步到选中值
                                } else {
                                    // 输入负数时重置
                                    customMinutes = 0
                                }
                            } catch (_: NumberFormatException) {
                                // 非法字符输入时忽略
                            }
                        },
                        label = { Text("自定义分钟") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(selectedMinutes) }) {
                Text("确定")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}
