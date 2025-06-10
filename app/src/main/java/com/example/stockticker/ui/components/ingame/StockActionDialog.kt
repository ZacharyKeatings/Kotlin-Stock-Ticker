// StockActionDialog.kt
package com.example.stockticker.ui.components.ingame

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.stockticker.viewmodel.GameViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import kotlin.math.roundToInt

/**
 * A buy/sell modal dialog.
 *
 * @param game           Full game JSON (so we can read stock prices and player portfolio).
 * @param visible        Controls whether the dialog is shown.
 * @param onDismiss      Called when the user cancels or confirms.
 * @param mode           "buy" or "sell".
 * @param selectedStock  The currently‐selected stock (null initially).
 * @param setSelectedStock  Callback to update which stock is selected.
 * @param quantity       Currently selected quantity (0 initially).
 * @param setQuantity    Callback to update the quantity.
 * @param gameVm         Shared GameViewModel (so we can call buyStock/sellStock).
 * @param gameId         The ID of the game.
 * @param localPlayer    The JSON object representing the local player (so we know cash & holdings).
 */
@Composable
fun StockActionDialog(
    game: JSONObject,
    visible: Boolean,
    onDismiss: () -> Unit,
    mode: String?,                    // "buy" or "sell"
    selectedStock: String?,
    setSelectedStock: (String) -> Unit,
    quantity: Int,
    setQuantity: (Int) -> Unit,
    gameVm: GameViewModel,
    gameId: String,
    localPlayer: JSONObject?          // the local player's JSON (cash & portfolio)
) {
    if (!visible || mode == null) return

    // Read stocks, portfolio, cash
    val stocksJson = game.optJSONObject("stocks") ?: JSONObject()
    val playerPortfolio = localPlayer?.optJSONObject("portfolio") ?: JSONObject()
    val playerCash = localPlayer?.optDouble("cash") ?: 0.0

    // Build the stock dropdown options
    val allStockNames = stocksJson.keys().asSequence().toList().sorted()

    val buyableStocks = allStockNames.filter { stock ->
        val pricePerShare = stocksJson.optJSONObject(stock)
            ?.optDouble("price") ?: 1.0
        playerCash >= pricePerShare * 500
    }

    val sellableStocks = allStockNames.filter { stock ->
        playerPortfolio.optInt(stock) > 0
    }
    val dropdownOptions = if (mode == "buy") buyableStocks else sellableStocks

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (mode == "buy") "Buy Stock" else "Sell Stock",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("Select stock:")
                Spacer(Modifier.height(8.dp))
                DropdownMenuBox(
                    items = dropdownOptions,
                    selected = selectedStock,
                    onItemSelected = setSelectedStock
                )
                Spacer(Modifier.height(16.dp))

                if (selectedStock == null) {
                    Text("Select a stock to continue.")
                } else {
                    val blockSize = 500
                    val stockPrice = stocksJson.optJSONObject(selectedStock)
                        ?.optDouble("price") ?: 1.0
                    val maxBuyBlocks = ((playerCash / stockPrice) / blockSize).toInt()
                    val maxSellBlocks = (playerPortfolio.optInt(selectedStock) / blockSize)

                    Text("Quantity: $quantity")
                    Spacer(Modifier.height(8.dp))

                    if (mode == "buy") {
                        if (maxBuyBlocks > 0) {
                            val currentBuyBlock = (quantity / blockSize).coerceIn(0, maxBuyBlocks)
                            Slider(
                                value = currentBuyBlock.toFloat(),
                                onValueChange = { newBlock ->
                                    val newQty = (newBlock.roundToInt() * blockSize)
                                        .coerceAtMost(maxBuyBlocks * blockSize)
                                    setQuantity(newQty)
                                },
                                valueRange = 0f..maxBuyBlocks.toFloat(),
                                steps = (maxBuyBlocks - 1).coerceAtLeast(0),
                                modifier = Modifier.fillMaxWidth()
                            )
                        } else {
                            Text("Not enough cash for $blockSize shares.")
                        }
                    } else { // sell
                        if (maxSellBlocks > 0) {
                            val currentSellBlock = (quantity / blockSize).coerceIn(0, maxSellBlocks)
                            Slider(
                                value = currentSellBlock.toFloat(),
                                onValueChange = { newBlock ->
                                    val newQty = (newBlock.roundToInt() * blockSize)
                                        .coerceAtMost(maxSellBlocks * blockSize)
                                    setQuantity(newQty)
                                },
                                valueRange = 0f..maxSellBlocks.toFloat(),
                                steps = (maxSellBlocks - 1).coerceAtLeast(0),
                                modifier = Modifier.fillMaxWidth()
                            )
                        } else {
                            Text("You need at least $blockSize shares to sell.")
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    selectedStock?.let { stock ->
                        if (quantity > 0) {
                            if (mode == "buy") {
                                gameVm.buyStock(gameId, stock, quantity)
                            } else {
                                gameVm.sellStock(gameId, stock, quantity)
                            }
                        }
                    }
                    onDismiss()
                },
                enabled = selectedStock != null && quantity > 0
            ) {
                Text(if (mode == "buy") "Confirm Purchase" else "Confirm Sale")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
