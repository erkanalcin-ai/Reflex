package com.erkan.reflex.model

enum class DuelPlayer(val title: String) {
    ONE("OYUNCU 1"),
    TWO("OYUNCU 2")
}

enum class DuelPhase {
    READY,
    WAITING,
    GO,
    ROUND_RESULT,
    FINISHED
}

data class TwoPlayerDuelState(
    val phase: DuelPhase = DuelPhase.READY,
    val playerOneScore: Int = 0,
    val playerTwoScore: Int = 0,
    val round: Int = 0,
    val earlyPlayers: Set<DuelPlayer> = emptySet(),
    val roundWinner: DuelPlayer? = null,
    val matchWinner: DuelPlayer? = null
) {
    fun scoreFor(player: DuelPlayer): Int = when (player) {
        DuelPlayer.ONE -> playerOneScore
        DuelPlayer.TWO -> playerTwoScore
    }
}
