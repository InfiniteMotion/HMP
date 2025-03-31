package com.example.hearablemusicplayer.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hearablemusicplayer.R

@Composable
fun Banner(
    bannerName: String,
    imageId: Int,
    modifier: Modifier
){
    val imageModifier = Modifier
        .size(100.dp)
        .shadow(elevation = 5.dp,shape = RoundedCornerShape(10.dp))
        .clip(RoundedCornerShape(10.dp))

    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
    ) {
        Image(
            painter = painterResource(id=imageId),
            contentDescription = bannerName,
            modifier = imageModifier
        )
        Spacer(
            modifier=Modifier.height(5.dp)
        )
        Text(
            text = bannerName,
            fontSize = 12.sp
        )
    }
}

@Composable
fun ListBanner(
    bannerNameF: String,
    bannerNameS: String,
    listNameF: String,
    listNameS: String,
    listNameT: String,
    imageF: Int,
    imageS: Int,
    imageT: Int,
    modifier: Modifier
){
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(top = 16.dp)
                .fillMaxWidth()
        ) {
            Spacer(
                modifier = Modifier.width(20.dp)
            )
            Text(
                text = bannerNameF,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(
                modifier = Modifier.width(10.dp)
            )
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(colorResource(R.color.HDRed))
            )
            Spacer(
                modifier = Modifier.width(10.dp)
            )
            Text(
                text = bannerNameS,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier=Modifier
                .padding(top = 10.dp, bottom = 16.dp)
                .fillMaxWidth(),

        ) {
            Banner(
                bannerName = listNameF,
                imageId = imageF,
                modifier = modifier
            )
            Banner(
                bannerName = listNameS,
                imageId = imageS,
                modifier = modifier
            )
            Banner(
                bannerName = listNameT,
                imageId = imageT,
                modifier = modifier
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ListBannerPreview() {
    ListBanner(
        bannerNameF = stringResource(R.string.banner_daily_FF),
        bannerNameS = stringResource(R.string.banner_daily_FS),
        listNameF = "Taylor Swift",
        listNameS = "Bruno Mars",
        listNameT = "Jay Chou",
        imageF = R.drawable.example_cover_1,
        imageS = R.drawable.example_cover_2,
        imageT = R.drawable.example_cover_3,
        modifier = Modifier
            .fillMaxWidth()
    )
}