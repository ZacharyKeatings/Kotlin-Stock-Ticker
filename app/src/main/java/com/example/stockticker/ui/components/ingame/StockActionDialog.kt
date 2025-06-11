package com.example.stockticker.ui.components.ingame

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.stockticker.viewmodel.GameViewModel
import org.json.JSONArray
import org.json.JSONObject
import kotlin.math.roundToInt

/**
 * A buy/sell modal dialog that only allows quantities in 500-share increments,
 * shows only stocks you can afford at least one block (for buys),
 * and only stocks you own at least one block of (for sells),
 * based on the new portfolio-as-lots JSON format.
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
    localPlayer: JSONObject?          // JSON with {"cash":…, "portfolio": { stock: [ {qty,price},… ] } }
) {
    if (!visible || mode == null) return

    // 1) Extract stocks & player data
    val stocksJson       = game.optJSONObject("stocks") ?: JSONObject()
    val playerCash       = localPlayer?.optDouble("cash") ?: 0.0
    val portfolioJson    = localPlayer?.optJSONObject("portfolio") ?: JSONObject()
    val blockSize        = 500

    // 2) Build list of all stock symbols
    val allStockNames = stocksJson.keys().asSequence().toList().sorted()

    // 3) Filter for buyable: can afford at least one block
    val buyableStocks = allStockNames.filter { stock ->
        val price = stocksJson.optJSONObject(stock)?.optDouble("price") ?: 1.0
        playerCash >= price * blockSize
    }

    // 4) Filter for sellable: own at least one block across all lots
    val sellableStocks = allStockNames.filter { stock ->
        val lotsArray = portfolioJson.optJSONArray(stock) ?: return@filter false
        var totalQty = 0
        for (i in 0 until lotsArray.length()) {
            val lot = lotsArray.optJSONObject(i)
            totalQty += lot?.optInt("qty") ?: 0
        }
        totalQty >= blockSize
    }

    // 5) Choose dropdown options based on mode
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
                // Stock selector
                Text("Select stock:")
                Spacer(Modifier.height(8.dp))
                DropdownMenuBox(
                    items           = dropdownOptions,
                    selected        = selectedStock,
                    onItemSelected  = {
                        setSelectedStock(it)
                        setQuantity(0) // reset quantity whenever stock changes
                    }
                )
                Spacer(Modifier.height(16.dp))

                // When no stock is selected
                if (selectedStock == null) {
                    Text("Select a stock to continue.")
                    return@Column
                }

                // 6) Compute max blocks for buy or sell
                val stockPrice   = stocksJson.optJSONObject(selectedStock)?.optDouble("price") ?: 1.0
                val maxBuyBlocks = (playerCash / stockPrice / blockSize).toInt()
                // Sum up total owned across lots
                val lotsArray    = portfolioJson.optJSONArray(selectedStock) ?: JSONArray()
                var totalOwned   = 0
                for (i in 0 until lotsArray.length()) {
                    totalOwned += lotsArray.optJSONObject(i)?.optInt("qty") ?: 0
                }
                val maxSellBlocks = (totalOwned / blockSize)

                // 7) Show slider & quantity
                Text("Quantity: $quantity")
                Spacer(Modifier.height(8.dp))

                if (mode == "buy") {
                    if (maxBuyBlocks > 0) {
                        val currentBlock = (quantity / blockSize).coerceIn(0, maxBuyBlocks)
                        Slider(
                            value       = currentBlock.toFloat(),
                            onValueChange = { newBlock ->
                                val newQty = (newBlock.roundToInt() * blockSize)
                                    .coerceAtMost(maxBuyBlocks * blockSize)
                                setQuantity(newQty)
                            },
                            valueRange  = 0f..maxBuyBlocks.toFloat(),
                            steps       = (maxBuyBlocks - 1).coerceAtLeast(0),
                            modifier    = Modifier.fillMaxWidth()
                        )
                    } else {
                        Text("Not enough cash for $blockSize shares.")
                    }
                } else {
                    if (maxSellBlocks > 0) {
                        val currentBlock = (quantity / blockSize).coerceIn(0, maxSellBlocks)
                        Slider(
                            value       = currentBlock.toFloat(),
                            onValueChange = { newBlock ->
                                val newQty = (newBlock.roundToInt() * blockSize)
                                    .coerceAtMost(maxSellBlocks * blockSize)
                                setQuantity(newQty)
                            },
                            valueRange  = 0f..maxSellBlocks.toFloat(),
                            steps       = (maxSellBlocks - 1).coerceAtLeast(0),
                            modifier    = Modifier.fillMaxWidth()
                        )
                    } else {
                        Text("You need at least $blockSize shares to sell.")
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
