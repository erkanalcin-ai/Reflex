package com.erkan.reflex.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.erkan.reflex.model.GameState
import com.erkan.reflex.ui.theme.PremiumGold
import com.erkan.reflex.ui.theme.PremiumMuted
import com.erkan.reflex.ui.theme.TextPrimary

@Composable
fun ScoreBoard(
    gameState: GameState,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            AnimatedContent(
                targetState = gameState.score,
                transitionSpec = {
                    slideInVertically { it } togetherWith slideOutVertically { -it }
                },
                label = "ScoreAnimation"
            ) { score ->
                Text(
                    text = "$score",
                    color = TextPrimary,
                    fontFamily = FontFamily.Serif,
                    fontSize = 38.sp,
                    lineHeight = 42.sp,
                    fontWeight = FontWeight.Normal
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(7.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (i in 1..3) {
                    val isAlive = i <= gameState.lives
                    Icon(
                        imageVector = if (isAlive) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        contentDescription = "Can $i",
                        tint = if (isAlive) PremiumGold else Color.White.copy(alpha = 0.2f),
                        modifier = Modifier.size(18.dp)
                    )
                }
                if (gameState.lives > 3) {
                    Text(
                        text = "+${gameState.lives - 3}",
                        color = PremiumGold,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Text(
                text = "${gameState.currentBalloonNumber.coerceAtMost(100)} / 100",
                color = TextPrimary.copy(alpha = 0.9f),
                fontFamily = FontFamily.Serif,
                fontSize = 15.sp,
                fontWeight = FontWeight.Normal
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        val progress = gameState.currentBalloonNumber.coerceIn(0, 100) / 100f
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.13f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress)
                    .fillMaxHeight()
                    .clip(CircleShape)
                    .background(PremiumGold.copy(alpha = 0.9f))
            )
        }
    }
}

@Composable
fun GameMetricsStrip(
    gameState: GameState,
    modifier: Modifier = Modifier
) {
    val balloonDuration = gameState.currentBalloon?.lifespanMs ?: run {
        val index = gameState.balloonIndex.coerceIn(0, 99)
        maxOf(16L, 1000L - (index * (1000L - 16L) / 99L))
    }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(22.dp))
            .background(Color(0x301F2329))
            .border(1.dp, Color.White.copy(alpha = 0.13f), RoundedCornerShape(22.dp))
            .padding(horizontal = 22.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "BALON",
                color = PremiumMuted,
                fontSize = 9.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 2.sp
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = "$balloonDuration ms",
                color = TextPrimary,
                fontFamily = FontFamily.Serif,
                fontSize = 27.sp,
                fontWeight = FontWeight.Normal
            )
        }

        Box(
            modifier = Modifier
                .width(1.dp)
                .height(48.dp)
                .background(Color.White.copy(alpha = 0.15f))
        )

        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = "REFLEKS",
                color = PremiumMuted,
                fontSize = 9.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 2.sp
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = gameState.lastReactionTimeMs?.let { "$it ms" } ?: "—",
                color = PremiumGold,
                fontFamily = FontFamily.Serif,
                fontSize = 27.sp,
                fontWeight = FontWeight.Normal
            )
        }
    }
}
