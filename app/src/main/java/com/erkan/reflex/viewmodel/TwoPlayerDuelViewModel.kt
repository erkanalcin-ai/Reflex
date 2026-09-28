package com.erkan.reflex.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.erkan.reflex.model.DuelPhase
import com.erkan.reflex.model.DuelPlayer
import com.erkan.reflex.model.TwoPlayerDuelState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.random.Random

enum class DuelTapResult {
    FALSE_START,
    HIT,
    IGNORED
}

class TwoPlayerDuelViewModel : ViewModel() {

    private val stateLock = Any()
    private val _state = MutableStateFlow(TwoPlayerDuelState())
    val state: StateFlow<TwoPlayerDuelState> = _state.asStateFlow()

    private var roundJob: Job? = null

    fun startMatch() {
        roundJob?.cancel()
        synchronized(stateLock) {
            _state.value = TwoPlayerDuelState(phase = DuelPhase.WAITING, round = 1)
        }

        roundJob = viewModelScope.launch {
            while (isActive) {
                // Her turda yeşil bekleme 1–3 saniye arasında rastgele seçilir.
                delay(Random.nextLong(1_000L, 3_001L))
                synchronized(stateLock) {
                    if (_state.value.phase == DuelPhase.WAITING) {
                        _state.value = _state.value.copy(phase = DuelPhase.GO)
                    }
                }

                // İlk geçerli dokunuşu bekle; kimse dokunmazsa turu zaman aşımına uğrat.
                val result = withTimeoutOrNull(REACTION_WINDOW_MS) {
                    _state.first { it.phase != DuelPhase.GO }
                }
                if (result == null) {
                    synchronized(stateLock) {
                        if (_state.value.phase == DuelPhase.GO) {
                            _state.value = _state.value.copy(phase = DuelPhase.ROUND_RESULT)
                        }
                    }
                }

                if (!isActive || _state.value.phase == DuelPhase.FINISHED) break
                if (_state.value.phase != DuelPhase.ROUND_RESULT) break
                delay(RESULT_DURATION_MS)
                synchronized(stateLock) {
                    val current = _state.value
                    if (current.phase == DuelPhase.ROUND_RESULT) {
                        _state.value = current.copy(
                            phase = DuelPhase.WAITING,
                            round = current.round + 1,
                            earlyPlayers = emptySet(),
                            roundWinner = null
                        )
                    }
                }
                if (!isActive || _state.value.phase == DuelPhase.FINISHED) break
            }
        }
    }

    fun onPlayerTapped(player: DuelPlayer): DuelTapResult = synchronized(stateLock) {
            val current = _state.value
            when (current.phase) {
                DuelPhase.WAITING -> {
                    // En fazla bir erken dokunuş cezası ve oyuncu başına tur uygulanır.
                    if (player in current.earlyPlayers) {
                        DuelTapResult.IGNORED
                    } else {
                        val penalized = current.withScore(player, current.scoreFor(player) - 1)
                        _state.value = penalized.copy(earlyPlayers = penalized.earlyPlayers + player)
                        DuelTapResult.FALSE_START
                    }
                }

                DuelPhase.GO -> {
                    // Erken dokunuş cezası puana işlendi; kırmızı sinyalde iki oyuncu da yarışır.
                    val scored = current.withScore(player, current.scoreFor(player) + 1)
                    val winsMatch = scored.scoreFor(player) >= WINNING_SCORE
                    _state.value = scored.copy(
                        phase = if (winsMatch) DuelPhase.FINISHED else DuelPhase.ROUND_RESULT,
                        roundWinner = player,
                        matchWinner = if (winsMatch) player else null
                    )
                    DuelTapResult.HIT
                }

                DuelPhase.READY,
                DuelPhase.ROUND_RESULT,
                DuelPhase.FINISHED -> DuelTapResult.IGNORED
            }
        }

    fun reset() {
        roundJob?.cancel()
        roundJob = null
        synchronized(stateLock) {
            _state.value = TwoPlayerDuelState()
        }
    }

    private fun TwoPlayerDuelState.withScore(player: DuelPlayer, score: Int): TwoPlayerDuelState = when (player) {
        DuelPlayer.ONE -> copy(playerOneScore = score)
        DuelPlayer.TWO -> copy(playerTwoScore = score)
    }

    private companion object {
        const val WINNING_SCORE = 10
        const val REACTION_WINDOW_MS = 2_500L
        const val RESULT_DURATION_MS = 750L
    }
}
