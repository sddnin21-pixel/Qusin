package com.wangzan101.mika.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import com.wangzan101.mika.R

/** Logo Qusin dùng chung trong giao diện. */
@Composable
fun QusinLogo(size: Dp, modifier: Modifier = Modifier, corner: Dp = size * 0.28f) {
    Image(
        painter = painterResource(R.drawable.qusin_logo),
        contentDescription = null,
        modifier = modifier.size(size).clip(RoundedCornerShape(corner)),
        contentScale = ContentScale.Crop,
    )
}
