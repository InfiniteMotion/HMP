package com.example.hearablemusicplayer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.example.hearablemusicplayer.domain.model.MusicExtra
import com.example.hearablemusicplayer.domain.model.MusicLabel

@Composable
fun Capsule(
    text: String,
    style: TextStyle = MaterialTheme.typography.bodyMedium,
    color: Color = MaterialTheme.colorScheme.primary,
){
    Text(
        text = text,
        style = style,
        color = MaterialTheme.colorScheme.onPrimary,
        modifier = Modifier
            .background(
                color = color,
                shape = RoundedCornerShape(16.dp)
            )
            .padding(horizontal = 12.dp, vertical = 6.dp)
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LabelsCapsule(
    extra: MusicExtra?,
    labels: List<MusicLabel?>
){
    Column(
        modifier = Modifier.fillMaxSize()
            .padding(vertical = 16.dp, horizontal = 16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        val color = MaterialTheme.colorScheme.tertiary
        var style = MaterialTheme.typography.labelMedium

        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            extra?.let { it1 ->
                listOf(
                    Triple("Bit" , it1.bitRate.toString(),"kbps"),
                    Triple("SampleRate" , it1.sampleRate.toString(),"Hz"),
                    Triple("FileSize" , it1.fileSize.toString(),"Byte"),
                    Triple("Format" , it1.format.toString(),"")
                ).forEach {
                    Capsule(text = "${it.first}: ${it.second} ${it.third}",color=color,style=style)
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        style = MaterialTheme.typography.labelSmall
        FlowRow(
            maxItemsInEachRow = 4,
            modifier = Modifier
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ){
            labels.forEach {
                it?.let {
                    Capsule(text = it.label.toString(),style=style)
                }
            }
        }
    }

}
