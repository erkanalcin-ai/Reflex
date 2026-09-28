package com.erkan.reflex.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.erkan.reflex.R
import com.erkan.reflex.model.Balloon
import kotlin.math.roundToInt

@Composable
fun BalloonView(
    balloon: Balloon,
    containerWidth: Float,
    containerHeight: Float,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val sizePx = with(density) { balloon.sizeDp.dp.toPx() }
    val left = (containerWidth * balloon.xRatio - sizePx / 2f).roundToInt()
    val top = (containerHeight * balloon.yRatio - sizePx / 2f).roundToInt()

    Box(
        modifier = modifier
            .offset { IntOffset(left, top) }
            .size(balloon.sizeDp.dp)
    ) {
        Image(
            painter = painterResource(R.drawable.premium_game_balloon),
            contentDescription = "Balona dokun",
            contentScale = ContentScale.Fit,
            colorFilter = ColorFilter.tint(
                color = lerp(Color.White, balloon.color, 0.22f),
                blendMode = BlendMode.Modulate
            ),
            modifier = Modifier.matchParentSize()
        )
    }
}
