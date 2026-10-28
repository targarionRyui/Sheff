package com.example.sheff.ui.navigation

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.sheff.ui.theme.BarGray
import com.example.sheff.ui.theme.InkBlack


@Composable
fun SheffBottomBar(
    currentRoute: String?,
    onDestinationClick: (TopDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .height(66.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(BarGray)
            .padding(horizontal = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TopDestination.items.forEach { destination ->
            val selected = currentRoute == destination.route


            val containerColor by animateColorAsState(
                targetValue = if (selected) InkBlack else Color.Transparent,
                label = "navItemColor"
            )
            val iconColor by animateColorAsState(
                targetValue = if (selected) Color.White else InkBlack,
                label = "navIconColor"
            )

            Row(
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(containerColor)
                    .clickable { onDestinationClick(destination) },
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = destination.icon,
                    contentDescription = destination.label,
                    tint = iconColor,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}
