package com.erkan.reflex.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.erkan.reflex.R
import com.erkan.reflex.model.DuelPhase
import com.erkan.reflex.model.DuelPlayer
import com.erkan.reflex.model.TwoPlayerDuelState
import com.erkan.reflex.ui.theme.PremiumGold
import com.erkan.reflex.ui.theme.PremiumInk
import com.erkan.reflex.ui.theme.PremiumMuted
import com.erkan.reflex.ui.theme.TextPrimary
import com.erkan.reflex.viewmodel.DuelTapResult
import com.erkan.reflex.viewmodel.TwoPlayerDuelViewModel

@Composable
fun TwoPlayerDuelScreen(
    viewModel: TwoPlayerDuelViewModel,
    onExit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsState()
    val hapticView = LocalView.current
    BackHandler(onBack = onExit)

    fun handlePlayerTap(player: DuelPlayer) {
        when (viewModel.onPlayerTapped(player)) {
            DuelTapResult.HIT -> hapticView.performReflexHitHaptic()
            DuelTapResult.FALSE_START -> hapticView.performReflexErrorHaptic()
            DuelTapResult.IGNORED -> Unit
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .background(Color(0xFF090B0F))
    ) {
        Image(
            painter = painterResource(R.drawable.premium_arena_1),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            alpha = 0.25f,
            modifier = Modifier.fillMaxSize()
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xC9090B0F))
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp, vertical = 8.dp)
        ) {
            DuelPlayerZone(
                player = DuelPlayer.TWO,
                state = state,
                isOppositeSide = true,
                onTap = { handlePlayerTap(DuelPlayer.TWO) },
                onStart = viewModel::startMatch,
                onRestart = viewModel::startMatch,
                onExit = onExit,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.height(8.dp))
            DuelPlayerZone(
                player = DuelPlayer.ONE,
                state = state,
                isOppositeSide = false,
                onTap = { handlePlayerTap(DuelPlayer.ONE) },
                onStart = viewModel::startMatch,
                onRestart = viewModel::startMatch,
                onExit = onExit,
                modifier = Modifier.weight(1f)
            )
        }

        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(42.dp)
                .clip(CircleShape)
                .background(PremiumGold)
                .border(1.dp, Color.White.copy(alpha = 0.72f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "VS",
                color = PremiumInk,
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            )
        }
    }
}

