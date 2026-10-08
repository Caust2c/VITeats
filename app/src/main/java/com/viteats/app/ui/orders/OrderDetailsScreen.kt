package com.viteats.app.ui.orders

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.viteats.app.VITeatsApplication
import com.viteats.app.ui.ViewModelFactory
import com.viteats.app.ui.components.NeobrutalButton
import com.viteats.app.ui.components.NeobrutalCard
import com.viteats.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderDetailsScreen(
    orderId: String,
    onBack: () -> Unit,
    viewModel: OrdersViewModel = viewModel(factory = ViewModelFactory(LocalContext.current.applicationContext as VITeatsApplication))
) {
    val qrState by viewModel.qrState.collectAsState()

    LaunchedEffect(orderId) {
        viewModel.fetchQR(orderId)
    }

    Scaffold(
        containerColor = LavenderBackground,
        topBar = {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .drawBehind {
                        drawLine(
                            color = NeobrutalBlack,
                            start = Offset(0f, size.height),
                            end = Offset(size.width, size.height),
                            strokeWidth = 3f
                        )
                    },
                color = NeobrutalWhite
            ) {
                TopAppBar(
                    title = {
                        Text(
                            "Order QR Code",
                            fontWeight = FontWeight.Black,
                            color = NeobrutalBlack
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = NeobrutalBlack
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = NeobrutalWhite)
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            when (val state = qrState) {
                is OrderQRState.Loading, is OrderQRState.Idle -> CircularProgressIndicator(color = NeobrutalBlack)
                is OrderQRState.Success -> {
                    val bitmap = remember(state.qrData) {
                        try {
                            val pureBase64 = if (state.qrData.contains(",")) {
                                state.qrData.split(",")[1]
                            } else {
                                state.qrData
                            }
                            val decodedString = Base64.decode(pureBase64, Base64.DEFAULT)
                            BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
                        } catch (e: Exception) {
                            null
                        }
                    }

                    if (bitmap != null) {
                        NeobrutalCard(
                            backgroundColor = NeobrutalWhite,
                            borderColor = NeobrutalBlack,
                            borderWidth = 2.5.dp,
                            shadowOffset = 5.dp,
                            cornerRadius = 20.dp
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Order #$orderId",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Black,
                                    color = NeobrutalBlack
                                )

                                Spacer(modifier = Modifier.height(18.dp))

                                Box(
                                    modifier = Modifier
                                        .size(240.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(NeobrutalWhite)
                                        .border(BorderStroke(2.dp, NeobrutalBlack), RoundedCornerShape(14.dp))
                                        .padding(10.dp)
                                ) {
                                    Image(
                                        bitmap = bitmap.asImageBitmap(),
                                        contentDescription = "Order QR Code",
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }

                                Spacer(modifier = Modifier.height(18.dp))

                                Text(
                                    text = "Show this QR at the mess counter",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Black,
                                    color = NeobrutalBlack
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Valid for collection today only.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MutedText
                                )
                            }
                        }
                    } else {
                        ErrorView("Failed to load QR code. It might have expired or been claimed.")
                    }
                }
                is OrderQRState.Error -> {
                    ErrorView(state.message) { viewModel.fetchQR(orderId) }
                }
            }
        }
    }
}

@Composable
fun ErrorView(message: String, onRetry: (() -> Unit)? = null) {
    NeobrutalCard(
        backgroundColor = SoftCoral,
        shadowOffset = 4.dp,
        modifier = Modifier.padding(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = message,
                color = NeobrutalBlack,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
            if (onRetry != null) {
                Spacer(modifier = Modifier.height(12.dp))
                NeobrutalButton(
                    onClick = onRetry,
                    backgroundColor = NeobrutalWhite
                ) {
                    Text("Retry", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

