package com.erkan.reflex.ui

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.erkan.reflex.model.GameStatus
import com.erkan.reflex.R
import com.erkan.reflex.ui.components.BalloonView
import com.erkan.reflex.ui.components.ExplosionEffect
import com.erkan.reflex.ui.components.GameOverDialog
import com.erkan.reflex.ui.components.PremiumBalloonArtwork
import com.erkan.reflex.ui.components.ScoreBoard
import com.erkan.reflex.ui.components.GameMetricsStrip
import com.erkan.reflex.ui.performReflexErrorHaptic
import com.erkan.reflex.ui.performReflexHitHaptic
import com.erkan.reflex.ui.theme.PremiumGold
import com.erkan.reflex.ui.theme.PremiumInk
import com.erkan.reflex.ui.theme.PremiumMuted
import com.erkan.reflex.ui.theme.TextPrimary
import com.erkan.reflex.viewmodel.GameViewModel
import com.erkan.reflex.viewmodel.TwoPlayerDuelViewModel

@Composable
fun GameScreen(
    viewModel: GameViewModel,
    duelViewModel: TwoPlayerDuelViewModel,
    modifier: Modifier = Modifier
) {
    var showDuel by remember { mutableStateOf(false) }

    if (showDuel) {
        TwoPlayerDuelScreen(
            viewModel = duelViewModel,
            onExit = {
                duelViewModel.reset()
                showDuel = false
            },
            modifier = modifier
        )
        return
    }

    val gameState by viewModel.gameState.collectAsState()
    val balloonDurationMs = gameState.lastBalloonDurationMs
    val backgroundVariant = when {
        balloonDurationMs <= 333L -> 3
        balloonDurationMs <= 667L -> 2
        else -> 1
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF11141A), Color(0xFF090B0F), Color(0xFF11110F))
                )
            )
    ) {
        val backgroundRes = when (backgroundVariant) {
            1 -> R.drawable.premium_arena_1
            2 -> R.drawable.premium_arena_2
            else -> R.drawable.premium_arena_3
        }
        Image(
            painter = painterResource(backgroundRes),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            alpha = 0.62f,
            modifier = Modifier.fillMaxSize()
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(Color(0x201F252B), Color.Transparent)
                    )
                )
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            when (gameState.status) {
                GameStatus.NOT_STARTED -> StartScreen(
                    highScore = gameState.highScore,
                    onStart = { viewModel.startGame() },
                    onOpenDuel = {
                        duelViewModel.reset()
                        showDuel = true
                    }
                )

                GameStatus.WAITING_BALLOON,
                GameStatus.BALLOON_ACTIVE,
                GameStatus.GAME_OVER -> {
                    ActiveGamePlay(
                        viewModel = viewModel,
                        modifier = Modifier.fillMaxSize()
                    )

                    if (gameState.status == GameStatus.GAME_OVER) {
                        GameOverDialog(
                            gameState = gameState,
                            onRestart = { viewModel.startGame() }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ActiveGamePlay(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    val gameState by viewModel.gameState.collectAsState()
    val densityScale = LocalDensity.current.density
    val hapticView = LocalView.current

    Column(modifier = modifier) {
        ScoreBoard(gameState = gameState)

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .pointerInput(viewModel, densityScale) {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent()
                            event.changes.forEach { change ->
                                // Her parmağın yalnızca ilk basışını işle; balon listesi
                                // değiştiğinde bu pointerInput döngüsü yeniden başlamaz.
                                if (!change.previousPressed && change.pressed) {
                                    val currentState = viewModel.gameState.value
                                    if (currentState.status != GameStatus.WAITING_BALLOON &&
                                        currentState.status != GameStatus.BALLOON_ACTIVE
                                    ) {
                                        return@forEach
                                    }

                                    val tap = change.position
                                    val playAreaWidthPx = size.width.toFloat()
                                    val playAreaHeightPx = size.height.toFloat()
                                    val tappedBalloon = currentState.currentBalloons.firstOrNull { balloon ->
                                        val balloonSizePx = balloon.sizeDp * densityScale
                                        val centerX = playAreaWidthPx * balloon.xRatio
                                        val centerY = playAreaHeightPx * balloon.yRatio
                                        tap.x >= centerX - balloonSizePx / 2f &&
                                            tap.x <= centerX + balloonSizePx / 2f &&
                                            tap.y >= centerY - balloonSizePx / 2f &&
                                            tap.y <= centerY + balloonSizePx / 2f
                                    }

                                    if (tappedBalloon != null) {
                                        if (viewModel.onBalloonTapped(tappedBalloon.id, tap.x, tap.y)) {
                                            hapticView.performReflexHitHaptic()
                                        }
                                    } else if (viewModel.onInvalidTap()) {
                                        hapticView.performReflexErrorHaptic()
                                    }
                                }
                            }
                        }
                    }
                }
        ) {
            val widthPx = constraints.maxWidth.toFloat()
            val heightPx = constraints.maxHeight.toFloat()

            gameState.currentBalloons.forEach { balloon ->
                BalloonView(
                    balloon = balloon,
                    containerWidth = widthPx,
                    containerHeight = heightPx
                )
            }

            ExplosionEffect(
                particles = viewModel.particles,
                floatingTexts = viewModel.floatingTexts
            )
        }

        GameMetricsStrip(
            gameState = gameState,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        )
    }
}

@Composable
fun StartScreen(
    highScore: Int,
    onStart: () -> Unit,
    onOpenDuel: () -> Unit
) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    val appVersion = remember(context) { getInstalledVersionName(context) }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 28.dp, vertical = 14.dp)
    ) {
        val heroHeight = (maxHeight * 0.37f).coerceIn(220.dp, 300.dp)
        val heroWidth = (maxWidth * 0.76f).coerceIn(230.dp, 290.dp)

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "REFLEX",
                    color = TextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 7.sp
                )
                Spacer(modifier = Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .width(48.dp)
                        .height(1.dp)
                        .clip(CircleShape)
                        .background(PremiumGold.copy(alpha = 0.72f))
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(heroHeight),
                    contentAlignment = Alignment.Center
                ) {
                    PremiumBalloonArtwork(width = heroWidth, height = heroHeight)
                }

                Text(
                    text = "Refleksini test et.",
                    color = TextPrimary,
                    fontFamily = FontFamily.Serif,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Normal,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(5.dp))
                Text(
                    text = "Balonu görür görmez dokun.",
                    color = PremiumMuted,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(22.dp))
                PersonalBestCard(highScore = highScore)
                Spacer(modifier = Modifier.height(22.dp))

                PrimaryGameAction(onClick = onStart)
                Spacer(modifier = Modifier.height(10.dp))
                DuelGameAction(onClick = onOpenDuel)
                Spacer(modifier = Modifier.height(14.dp))
            }

            HomeFooter(
                versionName = appVersion,
                onOpenPrivacy = { uriHandler.openUri(PRIVACY_POLICY_URL) }
            )
        }
    }
}

