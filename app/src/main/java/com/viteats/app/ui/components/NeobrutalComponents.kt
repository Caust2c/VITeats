package com.viteats.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.viteats.app.ui.theme.NeobrutalBlack
import com.viteats.app.ui.theme.NeobrutalWhite

@Composable
fun NeobrutalCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = NeobrutalWhite,
    borderColor: Color = NeobrutalBlack,
    borderWidth: Dp = 2.5.dp,
    shadowOffset: Dp = 4.dp,
    cornerRadius: Dp = 16.dp,
    shape: Shape = RoundedCornerShape(cornerRadius),
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val currentOffset = if (isPressed && onClick != null) shadowOffset / 2 else shadowOffset

    Box(modifier = modifier) {
        // Flat Black Drop Shadow
        if (shadowOffset > 0.dp) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .offset(x = currentOffset, y = currentOffset)
                    .background(borderColor, shape = shape)
            )
        }

        // Foreground Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape)
                .background(backgroundColor)
                .border(BorderStroke(borderWidth, borderColor), shape)
                .then(
                    if (onClick != null) {
                        Modifier.clickable(
                            interactionSource = interactionSource,
                            indication = null,
                            onClick = onClick
                        )
                    } else Modifier
                )
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                content = content
            )
        }
    }
}

@Composable
fun NeobrutalButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = NeobrutalWhite,
    contentColor: Color = NeobrutalBlack,
    borderColor: Color = NeobrutalBlack,
    borderWidth: Dp = 2.dp,
    shadowOffset: Dp = 3.dp,
    cornerRadius: Dp = 12.dp,
    shape: Shape = RoundedCornerShape(cornerRadius),
    enabled: Boolean = true,
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
    content: @Composable RowScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val currentOffset = if (isPressed && enabled) 1.dp else shadowOffset

    Box(modifier = modifier) {
        // Shadow
        if (enabled && shadowOffset > 0.dp) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .offset(x = currentOffset, y = currentOffset)
                    .background(borderColor, shape = shape)
            )
        }

        // Button Surface
        Surface(
            modifier = Modifier
                .clip(shape)
                .border(BorderStroke(borderWidth, if (enabled) borderColor else borderColor.copy(alpha = 0.4f)), shape)
                .clickable(
                    enabled = enabled,
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onClick
                ),
            color = if (enabled) backgroundColor else backgroundColor.copy(alpha = 0.5f),
            contentColor = contentColor,
            shape = shape
        ) {
            Row(
                modifier = Modifier.padding(contentPadding),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                content = content
            )
        }
    }
}

@Composable
fun NeobrutalPill(
    text: String,
    modifier: Modifier = Modifier,
    backgroundColor: Color = NeobrutalWhite,
    textColor: Color = NeobrutalBlack,
    borderColor: Color = NeobrutalBlack,
    isSelected: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    val shape = RoundedCornerShape(50)
    val shadowOffset = if (isSelected) 3.dp else 2.dp

    Box(modifier = modifier) {
        if (shadowOffset > 0.dp) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .offset(x = shadowOffset, y = shadowOffset)
                    .background(borderColor, shape = shape)
            )
        }

        Box(
            modifier = Modifier
                .clip(shape)
                .background(backgroundColor)
                .border(BorderStroke(2.dp, borderColor), shape)
                .then(
                    if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier
                )
                .padding(horizontal = 14.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.ExtraBold,
                color = textColor
            )
        }
    }
}
