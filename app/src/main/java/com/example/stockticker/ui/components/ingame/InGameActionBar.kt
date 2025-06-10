package com.example.stockticker.ui.components.ingame

import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun InGameActionBar(
    isMyTurn  : Boolean,
    isActive  : Boolean,
    isInitial : Boolean,
    hasRolled : Boolean,
    canBuyBlock  : Boolean,
    hasStock  : Boolean,
    onRoll    : () -> Unit,
    onBuy     : () -> Unit,
    onSell    : () -> Unit,
    onEnd     : () -> Unit
) {
    // 1. Set up an infinite transition that animates alpha between .3f and 1f
    val infiniteTransition = rememberInfiniteTransition()
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.1f,
        targetValue  = 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = FastOutLinearInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    // 2. Derive a border color only when it's your turn
    val borderColor = if (isMyTurn)
        MaterialTheme.colorScheme.primary.copy(alpha = pulseAlpha)
    else
        Color.Transparent

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            // 3. Apply the pulsing border + rounded shape
            .border(
                width = 2.dp,
                color = borderColor,
                shape = RoundedCornerShape(8.dp)
            )
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(8.dp)
            )
            .padding(8.dp)
    ) {
        // Top row: Roll / Buy
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onRoll,
                enabled = isMyTurn && !hasRolled && isActive,
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp)
            ) {
                Text("🎲 Roll Dice")
            }
            Button(
                onClick = onBuy,
                enabled = isMyTurn && (isInitial || (isActive && hasRolled)) && canBuyBlock,
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp)
            ) {
                Text("🟢 Buy Stock")
            }
        }

        Spacer(Modifier.height(8.dp))

        // Bottom row: End / Sell
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onEnd,
                enabled = isMyTurn && (isInitial || (isActive && hasRolled)),
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp)
            ) {
                Text("🔚 End Turn")
            }
            Button(
                onClick = onSell,
                enabled = isMyTurn && (isInitial || (isActive && hasRolled)) && hasStock,
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp)
            ) {
                Text("🔴 Sell Stock")
            }
        }
    }
}