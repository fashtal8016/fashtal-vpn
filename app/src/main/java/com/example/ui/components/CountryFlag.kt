package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun CountryFlag(
    countryCode: String,
    modifier: Modifier = Modifier,
    width: Dp = 32.dp,
    height: Dp = 22.dp
) {
    val shape = RoundedCornerShape(4.dp)
    Box(
        modifier = modifier
            .size(width = width, height = height)
            .clip(shape)
            .border(0.5.dp, Color(0x33000000), shape)
    ) {
        if (countryCode.equals("US", ignoreCase = true)) {
            // US Flag (Stripes & Canton)
            Box(modifier = Modifier.fillMaxSize()) {
                Column(modifier = Modifier.fillMaxSize()) {
                    val red = Color(0xFFB22234)
                    val white = Color(0xFFFFFFFF)
                    for (i in 0 until 7) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .background(if (i % 2 == 0) red else white)
                        )
                    }
                }
                // Blue Canton
                Box(
                    modifier = Modifier
                        .fillMaxHeight(0.55f)
                        .fillMaxWidth(0.45f)
                        .background(Color(0xFF3C3B6E))
                )
            }
        } else {
            // Germany Flag (Black, Red, Gold)
            Column(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .background(Color(0xFF000000))
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .background(Color(0xFFFF0000))
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .background(Color(0xFFFFCC00))
                )
            }
        }
    }
}
