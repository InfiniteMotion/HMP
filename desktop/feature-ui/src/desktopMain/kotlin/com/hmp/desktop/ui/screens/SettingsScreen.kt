package com.hmp.desktop.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hmp.domain.enum.AiProviderType
import com.hmp.domain.music.usecase.LoadMusicFromDeviceUseCase
import com.hmp.domain.setting.SettingsRepository
import com.hmp.domain.setting.model.AiProviderConfig
import com.hmp.desktop.player.DesktopMusicController
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(modifier: Modifier = Modifier) {
    val settingsRepository: SettingsRepository = koinInject()
    val loadMusicUseCase: LoadMusicFromDeviceUseCase = koinInject()
    val controller: DesktopMusicController = koinInject()
    val scope = rememberCoroutineScope()

    var scanStatus by remember { mutableStateOf("") }
    var currentProvider by remember { mutableStateOf(AiProviderType.DEEPSEEK) }
    var apiKey by remember { mutableStateOf("") }
    var model by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        currentProvider = settingsRepository.getCurrentProvider()
        val config = settingsRepository.getCurrentProviderConfig()
        apiKey = config.apiKey
        model = config.model
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        TopAppBar(title = { Text("Settings") })

        // Music Library
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Music Library", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = {
                    scope.launch {
                        scanStatus = "Scanning..."
                        val result = loadMusicUseCase()
                        scanStatus = if (result.isSuccess) "Scan complete!" else "Scan failed: ${result.exceptionOrNull()?.message}"
                    }
                }) {
                    Text("Scan Music")
                }
                if (scanStatus.isNotBlank()) {
                    Text(scanStatus, style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        // AI Provider
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("AI Provider", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Current: ${currentProvider.name}", style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = apiKey,
                    onValueChange = { apiKey = it },
                    label = { Text("API Key") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = model,
                    onValueChange = { model = it },
                    label = { Text("Model") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = {
                    scope.launch {
                        settingsRepository.saveProviderConfig(
                            AiProviderConfig(
                                type = currentProvider,
                                apiKey = apiKey,
                                model = model,
                                isConfigured = apiKey.isNotBlank()
                            )
                        )
                    }
                }) {
                    Text("Save")
                }
            }
        }
    }
}
