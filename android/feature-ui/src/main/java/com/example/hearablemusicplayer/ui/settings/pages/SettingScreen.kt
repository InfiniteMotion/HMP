package com.example.hearablemusicplayer.ui.settings.pages

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.example.hearablemusicplayer.ui.R
import com.example.hearablemusicplayer.ui.common.pages.base.SubScreen
import com.example.hearablemusicplayer.ui.common.layout.LocalWindowSizeInfo
import com.example.hearablemusicplayer.ui.common.navigation.Routes
import com.example.hearablemusicplayer.ui.common.util.rememberHapticFeedback

@Composable
fun SettingScreen(
    navController: NavBackStack<NavKey>
) {
    SubScreen(
        onBackClick = { navController.removeLastOrNull() },
        title = stringResource(R.string.title_settings)
    ) {
        val haptic = rememberHapticFeedback()
        val isLandscape = LocalWindowSizeInfo.current.isLandscape
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (isLandscape) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        SettingItem(
                            title = stringResource(R.string.profile_settings),
                            description = stringResource(R.string.profile_desc),
                            icon = R.drawable.person,
                            onClick = {
                                haptic.performClick()
                                navController.add(Routes.Settings.ProfileSettings)
                            }
                        )
                        SettingItem(
                            title = stringResource(R.string.backup_settings),
                            description = stringResource(R.string.backup_desc),
                            icon = R.drawable.externaldrive,
                            onClick = {
                                haptic.performClick()
                                navController.add(Routes.Settings.BackupSettings)
                            }
                        )
                    }
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        SettingItem(
                            title = stringResource(R.string.library_settings),
                            description = stringResource(R.string.library_desc),
                            icon = R.drawable.music,
                            onClick = {
                                haptic.performClick()
                                navController.add(Routes.Settings.LibrarySettings)
                            }
                        )
                    }
                }
            } else {
                SettingItem(
                    title = stringResource(R.string.profile_settings),
                    description = stringResource(R.string.profile_desc),
                    icon = R.drawable.person,
                    onClick = {
                        haptic.performClick()
                        navController.add(Routes.Settings.ProfileSettings)
                    }
                )
                SettingItem(
                    title = stringResource(R.string.backup_settings),
                    description = stringResource(R.string.backup_desc),
                    icon = R.drawable.externaldrive,
                    onClick = {
                        haptic.performClick()
                        navController.add(Routes.Settings.BackupSettings)
                    }
                )
                SettingItem(
                    title = stringResource(R.string.library_settings),
                    description = stringResource(R.string.library_desc),
                    icon = R.drawable.music,
                    onClick = {
                        haptic.performClick()
                        navController.add(Routes.Settings.LibrarySettings)
                    }
                )
            }
        }
    }
}

@Composable
fun SettingItem(
    title: String,
    description: String,
    icon: Int,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(icon),
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                painter = painterResource(R.drawable.chevron_right),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
