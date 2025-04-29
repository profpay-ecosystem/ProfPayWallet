package com.example.telegramWallet.ui.features.settings.row_button

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.AbsoluteAlignment
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

@Composable
fun RowSettingsButton(textContent: String, modifier: Modifier, openWidget: Boolean) {
    Row(
        modifier = modifier
            .background(MaterialTheme.colorScheme.primaryContainer)
            .height(55.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically

    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .weight(1.8f)
        ) {
            Text(text = textContent)
        }
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .weight(0.3f)
        ) {
            if (openWidget) {
                Icon(Icons.Filled.KeyboardArrowUp, contentDescription = "Вниз")
            } else {
                Icon(Icons.Filled.KeyboardArrowDown, contentDescription = "Вверх")
            }
        }
    }
}

@Composable
fun RowSettingsButtonHidden(
    textContent: String, modifier: Modifier, funcRight: @Composable () -> Unit
) {
    RowSettingsButtonHiddenWithoutDivider(textContent, modifier, funcRight)
    Divider(
        color = MaterialTheme.colorScheme.primaryContainer,
        modifier = Modifier.padding(horizontal = 14.dp)
    )
}

@Composable
fun RowSettingsButtonHiddenWithoutDivider(
    textContent: String, modifier: Modifier, funcRight: @Composable () -> Unit
) {
    Row(
        modifier = modifier
            .background(MaterialTheme.colorScheme.background)
            .height(55.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically

    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp)
            
        ) {
            Text(text = textContent)
        }
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .weight(0.9f)
                , horizontalAlignment = AbsoluteAlignment.Right
        ) {
            funcRight()
        }
    }
}

@Composable
fun RowButtonInHidden(textContent: String, modifier: Modifier) {
    Row(
        modifier = modifier
            .clip(shape = RoundedCornerShape(25.dp))
            .background(MaterialTheme.colorScheme.primaryContainer)
    ) {
        Row(
            Modifier
                .padding(horizontal = 16.dp, vertical = 6.dp)
        ) {
            Text(
                text = textContent,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}