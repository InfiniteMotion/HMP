package com.hmp.storybook.layout

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MenuOpen
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.hmp.storybook.i18n.Strings
import com.hmp.storybook.navigation.StorybookNavController
import com.hmp.storybook.navigation.navGroups
import com.hmp.storybook.theme.AppLanguage
import com.hmp.storybook.theme.HDBlue
import com.hmp.storybook.theme.LocalAppLanguage

/**
 * Storybook 主布局：HMP 风格顶部栏 + 侧边栏 + 内容区
 */
@Composable
fun StorybookLayout(
    navController: StorybookNavController,
    content: @Composable () -> Unit,
) {
    var sidebarExpanded by remember { mutableStateOf(true) }
    var currentLanguage by remember { mutableStateOf(AppLanguage.ZH) }

    CompositionLocalProvider(LocalAppLanguage provides currentLanguage) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopBar(
                sidebarExpanded = sidebarExpanded,
                onToggleSidebar = { sidebarExpanded = !sidebarExpanded },
                language = currentLanguage,
                onSwitchLanguage = {
                    currentLanguage = if (currentLanguage == AppLanguage.ZH) AppLanguage.EN else AppLanguage.ZH
                },
            )

            Row(modifier = Modifier.fillMaxSize().weight(1f)) {
                if (sidebarExpanded) {
                    Sidebar(
                        modifier = Modifier.width(260.dp),
                        language = currentLanguage,
                        navController = navController,
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surfaceContainerLow)
                        .verticalScroll(rememberScrollState())
                        .padding(24.dp),
                ) {
                    content()
                }
            }
        }
    }
}

// ========== TopBar：HMP 风格 ==========

@Composable
private fun TopBar(
    sidebarExpanded: Boolean,
    onToggleSidebar: () -> Unit,
    language: AppLanguage,
    onSwitchLanguage: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainer),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onToggleSidebar) {
                Icon(
                    imageVector = if (sidebarExpanded) Icons.Filled.MenuOpen else Icons.Filled.Menu,
                    contentDescription = "Toggle sidebar",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = Strings.hearableMusicPlayer(language),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = HDBlue,
            )

            Spacer(modifier = Modifier.weight(1f))

            TextButton(onClick = onSwitchLanguage) {
                Text(
                    text = Strings.switchLanguage(language),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        HorizontalDivider(
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
        )
    }
}

// ========== Sidebar：HMP 风格深色侧栏 ==========

@Composable
private fun Sidebar(
    modifier: Modifier = Modifier,
    language: AppLanguage,
    navController: StorybookNavController,
) {
    val currentRoute = navController.currentRoute

    Column(
        modifier = modifier
            .fillMaxHeight()
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .verticalScroll(rememberScrollState())
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        navGroups.forEach { group ->
            // 分组标题
            Text(
                text = group.titleKey(language),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            )

            group.routes.forEach { (route, titleKey) ->
                val isSelected = currentRoute == route
                val itemColor by animateColorAsState(
                    targetValue = if (isSelected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    animationSpec = tween(200),
                    label = "navItemColor",
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { navController.navigateTo(route) },
                        )
                        .then(
                            if (isSelected) {
                                Modifier.background(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                    RoundedCornerShape(8.dp),
                                )
                            } else {
                                Modifier
                            },
                        )
                        .padding(start = 12.dp, top = 8.dp, end = 12.dp, bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // 左侧指示条
                    Box(
                        modifier = Modifier
                            .width(3.dp)
                            .height(16.dp)
                            .clip(RoundedCornerShape(1.5.dp))
                            .background(
                                if (isSelected) itemColor else Color.Transparent,
                            ),
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = titleKey(language),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                        color = itemColor,
                    )
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 6.dp, horizontal = 12.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
            )
        }
    }
}
