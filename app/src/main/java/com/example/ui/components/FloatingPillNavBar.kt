package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Calculate
import androidx.compose.material.icons.outlined.DirectionsBoat
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.CardSurface
import com.example.ui.theme.SubtleBorder
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WalletIndigo

enum class HomeTab(
    val tag: String,
    val titleRes: Int,
    val activeIcon: ImageVector,
    val inactiveIcon: ImageVector
) {
    BERANDA(
        tag = "nav_beranda",
        titleRes = R.string.nav_home,
        activeIcon = Icons.Filled.Home,
        inactiveIcon = Icons.Outlined.Home
    ),
    CNF_CALCULATION(
        tag = "nav_cnf_calculation",
        titleRes = R.string.nav_cnf_calc,
        activeIcon = Icons.Outlined.Calculate,
        inactiveIcon = Icons.Outlined.Calculate
    ),
    CNF_PRICE(
        tag = "nav_cnf_price",
        titleRes = R.string.nav_cnf_price,
        activeIcon = Icons.Outlined.DirectionsBoat,
        inactiveIcon = Icons.Outlined.DirectionsBoat
    ),
    TRANSAKSI(
        tag = "nav_transaksi",
        titleRes = R.string.nav_transactions,
        activeIcon = Icons.Outlined.Folder,
        inactiveIcon = Icons.Outlined.Folder
    ),
    LAPORAN(
        tag = "nav_laporan",
        titleRes = R.string.nav_reports,
        activeIcon = Icons.Outlined.BarChart,
        inactiveIcon = Icons.Outlined.BarChart
    )
}

@Composable
fun FloatingPillNavBar(
    selectedTab: HomeTab,
    onTabSelected: (HomeTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 14.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .widthIn(max = 600.dp)
                .fillMaxWidth(),
            shape = RoundedCornerShape(32.dp),
            color = CardSurface,
            shadowElevation = 10.dp,
            tonalElevation = 2.dp,
            border = BorderStroke(1.dp, SubtleBorder)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                HomeTab.entries.forEach { tab ->
                    val isSelected = tab == selectedTab
                    val label = stringResource(tab.titleRes)
                    val tint = if (isSelected) WalletIndigo else TextSecondary
                    val pillColor = if (isSelected) WalletIndigo.copy(alpha = 0.11f) else Color.Transparent

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(24.dp))
                            .background(pillColor)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple(bounded = true),
                                onClick = { onTabSelected(tab) }
                            )
                            .padding(vertical = 8.dp, horizontal = 2.dp)
                            .testTag(tab.tag),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = if (isSelected) tab.activeIcon else tab.inactiveIcon,
                            contentDescription = label,
                            tint = tint,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            ),
                            color = tint,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}