@Composable
private fun DuelPlayerZone(
    player: DuelPlayer,
    state: TwoPlayerDuelState,
    isOppositeSide: Boolean,
    onTap: () -> Unit,
    onStart: () -> Unit,
    onRestart: () -> Unit,
    onExit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isWinner = state.matchWinner == player
    val canTap = state.phase == DuelPhase.WAITING || state.phase == DuelPhase.GO
    val zoneBrush = when (state.phase) {
        DuelPhase.READY,
        DuelPhase.WAITING -> Brush.linearGradient(listOf(Color(0xF0125238), Color(0xF008271D)))
        DuelPhase.GO -> Brush.linearGradient(listOf(Color(0xF0D52C3B), Color(0xF06D111C)))
        DuelPhase.ROUND_RESULT -> if (state.roundWinner == player) {
            Brush.linearGradient(listOf(Color(0xF0785D2D), Color(0xF0392C19)))
        } else {
            Brush.linearGradient(listOf(Color(0xF020242B), Color(0xF0101217)))
        }
        DuelPhase.FINISHED -> if (isWinner) {
            Brush.linearGradient(listOf(Color(0xF0785D2D), Color(0xF0392C19)))
        } else {
            Brush.linearGradient(listOf(Color(0xF020242B), Color(0xF0101217)))
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(26.dp))
            .background(zoneBrush)
            .then(if (canTap) Modifier.clickable(onClick = onTap) else Modifier)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .then(if (isOppositeSide) Modifier.rotate(180f) else Modifier)
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = player.title,
                        color = PremiumGold,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.2.sp
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = if (state.phase == DuelPhase.READY) "REFLEKS DÜELLOSU" else "TUR ${state.round}",
                        color = PremiumMuted,
                        fontSize = 10.sp,
                        letterSpacing = 1.2.sp
                    )
                }
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = state.scoreFor(player).toString(),
                        color = TextPrimary,
                        fontFamily = FontFamily.Serif,
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Normal
                    )
                    Text(
                        text = " / 10",
                        color = PremiumMuted,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(bottom = 5.dp)
                    )
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = signalTitle(player, state),
                    color = signalColor(player, state),
                    fontSize = 25.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 2.2.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(7.dp))
                Text(
                    text = signalSubtitle(player, state),
                    color = TextPrimary.copy(alpha = 0.78f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                )
            }

            when (state.phase) {
                DuelPhase.READY -> if (!isOppositeSide) {
                    Button(
                        onClick = onStart,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PremiumGold,
                            contentColor = PremiumInk
                        ),
                        shape = RoundedCornerShape(50),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                    ) {
                        Text(
                            text = "DÜELLOYU BAŞLAT",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp
                        )
                    }
                } else {
                    Text(
                        text = "BAŞLATMAK İÇİN ALTTAKİ OYUNCUYU BEKLE",
                        color = PremiumMuted,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.sp,
                        textAlign = TextAlign.Center
                    )
                }

                DuelPhase.FINISHED -> if (!isOppositeSide) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = onRestart,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PremiumGold,
                                contentColor = PremiumInk
                            ),
                            shape = RoundedCornerShape(50),
                            modifier = Modifier.weight(1f).height(48.dp)
                        ) {
                            Text("YENİDEN OYNA", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = onExit,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0x4021262E),
                                contentColor = TextPrimary
                            ),
                            shape = RoundedCornerShape(50),
                            modifier = Modifier.weight(1f).height(48.dp)
                        ) {
                            Text("ANA MENÜ", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    Text(
                        text = "İYİ BİR YARIŞTI",
                        color = PremiumMuted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.5.sp
                    )
                }

                DuelPhase.WAITING,
                DuelPhase.GO,
                DuelPhase.ROUND_RESULT -> Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

private fun signalTitle(player: DuelPlayer, state: TwoPlayerDuelState): String = when (state.phase) {
    DuelPhase.READY -> if (player == DuelPlayer.ONE) "HAZIR MISIN?" else "YERİNİ AL"
    DuelPhase.WAITING -> if (player in state.earlyPlayers) "ERKEN DOKUNUŞ" else "BEKLE"
    DuelPhase.GO -> "DOKUN!"
    DuelPhase.ROUND_RESULT -> when (state.roundWinner) {
        player -> "İLK SEN!"
        null -> "SÜRE DOLDU"
        else -> "RAKİP HIZLI"
    }
    DuelPhase.FINISHED -> if (state.matchWinner == player) "KAZANDIN!" else "YARIŞ BİTTİ"
}

private fun signalSubtitle(player: DuelPlayer, state: TwoPlayerDuelState): String = when (state.phase) {
    DuelPhase.READY -> if (player == DuelPlayer.ONE) {
        "Yeşilken bekle. Kırmızıya döner dönmez kendi tarafına dokun."
    } else {
        "Telefonu iki oyuncunun arasına yerleştir. Ekranın bu yarısı sana göre çevrildi."
    }
    DuelPhase.WAITING -> if (player in state.earlyPlayers) "−1 PUAN  ·  BU TUR BEKLE" else "SİNYALİ BEKLE"
    DuelPhase.GO -> if (player in state.earlyPlayers) "ERKEN DOKUNUŞ CEZASI  −1" else "ŞİMDİ DOKUN"
    DuelPhase.ROUND_RESULT -> when (state.roundWinner) {
        player -> "+1 PUAN"
        null -> "SONRAKİ TUR BAŞLIYOR"
        else -> "${state.roundWinner.title}  +1 PUAN"
    }
    DuelPhase.FINISHED -> if (state.matchWinner == player) "10 PUANA İLK ULAŞAN" else "SON SKOR  ${state.scoreFor(player)} / 10"
}

private fun signalColor(player: DuelPlayer, state: TwoPlayerDuelState): Color = when {
    state.phase == DuelPhase.GO && player !in state.earlyPlayers -> Color(0xFFFFD6D6)
    state.phase == DuelPhase.WAITING && player in state.earlyPlayers -> PremiumGold
    state.phase == DuelPhase.ROUND_RESULT && state.roundWinner == player -> PremiumGold
    state.phase == DuelPhase.FINISHED && state.matchWinner == player -> PremiumGold
    else -> TextPrimary
}
