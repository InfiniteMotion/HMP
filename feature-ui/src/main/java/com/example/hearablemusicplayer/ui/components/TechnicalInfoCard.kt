package com.example.hearablemusicplayer.ui.components

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color.Companion.Transparent
import androidx.compose.ui.unit.dp
import com.example.hearablemusicplayer.domain.music.MusicExtra

@Composable
fun TechnicalInfoCard(
    extra: MusicExtra?,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(Transparent),
        border = CardDefaults.outlinedCardBorder(true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 第一行：比特率和采样率
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                extra?.bitRate?.let {
                    TechnicalInfoItem("比特率", "$it kbps")
                }
                extra?.sampleRate?.let {
                    TechnicalInfoItem("采样率", "$it Hz")
                }
            }

            // 第二行：文件大小和格式
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                extra?.fileSize?.let {
                    TechnicalInfoItem("文件大小", formatFileSize(it))
                }
                extra?.format?.let {
                    TechnicalInfoItem("格式", it)
                }
            }
        }
    }
}

@Composable
private fun TechnicalInfoItem(
    label: String,
    value: String
) {
    Row (
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@SuppressLint("DefaultLocale")
private fun formatFileSize(bytes: Long): String {
    return when {
        bytes >= 1024 * 1024 -> "${String.format("%.1f", bytes / (1024.0 * 1024.0))} MB"
        bytes >= 1024 -> "${String.format("%.1f", bytes / 1024.0)} KB"
        else -> "$bytes B"
    }
}