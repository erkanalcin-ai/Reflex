package com.erkan.reflex.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.erkan.reflex.model.GameState
import com.erkan.reflex.ui.theme.PremiumGold
import com.erkan.reflex.ui.theme.PremiumInk
import com.erkan.reflex.ui.theme.PremiumMuted
import com.erkan.reflex.ui.theme.PremiumPanel
import com.erkan.reflex.ui.theme.TextPrimary

@Composable
fun GameOverDialog(
    gameState: GameState,
    onRestart: () -> Unit
) {
    Dialog(
        onDismissRequest = { /* Modal dışına tıklayınca kapanmasın */ },
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    ) {
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = PremiumInk),
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 2.dp,
                    brush = Brush.linearGradient(
                        listOf(PremiumGold.copy(alpha = 0.9f), Color.White.copy(alpha = 0.22f), PremiumGold.copy(alpha = 0.45f))
                    ),
                    shape = RoundedCornerShape(28.dp)
                )
                .padding(2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val isVictory = gameState.isCompleted

                Text(
                    text = if (isVictory) "TEBRİKLER!" else "OYUN BİTTİ",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    color = if (isVictory) PremiumGold else TextPrimary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = if (isVictory) "100 Balonu Başarıyla Tamamladın!" else "3 Canını da Kaybettin!",
                    fontSize = 14.sp,
                    color = PremiumMuted,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Büyük Skor Kartı
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(PremiumPanel)
                        .padding(vertical = 18.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "TOPLAM SKOR",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = PremiumMuted
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${gameState.score}",
                            fontSize = 44.sp,
                            fontWeight = FontWeight.Black,
                            color = PremiumGold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // İstatistikler (En İyi Refleks, En Yüksek Skor)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // En İyi Refleks Süresi
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(PremiumPanel)
                            .padding(12.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                            Text(text = "⚡ En İyi Refleks", fontSize = 11.sp, color = PremiumMuted)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = gameState.bestReactionTimeMs?.let { "${it} ms" } ?: "--",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }
                    }

                    // En Yüksek Skor
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(PremiumPanel)
                            .padding(12.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Filled.Star,
                                    contentDescription = null,
                                    tint = PremiumGold,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = "Rekor", fontSize = 11.sp, color = PremiumMuted)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${gameState.highScore}",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = PremiumGold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Tekrar Oyna Butonu
                Button(
                    onClick = onRestart,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PremiumGold
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Refresh,
                            contentDescription = "Tekrar Oyna",
                            tint = PremiumInk
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "YENİDEN BAŞLA",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = PremiumInk
                        )
                    }
                }
            }
        }
    }
}
