package com.example.telegramWallet.data.utils

import android.content.SharedPreferences
import org.mindrot.jbcrypt.BCrypt
import androidx.core.content.edit

fun hashPasswordWithBCrypt(sharedPref: SharedPreferences, password: Int): String {
    // Генерация соли для хэширования
    var salt = sharedPref.getString("salt", "-1")
    if (salt.equals("-1")) {
        sharedPref.edit() { putString("salt", BCrypt.gensalt()) }
        salt = sharedPref.getString("salt", "-1")
    }
    // Хэширование пароля с использованием соли
    return BCrypt.hashpw(password.toString(), salt)
}