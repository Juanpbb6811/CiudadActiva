package com.ciudadactiva.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ciudadactiva.app.ui.theme.BrandTealDark

@Composable
fun OtpInputField(
    otpValue: String,
    onOtpChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    otpLength: Int = 6,
    isError: Boolean = false
) {
    // Campo oculto pero activo que maneja la entrada y el pegado de texto
    BasicTextField(
        value = otpValue,
        onValueChange = { newValue ->
            val filtered = newValue.filter { it.isDigit() }.take(otpLength)
            onOtpChange(filtered)
        },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
        decorationBox = {
            Row(
                modifier = modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (i in 0 until otpLength) {
                    val char = otpValue.getOrNull(i)?.toString() ?: ""
                    val isCurrent = i == otpValue.length
                    val isFilled = i < otpValue.length

                    val borderColor = when {
                        isError -> Color(0xFFD32F2F)
                        isCurrent -> BrandTealDark
                        isFilled -> Color(0xFF00695C)
                        else -> Color(0xFFCFD8DC)
                    }

                    val bgColor = when {
                        isError -> Color(0xFFFFEBEE)
                        isCurrent -> Color(0xFFE0F2F1)
                        isFilled -> Color(0xFFF1F8F6)
                        else -> Color(0xFFFAFAFA)
                    }

                    Box(
                        modifier = Modifier
                            .width(44.dp)
                            .height(54.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(bgColor)
                            .border(
                                width = if (isCurrent || isFilled) 1.8.dp else 1.dp,
                                color = borderColor,
                                shape = RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = char,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isError) Color(0xFFD32F2F) else Color(0xFF1E2D2B),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    )
}
