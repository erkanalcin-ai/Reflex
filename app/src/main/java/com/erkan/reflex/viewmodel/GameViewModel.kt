package com.erkan.reflex.viewmodel

import android.app.Application
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.erkan.reflex.model.Balloon
import com.erkan.reflex.model.FloatingText
import com.erkan.reflex.model.GameState
import com.erkan.reflex.model.GameStatus
import com.erkan.reflex.model.Particle
import com.erkan.reflex.ui.theme.BalloonGray
import com.erkan.reflex.ui.theme.BalloonPurple
import com.erkan.reflex.ui.theme.BalloonRed
import com.erkan.reflex.ui.theme.NeonCyan
import com.erkan.reflex.ui.theme.NeonGreen
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("reflex_prefs", Context.MODE_PRIVATE)

    private val _gameState = MutableStateFlow(GameState(highScore = loadHighScore()))
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    val particles = mutableStateListOf<Particle>()
    val floatingTexts = mutableStateListOf<FloatingText>()

    private var balloonJob: Job? = null
    private var particleAnimationJob: Job? = null

    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val manager = application.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        manager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        application.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    init {
        startParticleLoop()
    }

    private fun loadHighScore(): Int {
        return prefs.getInt("high_score", 0)
    }

    private fun saveHighScore(score: Int) {
        prefs.edit().putInt("high_score", score).apply()
    }

    fun startGame() {
        balloonJob?.cancel()
        particles.clear()
        floatingTexts.clear()

        _gameState.update {
            GameState(
                status = GameStatus.WAITING_BALLOON,
                score = 0,
                lives = 3,
                highScore = loadHighScore(),
                balloonIndex = 0,
                currentBalloons = emptyList(),
                lastReactionTimeMs = null,
                bestReactionTimeMs = null,
                missedCount = 0,
                poppedCount = 0
            )
        }

        scheduleNextBalloon()
    }

    private fun scheduleNextBalloon() {
        balloonJob?.cancel()
        balloonJob = viewModelScope.launch {
            val state = _gameState.value
            if (state.lives <= 0 || state.balloonIndex >= 100) {
                endGame()
                return@launch
            }

            _gameState.update { it.copy(status = GameStatus.WAITING_BALLOON, currentBalloons = emptyList()) }

            // Dalga başlangıçları arasında 400–1000 ms rastgele bekleme.
            val waitDelay = Random.nextLong(400L, 1001L)
            delay(waitDelay)

            // Süre hesabı: 1. balon = 1000ms, 100. balon = 16ms
            val index = _gameState.value.balloonIndex.coerceIn(0, 99)
            val durationMs = maxOf(16L, 1000L - (index * (1000L - 16L) / 99L))

            // Renk geçişi: 0.0 -> Gri, 0.5 -> Mor, 1.0 -> Kırmızı
            val progress = (index / 99f).coerceIn(0f, 1f)
            val color = if (progress <= 0.5f) {
                lerp(BalloonGray, BalloonPurple, progress * 2f)
            } else {
                lerp(BalloonPurple, BalloonRed, (progress - 0.5f) * 2f)
            }

            val spawnTime = System.currentTimeMillis()
            val positions = when (Random.nextInt(100)) {
                in 0..4 -> listOf(0.29f to 0.20f, 0.71f to 0.50f, 0.29f to 0.80f)
                in 5..24 -> if (Random.nextBoolean()) {
                    listOf(0.34f to 0.28f, 0.66f to 0.72f)
                } else {
                    listOf(0.66f to 0.28f, 0.34f to 0.72f)
                }
                else -> listOf(
                    (Random.nextFloat() * 0.24f + 0.38f) to (Random.nextFloat() * 0.40f + 0.30f)
                )
            }
            val balloons = positions.map { (xRatio, yRatio) ->
                Balloon(
                    id = UUID.randomUUID().toString(),
                    xRatio = xRatio,
                    yRatio = yRatio,
                    lifespanMs = durationMs,
                    color = color,
                    spawnTime = spawnTime,
                    sizeDp = 228f
                )
            }

            _gameState.update {
                it.copy(
                    status = GameStatus.BALLOON_ACTIVE,
                    currentBalloons = balloons
                )
            }

            // Balonun ekranda kalma süresini başlat
            delay(durationMs)

            // Süre dolduğunda balon hala patlatılmadıysa kaçırıldı sayılır
            if (_gameState.value.currentBalloons.any { it.spawnTime == spawnTime }) {
                onBalloonMissed()
            }
        }
    }

    fun onBalloonTapped(balloonId: String, touchX: Float, touchY: Float) {
        val state = _gameState.value
        val current = state.currentBalloons.firstOrNull { it.id == balloonId } ?: return
        val now = System.currentTimeMillis()
        val reactionTime = now - current.spawnTime
        val remainingMs = maxOf(0L, current.lifespanMs - reactionTime)

        // Parçacık patlama efekti oluştur
        createExplosion(touchX, touchY, current.color)

        // Kalan süreye göre rozet rengi
        val badgeColor = when {
            remainingMs >= 350L -> NeonGreen
            remainingMs >= 120L -> NeonCyan
            else -> Color(0xFFFF9100) // Kritik son an!
        }

        // Balonun hemen üstünde anlık kalan süre bildirimi
        floatingTexts.add(
            FloatingText(
                id = now,
                text = "${remainingMs} ms kaldı",
                remainingMs = remainingMs,
                x = touchX,
                y = touchY - 70f,
                color = badgeColor
            )
        )

        // Titreşim bildirimi
        vibrate(short = true)

        val newScore = state.score + 1
        val newHighScore = maxOf(_gameState.value.highScore, newScore)
        if (newHighScore > _gameState.value.highScore) {
            saveHighScore(newHighScore)
        }

        val bestTime = state.bestReactionTimeMs?.let { minOf(it, reactionTime) } ?: reactionTime
        val remainingBalloons = state.currentBalloons.filterNot { it.id == balloonId }
        val newPoppedCount = state.poppedCount + 1
        val extraLifeEarned = newPoppedCount % 10 == 0

        _gameState.update {
            it.copy(
                score = newScore,
                highScore = newHighScore,
                poppedCount = newPoppedCount,
                lives = if (extraLifeEarned) it.lives + 1 else it.lives,
                lastReactionTimeMs = reactionTime,
                bestReactionTimeMs = bestTime,
                balloonIndex = if (remainingBalloons.isEmpty()) it.balloonIndex + 1 else it.balloonIndex,
                currentBalloons = remainingBalloons
            )
        }

        if (remainingBalloons.isEmpty()) {
            balloonJob?.cancel()
            scheduleNextBalloon()
        }
    }

    private fun onBalloonMissed() {
        vibrate(short = false)

        val newScore = _gameState.value.score - 1
        val newLives = _gameState.value.lives - 1
        val newMissed = _gameState.value.missedCount + 1

        _gameState.update {
            it.copy(
                score = newScore,
                lives = newLives,
                missedCount = newMissed,
                balloonIndex = it.balloonIndex + 1,
                currentBalloons = emptyList()
            )
        }

        if (newLives <= 0 || _gameState.value.balloonIndex >= 100) {
            endGame()
        } else {
            scheduleNextBalloon()
        }
    }

    private fun endGame() {
        balloonJob?.cancel()
        _gameState.update { it.copy(status = GameStatus.GAME_OVER, currentBalloons = emptyList()) }
    }

    private fun createExplosion(x: Float, y: Float, balloonColor: Color) {
        val particleCount = 28
        val newParticles = mutableListOf<Particle>()

        for (i in 0 until particleCount) {
            val angle = Random.nextFloat() * 2f * Math.PI.toFloat()
            val speed = Random.nextFloat() * 12f + 4f
            val vx = cos(angle) * speed
            val vy = sin(angle) * speed

            val pColor = when (Random.nextInt(4)) {
                0 -> Color.White
                1 -> Color(0xFFFFD700)
                else -> balloonColor
            }

            newParticles.add(
                Particle(
                    id = System.nanoTime() + i,
                    x = x,
                    y = y,
                    vx = vx,
                    vy = vy,
                    color = pColor,
                    radius = Random.nextFloat() * 8f + 4f,
                    alpha = 1f,
                    lifetime = 1f
                )
            )
        }

        particles.addAll(newParticles)
    }

    private fun startParticleLoop() {
        particleAnimationJob?.cancel()
        particleAnimationJob = viewModelScope.launch {
            while (isActive) {
                delay(16L) // ~60 FPS

                if (particles.isNotEmpty()) {
                    val updated = particles.mapNotNull { p ->
                        val newAlpha = p.alpha - 0.04f
                        if (newAlpha <= 0f) null
                        else {
                            p.copy(
                                x = p.x + p.vx,
                                y = p.y + p.vy + 0.35f,
                                vx = p.vx * 0.94f,
                                vy = p.vy * 0.94f,
                                alpha = newAlpha
                            )
                        }
                    }
                    particles.clear()
                    particles.addAll(updated)
                }

                if (floatingTexts.isNotEmpty()) {
                    val updatedTexts = floatingTexts.mapNotNull { ft ->
                        val newAlpha = ft.alpha - 0.025f
                        if (newAlpha <= 0f) null
                        else {
                            ft.copy(
                                y = ft.y - 1.8f,
                                alpha = newAlpha
                            )
                        }
                    }
                    floatingTexts.clear()
                    floatingTexts.addAll(updatedTexts)
                }
            }
        }
    }

    private fun vibrate(short: Boolean) {
        try {
            val duration = if (short) 35L else 120L
            val amplitude = if (short) VibrationEffect.DEFAULT_AMPLITUDE else 200
            vibrator?.vibrate(VibrationEffect.createOneShot(duration, amplitude))
        } catch (_: Exception) {
        }
    }

    override fun onCleared() {
        super.onCleared()
        balloonJob?.cancel()
        particleAnimationJob?.cancel()
    }
}
