package com.erkan.reflex.model

import androidx.compose.ui.graphics.Color

/**
 * Balon patladığında etrafa saçılan parçacık modeli.
 */
data class Particle(
    val id: Long,
    val x: Float,
    val y: Float,
    val vx: Float,
    val vy: Float,
    val color: Color,
    val radius: Float,
    val alpha: Float = 1f,
    val lifetime: Float = 1f
)

/**
 * Patlama anında balonun üstünde beliren anlık kalan süre bildirimi (+1, 450 ms kaldı vb.)
 */
data class FloatingText(
    val id: Long,
    val text: String,
    val remainingMs: Long,
    val x: Float,
    val y: Float,
    val color: Color,
    val alpha: Float = 1f
)
