package com.example.hearablemusicplayer.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ListeningChart(
    text: String,
    data: List<Int>,
    days: List<String>
) {
    val maxBarHeight = 70.dp
    val maxValue = (data.maxOrNull() ?: 1).toFloat() // 避免除以0

    Card(
        modifier = Modifier
            .size(height = 200.dp, width = 340.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.background
        ),
        border = BorderStroke(2.dp, color = MaterialTheme.colorScheme.primary),
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.titleLarge,
            )
        }

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.Bottom
        ) {
            data.forEachIndexed { index, value ->
                val barHeight = if (maxValue == 0f) 0.dp
                else (value / maxValue * maxBarHeight.value).dp

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = value.toString(),
                        fontSize = 10.sp,
                        color = Color.Gray
                    )
                    Box(
                        modifier = Modifier
                            .width(16.dp)
                            .height(barHeight)
                            .background(
                                if (index == data.lastIndex) MaterialTheme.colorScheme.secondary
                                else MaterialTheme.colorScheme.tertiary,
                                RoundedCornerShape(4.dp)
                            )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = days[index],
                        fontSize = 12.sp,
                    )
                }
            }
        }
    }
}


