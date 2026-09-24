package com.erkan.reflex.model

import androidx.compose.ui.graphics.Color

/**
 * Ekranda beliren balon modeli.
 *
 * @param id Benzersiz kimlik
 * @param xRatio Ekranın yatay oranı (0.1f - 0.9f arası güvenli alan)
 * @param yRatio Ekranın dikey oranı (0.15f - 0.85f arası güvenli alan)
 * @param lifespanMs Balonun ekranda kalacağı milisaniye süresi (1000ms -> 16ms)
 * @param color Kademeye göre belirlenen renk (Gri -> Mor -> Kırmızı)
 * @param spawnTime Balonun ekranda belirdiği anlık milisaniye zaman damgası
 * @param sizeDp Balon boyutu (dp)
 */
data class Balloon(
    val id: String,
    val xRatio: Float,
    val yRatio: Float,
    val lifespanMs: Long,
    val color: Color,
    val spawnTime: Long = System.currentTimeMillis(),
    val sizeDp: Float = 76f
)
