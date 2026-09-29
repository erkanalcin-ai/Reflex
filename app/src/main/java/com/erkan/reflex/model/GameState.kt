package com.erkan.reflex.model

enum class GameStatus {
    NOT_STARTED,
    WAITING_BALLOON,
    BALLOON_ACTIVE,
    GAME_OVER
}

data class GameState(
    val status: GameStatus = GameStatus.NOT_STARTED,
    val score: Int = 0,
    val lives: Int = 3,
    val highScore: Int = 0,
    val balloonIndex: Int = 0, // 0'dan 99'a kadar (100 balon)
    val difficultyIndex: Int = 0, // Başarıda +1, tamamen kaçırmada -1 zorluk kademesi
    val lastBalloonDurationMs: Long = 1000L, // Arka plan son doğan balonun süresini izler
    val currentBalloons: List<Balloon> = emptyList(),
    val lastReactionTimeMs: Long? = null,
    val bestReactionTimeMs: Long? = null,
    val missedCount: Int = 0,
    val poppedCount: Int = 0
) {
    val currentBalloon: Balloon?
        get() = currentBalloons.firstOrNull()

    val currentBalloonNumber: Int
        get() = balloonIndex + 1

    val isCompleted: Boolean
        get() = balloonIndex >= 100 && lives > 0
}
