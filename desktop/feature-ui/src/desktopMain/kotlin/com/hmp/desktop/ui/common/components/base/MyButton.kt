package com.hmp.desktop.ui.common.components.base
import com.hmp.desktop.ui.common.navigation.NavController

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color.Companion.Transparent
import androidx.compose.ui.unit.dp


import com.hmp.domain.playlist.AlgorithmType
import com.hmp.domain.playlist.ExtensionConfig
import com.hmp.domain.playlist.WeightTemplate
import com.hmp.desktop.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.painterResource
import com.hmp.desktop.ui.common.components.SegmentedControl
import com.hmp.desktop.ui.common.components.SegmentedOption
import com.hmp.desktop.ui.common.design.animation.AnimationTokens
import com.hmp.desktop.ui.common.navigation.Routes
import com.hmp.desktop.ui.common.util.rememberHapticFeedback

@Composable
fun PlayControlButtonOne(
    selectedGenre: String,
    selectedOrder: String,
    onFilterGenreChange: (String) -> Unit,
    onFilterOrderChange: (String) -> Unit,
    onOrderPlay: () -> Unit,
    onShufflePlay: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val haptic = rememberHapticFeedback()

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            IconButton(
                onClick = {
                    haptic.performClick()
                    expanded = !expanded
                },
                modifier = Modifier
                    .size(32.dp)
            ) {
                Icon(
                    painter = painterResource(Res.drawable.slider_vertical_3),
                    tint = MaterialTheme.colorScheme.onSurface,
                    contentDescription = "select Button",
                    modifier = Modifier.size(24.dp),
                )
            }
            IconButton(
                onClick = {
                    onOrderPlay()
                },
                modifier = Modifier
                    .size(32.dp)
            ) {
                Icon(
                    painter = painterResource(Res.drawable.order_play),
                    tint = MaterialTheme.colorScheme.onSurface,
                    contentDescription = "order play Button",
                    modifier = Modifier.size(24.dp),
                )
            }
            IconButton(
                onClick = {
                    haptic.performConfirm()
                    onShufflePlay()
                },
                modifier = Modifier
                    .size(32.dp)
            ) {
                Icon(
                    painter = painterResource(Res.drawable.`shuffle`),
                    tint = MaterialTheme.colorScheme.onSurface,
                    contentDescription = "shuffle play Button",
                    modifier = Modifier.size(24.dp),
                )
            }
            IconButton(
                onClick = {
                    haptic.performLightClick()
                },
                modifier = Modifier
                    .size(32.dp)
            ) {
                Icon(
                    painter = painterResource(Res.drawable.lightbulb),
                    tint = MaterialTheme.colorScheme.onSurface,
                    contentDescription = "self play Button",
                    modifier = Modifier.size(24.dp),
                )
            }
        }
        // 可隐藏的内容块
        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically(animationSpec = tween(durationMillis = 300, easing = AnimationTokens.EASE_OUT)) +
                    fadeIn(animationSpec = tween(durationMillis = 300, easing = AnimationTokens.EASE_OUT)),
            exit = shrinkVertically(animationSpec = tween(durationMillis = 300, easing = AnimationTokens.EASE_IN)) +
                    fadeOut(animationSpec = tween(durationMillis = 300, easing = AnimationTokens.EASE_IN))
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                color = Transparent,
            ) {
                val genres = listOf(
                    stringResource(Res.string.sort_title) to "title",
                    stringResource(Res.string.sort_artist) to "artist",
                    stringResource(Res.string.sort_duration) to "duration",
                    stringResource(Res.string.sort_size) to "fileSize",
                    stringResource(Res.string.sort_play_count) to "playCount",
                    stringResource(Res.string.sort_add_time) to "date"
                )
                val orders = listOf(
                    stringResource(Res.string.order_asc) to "ASC",
                    stringResource(Res.string.order_desc) to "DESC"
                )
                Column(
                    modifier = Modifier.padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = stringResource(Res.string.sort),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        FlowRow(
                            maxItemsInEachRow = 3,
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            genres.forEach { (genre,eGenre) ->
                                FilterChip(
                                    selected = selectedGenre == eGenre,
                                    onClick = {
                                        haptic.performLightClick()
                                        (if (selectedGenre == eGenre) null else eGenre)?.let {
                                            onFilterGenreChange(it)
                                        }
                                    },
                                    label = { Text(text = genre, style = MaterialTheme.typography.titleSmall) },
                                    // 选中状态样式
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    ),
                                )
                            }
                        }
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = stringResource(Res.string.order),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            orders.forEach{ (order,eOrder) ->
                                FilterChip(
                                    selected = selectedOrder == eOrder,
                                    onClick = {
                                        haptic.performLightClick()
                                        (if (selectedOrder == eOrder) null else eOrder)?.let {
                                            onFilterOrderChange(it)
                                        }
                                    },
                                    label = { Text(text = order, style = MaterialTheme.typography.titleSmall) },
                                    // 选中状态样式
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    ),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PlayControlButtonTwo(
    onShufflePlay: () -> Unit,
    onOrderPlay: () -> Unit
) {
    val haptic = rememberHapticFeedback()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            IconButton(
                    onClick = {
                        haptic.performConfirm()
                        onShufflePlay()
                    },
            modifier = Modifier
                .size(32.dp)
            ) {
            Icon(
                painter = painterResource(Res.drawable.`shuffle`),
                tint = MaterialTheme.colorScheme.onSurface,
                contentDescription = "shuffle play Button",
                modifier = Modifier.size(24.dp),
            )
            }
            Spacer(modifier = Modifier.width(160.dp))
            IconButton(
                onClick = {
                    haptic.performConfirm()
                    onOrderPlay()
                },
                modifier = Modifier
                    .size(32.dp)
            ) {
                Icon(
                    painter = painterResource(Res.drawable.order_play),
                    tint = MaterialTheme.colorScheme.onSurface,
                    contentDescription = "order play Button",
                    modifier = Modifier.size(24.dp),
                )
            }
        }
    }
}



