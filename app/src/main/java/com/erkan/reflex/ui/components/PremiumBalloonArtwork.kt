package com.erkan.reflex.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.erkan.reflex.R

@Composable
fun PremiumBalloonArtwork(
    modifier: Modifier = Modifier,
    width: Dp = 270.dp,
    height: Dp = 330.dp
) {
    Image(
        painter = painterResource(R.drawable.premium_glass_balloon),
        contentDescription = null,
        contentScale = ContentScale.Fit,
        modifier = modifier.size(width, height)
    )
}
