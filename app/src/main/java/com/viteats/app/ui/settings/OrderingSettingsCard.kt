package com.viteats.app.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.viteats.app.VITeatsApplication
import com.viteats.app.data.AutoOrderScheduler
import com.viteats.app.ui.components.NeobrutalCard
import com.viteats.app.ui.theme.*

@Composable
fun OrderingSettingsCard() {
    val context = LocalContext.current
    val app = context.applicationContext as VITeatsApplication
    val config by app.autoOrderSettings.config.collectAsState()
    val favourites by app.favouritesManager.favouriteItemIds.collectAsState()
    LaunchedEffect(Unit) { app.autoOrderSettings.refresh() }
    var enabled by remember(config.generation) { mutableStateOf(config.enabled) }
    var partial by remember(config.generation) { mutableStateOf(config.allowPartial) }
    var time by remember(config.generation) { mutableStateOf("%02d:%02d".format(config.hour, config.minute)) }
    var limit by remember(config.generation) { mutableStateOf(if (config.hardLimit > 0) config.hardLimit.toString() else "") }
    var message by remember { mutableStateOf<String?>(null) }
    val parsedTime = time.split(':').map { it.toIntOrNull() }
    val hour = parsedTime.getOrNull(0)
    val minute = parsedTime.getOrNull(1)
    val amount = if (limit.isBlank()) 0.0 else limit.toDoubleOrNull()
    val valid = parsedTime.size == 2 && hour != null && hour in 0..23 && minute != null && minute in 0..59 &&
        amount != null && amount.isFinite() && amount >= 0 && (!enabled || favourites.isNotEmpty())

    NeobrutalCard(backgroundColor = if (LocalDarkTheme.current) DarkCardBg else NeobrutalWhite) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Ordering preferences", fontWeight = FontWeight.Black)
            OutlinedTextField(value = limit, onValueChange = { limit = it; message = null },
                label = { Text("Hard limit per order (₹)") }, supportingText = { Text("Blank or 0: no limit. Manual orders ask before exceeding it; automatic orders skip.") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true,
                modifier = Modifier.fillMaxWidth())
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Auto-order favourites", modifier = Modifier.weight(1f))
                Switch(checked = enabled, onCheckedChange = { enabled = it; message = null })
            }
            Text("Orders one of each of your ${favourites.size} favourites every day using your saved PIN and wallet.")
            OutlinedTextField(value = time, onValueChange = { time = it; message = null },
                label = { Text("Daily time in IST (HH:mm)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            Text("Choose an open meal time. Android may delay background work; runs over 15 minutes late are skipped.", style = MaterialTheme.typography.bodySmall)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Order available favourites only", modifier = Modifier.weight(1f))
                Switch(checked = partial, onCheckedChange = { partial = it; message = null })
            }
            Text(if (partial) "Partial: unavailable favourites are skipped." else "Complete: order only when every favourite is available.",
                style = MaterialTheme.typography.bodySmall)
            Button(enabled = valid, onClick = {
                val updated = config.copy(enabled = enabled, hour = hour!!, minute = minute!!,
                    allowPartial = partial, hardLimit = amount!!)
                app.autoOrderSettings.save(updated)
                val owner = app.sessionManager.userIdentifier
                if (owner != null) AutoOrderScheduler.update(context, owner, app.autoOrderSettings.read())
                message = if (enabled) "Saved. Daily wallet payments are enabled." else "Saved. Automatic ordering is off."
            }) { Text("Save ordering settings") }
            if (enabled && favourites.isEmpty()) Text("Add favourites before enabling automatic ordering.")
            message?.let { Text(it) }
            Text("Last run: ${config.lastResult}", style = MaterialTheme.typography.bodySmall)
        }
    }
}
