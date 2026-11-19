package com.example.tracker.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tracker.ui.theme.AppColors
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import kotlin.math.abs

private val moneyFormat = DecimalFormat("#,##0.0#", DecimalFormatSymbols(Locale.US))


fun money(value: Double): String {
    val formatted = moneyFormat.format(abs(value))
    return if (value < 0) "-\$$formatted" else "\$$formatted"
}


@Composable
fun AppTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    height: Dp = 50.dp,
    horizontalPadding: Dp = 14.dp,
    fontSize: TextUnit = 14.sp,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None
) {
    Box(
        modifier = modifier
            .height(height)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .border(1.dp, AppColors.Slate300, RoundedCornerShape(12.dp))
            .padding(horizontal = horizontalPadding),
        contentAlignment = Alignment.CenterStart
    ) {
        if (value.isEmpty()) {
            Text(text = placeholder, color = AppColors.Slate400, fontSize = fontSize)
        }
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            textStyle = TextStyle(color = AppColors.Slate900, fontSize = fontSize),
            cursorBrush = SolidColor(AppColors.Indigo500),
            keyboardOptions = keyboardOptions,
            visualTransformation = visualTransformation,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