@Composable
fun BackButton(
    onClick: () -> Unit
){
    val haptic = rememberHapticFeedback()
    FilledIconButton(
        onClick = {
            haptic.performClick()
            onClick()
        },
        modifier = Modifier
            .size(32.dp), // Larger touch target
        colors = IconButtonDefaults.filledIconButtonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        )
    ) {
        Icon(
            painter = painterResource(Res.drawable.chevron_left),
            contentDescription = "Back Button",
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
fun SearchButton(
    navController: NavController
){
    val haptic = rememberHapticFeedback()
    FilledIconButton(
        onClick = {
            haptic.performClick()
            navController.navigate(Routes.Library.Search)
        },
        modifier = Modifier
            .size(32.dp), // Larger touch target
        colors = IconButtonDefaults.filledIconButtonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        )
    ) {
        Icon(
            painter = painterResource(Res.drawable.magnifyingglass),
            contentDescription = "Search Button",
            modifier = Modifier.size(16.dp)
        )
    }
}

@Composable
fun NewPlaylistButton(onClick: () -> Unit) {
    val haptic = rememberHapticFeedback()
    FilledIconButton(
        onClick = {
            haptic.performClick()
            onClick()
        },
        modifier = Modifier.size(32.dp),
        colors = IconButtonDefaults.filledIconButtonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        )
    ) {
        Icon(
            painter = painterResource(Res.drawable.plus),
            contentDescription = "New Playlist",
            modifier = Modifier.size(16.dp)
        )
    }
}

/**
 * 播放列表生成组合按钮组件
 * 提供智能播放列表生成功能入口，采用两排组合按钮布局
 * 第一排：偏好选择（多选一）
 * 第二排：算法选择（二选一）+ 生成按钮
 */
