package com.example.sheff.ui.components

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.sheff.ui.theme.BrownText
import com.example.sheff.ui.theme.CardGray

@Composable
fun CategoryCard(
    name: String,
    imageUrl: String?,
    modifier: Modifier = Modifier
        .width(212.dp)
        .height(150.dp),
    onClick: () -> Unit = {}
) {
    val interactionSource = remember { MutableInteractionSource() }
    val hasImage = !imageUrl.isNullOrBlank()

    Box(
        modifier = modifier
            .pressScale(interactionSource)
            .clip(RoundedCornerShape(24.dp))
            .background(CardGray)
            .clickable(
                interactionSource = interactionSource,
                indication = LocalIndication.current,
                onClick = onClick
            )
    ) {
        if (hasImage) {
            AsyncImage(
                model = imageUrl,
                contentDescription = name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize()
            )
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.verticalGradient(
                            0.45f to Color.Transparent,
                            1f to Color.Black.copy(alpha = 0.55f)
                        )
                    )
            )
        }

        Text(
            text = name,
            style = MaterialTheme.typography.titleLarge,
            color = if (hasImage) Color.White else BrownText,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp)
        )
    }
}