@Composable
private fun PrimaryGameAction(onClick: () -> Unit) {
    val shape = RoundedCornerShape(22.dp)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(76.dp)
            .clip(shape)
            .background(
                Brush.horizontalGradient(
                    listOf(Color(0xFFFFE8AE), PremiumGold, Color(0xFFD5B16A))
                )
            )
            .border(1.dp, Color.White.copy(alpha = 0.42f), shape)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(start = 20.dp, end = 15.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(
                text = "TEK KİŞİLİK · REFLEKS TESTİ",
                color = PremiumInk.copy(alpha = 0.68f),
                fontSize = 9.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.3.sp
            )
            Text(
                text = "OYUNA BAŞLA",
                color = PremiumInk,
                fontSize = 15.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.4.sp
            )
        }
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(PremiumInk)
                .border(1.dp, Color.White.copy(alpha = 0.16f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.PlayArrow,
                contentDescription = null,
                tint = PremiumGold,
                modifier = Modifier.size(23.dp)
            )
        }
    }
}

@Composable
private fun DuelGameAction(onClick: () -> Unit) {
    val shape = RoundedCornerShape(22.dp)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .clip(shape)
            .background(
                Brush.horizontalGradient(listOf(Color(0xFF20242D), Color(0xFF12161E)))
            )
            .border(1.dp, PremiumGold.copy(alpha = 0.35f), shape)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(start = 20.dp, end = 15.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(32.dp)
                    .clip(CircleShape)
                    .background(PremiumGold.copy(alpha = 0.82f))
            )
            Spacer(modifier = Modifier.width(13.dp))
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    text = "AYNI TELEFONDA · 2 OYUNCU",
                    color = PremiumMuted,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 1.15.sp
                )
                Text(
                    text = "REFLEKS DÜELLOSU",
                    color = TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.05.sp
                )
            }
        }
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(PremiumGold.copy(alpha = 0.08f))
                .border(1.dp, PremiumGold.copy(alpha = 0.52f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "2P",
                color = PremiumGold,
                fontFamily = FontFamily.Serif,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.7.sp
            )
        }
    }
}

@Composable
private fun HomeFooter(versionName: String, onOpenPrivacy: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(PremiumGold.copy(alpha = 0.13f))
        )
        TextButton(onClick = onOpenPrivacy) {
            Text(
                text = "GİZLİLİK POLİTİKASI  ↗",
                color = PremiumMuted,
                fontSize = 9.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 1.35.sp
            )
        }
        Text(
            text = "SÜRÜM $versionName",
            color = PremiumMuted.copy(alpha = 0.58f),
            fontSize = 8.sp,
            fontWeight = FontWeight.Light,
            letterSpacing = 1.6.sp
        )
    }
}

@Suppress("DEPRECATION")
private fun getInstalledVersionName(context: Context): String =
    context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "1.1"

private const val PRIVACY_POLICY_URL =
    "https://erkanalcin-ai.github.io/Reflex/privacy-policy.html"

@Composable
private fun PersonalBestCard(highScore: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0x251F2329))
            .padding(horizontal = 22.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = "KİŞİSEL REKOR",
                color = PremiumMuted,
                fontSize = 9.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 2.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "$highScore",
                color = TextPrimary,
                fontFamily = FontFamily.Serif,
                fontSize = 29.sp,
                fontWeight = FontWeight.Normal
            )
        }
        Box(
            modifier = Modifier
                .width(1.dp)
                .height(50.dp)
                .background(Color.White.copy(alpha = 0.16f))
        )
        Icon(
            imageVector = Icons.Filled.Star,
            contentDescription = "Kişisel rekor kupası",
            tint = PremiumGold.copy(alpha = 0.9f),
            modifier = Modifier.size(34.dp)
        )
    }
}