@Composable
fun GeneratePlaylistComboButtons(
    seedMusicId: Long,
    defaultAlgorithmType: AlgorithmType?,
    defaultTemplate: WeightTemplate?,
    onGeneratePlaylist: (Long) -> Unit,
    onSaveDefaultConfig: ((AlgorithmType, WeightTemplate, ExtensionConfig) -> Unit),
    horizontalLayout: Boolean = false
) {
    val haptic = rememberHapticFeedback()
    var selectedAlgorithm by remember { mutableStateOf(defaultAlgorithmType?: AlgorithmType.OPTIMIZED_SIMILARITY) }
    var selectedTemplate by remember { mutableStateOf(defaultTemplate?: WeightTemplate.BALANCED) }
    val weightOptions = listOf(
        SegmentedOption(id = WeightTemplate.BALANCED.name, label = "平衡"),
        SegmentedOption(id = WeightTemplate.GENRE_FOCUS.name, label = "风格"),
        SegmentedOption(id = WeightTemplate.MOOD_FOCUS.name, label = "情绪"),
        SegmentedOption(id = WeightTemplate.SCENARIO_FOCUS.name, label = "场景"),
        SegmentedOption(id = WeightTemplate.ERA_FOCUS.name, label = "年代")
    )
    val algorithmOptions = listOf(
        SegmentedOption(id = AlgorithmType.OPTIMIZED_SIMILARITY.name, label = "相似"),
        SegmentedOption(id = AlgorithmType.CHAIN_SIMILARITY.name, label = "心动")
    )

    if (horizontalLayout) {
        // Expanded 两列布局：左侧算法配置，右侧正方形生成按钮
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SegmentedControl(
                    modifier = Modifier,
                    options = weightOptions,
                    selectedOption = selectedTemplate.name,
                    onOptionSelected = { optionId ->
                        WeightTemplate.entries.find { it.name == optionId }?.let {
                            selectedTemplate = it
                        }
                        onSaveDefaultConfig(selectedAlgorithm, selectedTemplate, ExtensionConfig())
                        haptic.performClick()
                    },
                    showIcons = false
                )
                SegmentedControl(
                    modifier = Modifier,
                    options = algorithmOptions,
                    selectedOption = selectedAlgorithm.name,
                    onOptionSelected = { optionId ->
                        AlgorithmType.entries.find { it.name == optionId }?.let {
                            selectedAlgorithm = it
                        }
                        onSaveDefaultConfig(selectedAlgorithm, selectedTemplate, ExtensionConfig())
                        haptic.performClick()
                    },
                    showIcons = false
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // 右侧正方形生成按钮（高度与左侧两行控件一致）
            Box(
                modifier = Modifier
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                    .border(1.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(16.dp))
                    .clickable { onGeneratePlaylist(seedMusicId) },
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.lightbulb),
                        tint = MaterialTheme.colorScheme.primary,
                        contentDescription = "generate Button",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "生成推荐列表",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    } else {
        // 默认纵向布局
        Column(
            modifier = Modifier,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            SegmentedControl(
                modifier = Modifier,
                options = weightOptions,
                selectedOption = selectedTemplate.name,
                onOptionSelected = { optionId ->
                    WeightTemplate.entries.find { it.name == optionId }?.let {
                        selectedTemplate = it
                    }
                    onSaveDefaultConfig(selectedAlgorithm, selectedTemplate, ExtensionConfig())
                    haptic.performClick()
                },
                showIcons = false
            )

            SegmentedControl(
                modifier = Modifier,
                options = algorithmOptions,
                selectedOption = selectedAlgorithm.name,
                onOptionSelected = { optionId ->
                    AlgorithmType.entries.find { it.name == optionId }?.let {
                        selectedAlgorithm = it
                    }
                    onSaveDefaultConfig(selectedAlgorithm, selectedTemplate, ExtensionConfig())
                    haptic.performClick()
                },
                showIcons = false
            )

            Row(
                modifier = Modifier
                    .height(48.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                    .border(1.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(16.dp))
                    .clickable { onGeneratePlaylist(seedMusicId) },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    painter = painterResource(Res.drawable.lightbulb),
                    tint = MaterialTheme.colorScheme.primary,
                    contentDescription = "generate Button",
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "生成推荐列表",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}