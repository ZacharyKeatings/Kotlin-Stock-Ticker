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
    mode: String?,
    selectedStock: String?,
    setSelectedStock: (String) -> Unit,
    quantity: Int,
    setQuantity: (Int) -> Unit,
    gameVm: GameViewModel,
    gameId: String,
    localPlayer: JSONObject?
) {
    if (!visible || mode == null) return

    val stocksJson = game.optJSONObject("stocks") ?: return
    val portfolio = localPlayer?.optJSONObject("portfolio") ?: JSONObject()
    val availableCash = localPlayer?.optDouble("cash") ?: 0.0

    // All possible stock names, sorted
    val stockList = stocksJson.keys().asSequence().toList().sorted()

    // If buying: only those the player can afford
    // If selling: only those with owned quantity > 0
    val affordableStocks = stockList.filter { stock ->
        val price = stocksJson.optJSONObject(stock)?.optDouble("price") ?: 1.0
        mode == "sell" || price <= availableCash
    }
    val ownedStocks = stockList.filter { stock ->
        portfolio.optInt(stock) > 0
    }
    val options = if (mode == "buy") affordableStocks else ownedStocks

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            val actionLabel = if (mode == "buy") "Buy" else "Sell"
            Button(
                onClick = {
                    if (selectedStock != null && quantity > 0) {
                        if (mode == "buy") {
                            gameVm.buyStock(gameId, selectedStock, quantity)
                        } else {
                            gameVm.sellStock(gameId, selectedStock, quantity)
                        }
                    }
                    onDismiss()
                },
                enabled = (selectedStock != null && quantity > 0)
            ) {
                Text(if (mode == "buy") "Confirm Purchase" else "Confirm Sale")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        title = {
            Text(
                text = if (mode == "buy") "Buy Stock" else "Sell Stock",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column {
                Text("Select stock:")
                Spacer(Modifier.height(8.dp))
                DropdownMenuBox(
                    items = options,
                    selected = selectedStock,
                    onItemSelected = setSelectedStock
                )
                Spacer(Modifier.height(16.dp))

                val maxQty = if (mode == "buy") {
                    val price = selectedStock
                        ?.let { stocksJson.optJSONObject(it)?.optDouble("price") }
                        ?: 1.0
                    (availableCash / price).toInt()
                } else {
                    selectedStock?.let { portfolio.optInt(it) } ?: 0
                }

                if (maxQty > 0) {
                    Text("Quantity: $quantity")
                    Slider(
                        value = quantity.toFloat(),
                        onValueChange = { setQuantity(it.toInt()) },
                        valueRange = 1f..maxQty.toFloat(),
                        steps = maxQty - 1,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    Text("No eligible shares or cash to perform this action.")
                }
            }
        }
    )
}
