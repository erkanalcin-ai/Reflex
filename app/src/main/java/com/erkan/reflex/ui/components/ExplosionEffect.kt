package com.erkan.reflex.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.layout
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.erkan.reflex.model.FloatingText
import com.erkan.reflex.model.Particle
import com.erkan.reflex.ui.theme.DarkSurface
import kotlin.math.roundToInt

@Composable
fun ExplosionEffect(
    particles: List<Particle>,
    floatingTexts: List<FloatingText>,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        // Parçacık çizimi
        Canvas(modifier = Modifier.fillMaxSize()) {
            for (p in particles) {
                drawCircle(
                    color = p.color.copy(alpha = p.alpha.coerceIn(0f, 1f)),
                    radius = p.radius,
                    center = Offset(p.x, p.y)
                )
            }
        }

        // Balon patladığında üstünde beliren anlık kalan süre bildirimi
        for (ft in floatingTexts) {
            val alpha = ft.alpha.coerceIn(0f, 1f)
            Box(
                modifier = Modifier
                    .layout { measurable, constraints ->
                        val placeable = measurable.measure(constraints)
                        layout(placeable.width, placeable.height) {
                            placeable.placeRelative(
                                x = (ft.x - placeable.width / 2f).roundToInt(),
                                y = (ft.y - placeable.height / 2f).roundToInt()
                            )
                        }
                    }
                    .clip(RoundedCornerShape(12.dp))
                    .background(DarkSurface.copy(alpha = 0.85f * alpha))
                    .border(
                        width = 1.5.dp,
                        color = ft.color.copy(alpha = 0.9f * alpha),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "+1",
                        color = Color.White.copy(alpha = alpha),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "⏱ " + ft.text,
                        color = ft.color.copy(alpha = alpha),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        style = TextStyle(
                            shadow = Shadow(
                                color = Color.Black.copy(alpha = 0.9f * alpha),
                                offset = Offset(1f, 1f),
                                blurRadius = 3f
                            )
                        )
                    )
                }
            }
        }
    }
}
