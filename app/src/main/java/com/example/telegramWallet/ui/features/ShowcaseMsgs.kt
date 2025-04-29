package com.example.telegramWallet.ui.features

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ly.com.tahaben.showcase_layout_compose.model.Arrow
import ly.com.tahaben.showcase_layout_compose.model.Gravity
import ly.com.tahaben.showcase_layout_compose.model.MsgAnimation
import ly.com.tahaben.showcase_layout_compose.model.ShowcaseMsg

class ShowcaseMsgs(colorScheme: ColorScheme) {
    val DeleteAddress: ShowcaseMsg = ShowcaseMsg(
        deleteAddressStr,
        textStyle = TextStyle(color = colorScheme.background),
        roundedCorner = 15.dp,
        gravity = Gravity.Bottom,
        arrow = Arrow(
            color = colorScheme.primary,
            curved = true, hasHead = false
        ),
        enterAnim = MsgAnimation.FadeInOut(),
        exitAnim = MsgAnimation.FadeInOut()
    )

}

private val deleteAddressStr = buildAnnotatedString {
    pushStyle(SpanStyle(fontWeight = FontWeight.Bold, fontSize = 20.sp))
    append("Копирование/удаление адреса.\n")
    pop()
    append("Зажмите ")
    pushStyle(SpanStyle(fontWeight = FontWeight.Bold))
    append("адрес")
    pop()
    append(", чтобы скопировать его или удалить")
}