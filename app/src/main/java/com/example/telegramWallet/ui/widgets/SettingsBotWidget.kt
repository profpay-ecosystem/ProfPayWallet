package com.example.telegramWallet.ui.widgets

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.telegramWallet.R
import com.example.telegramWallet.bridge.view_model.settings.SettingsViewModel
import androidx.core.net.toUri

@Composable
fun SettingsBotWidget(
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val context = LocalContext.current

    val sharedPrefs = context.getSharedPreferences(
        ContextCompat.getString(context, R.string.preference_file_key),
        Context.MODE_PRIVATE
    )
    val appUniqueID: String? = sharedPrefs.getString("APP_UNIQUE_ID", null)

    Row(modifier = Modifier
        .clip(RoundedCornerShape(8.dp))
        .clickable {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = "https://t.me/ProfPay_bot?start=$appUniqueID".toUri()
            }
            context.startActivity(intent)
        }
        ,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Привязать Telegram аккаунт",
            fontWeight = FontWeight.SemiBold,
            fontSize = 15.sp,
            modifier = Modifier.padding( top = 4.dp, bottom = 4.dp, end = 2.dp)
        )
        Icon(
            Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = "",
            modifier = Modifier.size(20.dp)
        )
    }
}