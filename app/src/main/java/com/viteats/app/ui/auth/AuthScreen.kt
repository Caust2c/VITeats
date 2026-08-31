package com.viteats.app.ui.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.viteats.app.VITeatsApplication
import com.viteats.app.ui.ViewModelFactory
import com.viteats.app.ui.components.NeobrutalButton
import com.viteats.app.ui.components.NeobrutalCard
import com.viteats.app.ui.components.NeobrutalPill
import com.viteats.app.ui.theme.*

@Composable
fun AuthScreen(
    onLoginSuccess: () -> Unit,
    viewModel: AuthViewModel = viewModel(factory = ViewModelFactory(LocalContext.current.applicationContext as VITeatsApplication))
) {
    val authState by viewModel.authState.collectAsState()
    var appNumber by remember { mutableStateOf("") }
    var pin by remember { mutableStateOf("") }
    val scrollState = rememberScrollState()

    LaunchedEffect(authState) {
        if (authState is AuthState.Success) {
            onLoginSuccess()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LavenderBackground)
            .padding(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // App Branding Hero
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(PastelYellow)
                    .border(BorderStroke(2.5.dp, NeobrutalBlack), RoundedCornerShape(18.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Fastfood,
                    contentDescription = "VITeats Logo",
                    tint = NeobrutalBlack,
                    modifier = Modifier.size(40.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "VITeats",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Black,
                color = NeobrutalBlack,
                letterSpacing = (-1).sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            NeobrutalPill(
                text = "Campus Dining & Wallet",
                backgroundColor = SoftCyan,
                textColor = NeobrutalBlack,
                isSelected = false
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Credentials Card
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
                        text = "Student Login",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = NeobrutalBlack
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Sign in with your VIT application credentials",
                        style = MaterialTheme.typography.bodySmall,
                        color = MutedText
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Application Number Input
                    OutlinedTextField(
                        value = appNumber,
                        onValueChange = { appNumber = it },
                        label = { Text("Application Number / Reg No", fontWeight = FontWeight.Bold) },
                        placeholder = { 
                            Text(
                                "e.g. 21BCE1234", 
                                color = Color(0xFF64748B),
                                fontWeight = FontWeight.Medium
                            ) 
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Person, contentDescription = null, tint = NeobrutalBlack)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                        enabled = authState !is AuthState.Loading,
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = NeobrutalBlack,
                            unfocusedTextColor = NeobrutalBlack,
                            focusedBorderColor = NeobrutalBlack,
                            unfocusedBorderColor = NeobrutalBlack.copy(alpha = 0.6f),
                            focusedLabelColor = NeobrutalBlack,
                            unfocusedLabelColor = Color(0xFF334155),
                            focusedPlaceholderColor = Color(0xFF64748B),
                            unfocusedPlaceholderColor = Color(0xFF64748B),
                            focusedContainerColor = LavenderCard,
                            unfocusedContainerColor = LavenderCard,
                            cursorColor = NeobrutalBlack
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // PIN / OTP Input
                    OutlinedTextField(
                        value = pin,
                        onValueChange = { pin = it },
                        label = { Text("PIN", fontWeight = FontWeight.Bold) },
                        placeholder = { 
                            Text(
                                "Enter PIN", 
                                color = Color(0xFF64748B),
                                fontWeight = FontWeight.Medium
                            ) 
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Key, contentDescription = null, tint = NeobrutalBlack)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        enabled = authState !is AuthState.Loading,
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = NeobrutalBlack,
                            unfocusedTextColor = NeobrutalBlack,
                            focusedBorderColor = NeobrutalBlack,
                            unfocusedBorderColor = NeobrutalBlack.copy(alpha = 0.6f),
                            focusedLabelColor = NeobrutalBlack,
                            unfocusedLabelColor = Color(0xFF334155),
                            focusedPlaceholderColor = Color(0xFF64748B),
                            unfocusedPlaceholderColor = Color(0xFF64748B),
                            focusedContainerColor = LavenderCard,
                            unfocusedContainerColor = LavenderCard,
                            cursorColor = NeobrutalBlack
                        )
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Error Alert Banner
                    if (authState is AuthState.Error) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(SoftCoral)
                                .border(BorderStroke(1.5.dp, NeobrutalBlack), RoundedCornerShape(10.dp))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = (authState as AuthState.Error).message,
                                color = NeobrutalBlack,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Sign In Button
                    NeobrutalButton(
                        onClick = { viewModel.login(appNumber.trim(), pin.trim()) },
                        backgroundColor = MintGreen,
                        contentColor = NeobrutalBlack,
                        borderColor = NeobrutalBlack,
                        borderWidth = 2.dp,
                        shadowOffset = 3.dp,
                        cornerRadius = 14.dp,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = authState !is AuthState.Loading && appNumber.isNotBlank() && pin.isNotBlank(),
                        contentPadding = PaddingValues(vertical = 14.dp)
                    ) {
                        if (authState is AuthState.Loading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = NeobrutalBlack,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Authenticating...",
                                fontWeight = FontWeight.Black,
                                style = MaterialTheme.typography.titleMedium
                            )
                        } else {
                            Text(
                                text = "Enter Mess Portal",
                                fontWeight = FontWeight.Black,
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                }
            }
        }
    }
}
