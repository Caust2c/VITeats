package com.viteats.app.ui.student

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun StudentScreen(viewModel: StudentViewModel) {
    val balanceState by viewModel.balanceState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when (val state = balanceState) {
            is BalanceState.Loading -> CircularProgressIndicator()
            is BalanceState.Success -> {
                val balance = state.balance
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Wallet Balance",
                            style = MaterialTheme.typography.labelLarge
                        )
                        Text(
                            text = "₹${balance.bal}",
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = balance.name,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = balance.regno,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = "Email: ${balance.email}")
                        Text(text = "Card No: ${balance.cardno}")
                        Text(text = "Customer ID: ${balance.custid}")
                    }
                }
            }
            is BalanceState.Error -> {
                Text(text = state.message, color = MaterialTheme.colorScheme.error)
                Button(onClick = { viewModel.fetchBalance() }) {
                    Text("Retry")
                }
            }
        }
    }
}
