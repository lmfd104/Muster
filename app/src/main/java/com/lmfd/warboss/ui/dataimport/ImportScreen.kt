package com.lmfd.warboss.ui.dataimport

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lmfd.warboss.ui.theme.WarbossTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportScreen(
    onImportComplete: () -> Unit,
    viewModel: ImportViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    ImportContent(
        uiState = uiState,
        onStart = viewModel::startImport,
        onCancel = viewModel::cancelImport,
        onBrowse = onImportComplete,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ImportContent(
    uiState: ImportUiState,
    onStart: () -> Unit,
    onCancel: () -> Unit,
    onBrowse: () -> Unit,
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Army Data Import") }) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp),
            contentAlignment = Alignment.Center,
        ) {
            when (uiState) {
                ImportUiState.Idle -> IdleContent(onStart)
                is ImportUiState.Interrupted -> InterruptedContent(onStart)
                is ImportUiState.Downloading -> DownloadingContent(uiState.progress, onCancel)
                is ImportUiState.Parsing -> ParsingContent(uiState.factionName, uiState.progress, onCancel)
                is ImportUiState.Complete -> CompleteContent(uiState.factionCount, onBrowse, onStart)
                is ImportUiState.Error -> ErrorContent(uiState.message, onStart)
            }
        }
    }
}

@Composable
private fun IdleContent(onStart: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text("No Army Data", style = MaterialTheme.typography.headlineMedium)
        Text(
            "Import army data from BSData to browse units and build lists.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(8.dp))
        Button(onClick = onStart) { Text("Import Army Data") }
    }
}

@Composable
private fun InterruptedContent(onStart: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text("Import Interrupted", style = MaterialTheme.typography.headlineMedium)
        Text(
            "A previous import was not completed. Tap below to try again.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(8.dp))
        Button(onClick = onStart) { Text("Retry Import") }
    }
}

@Composable
private fun DownloadingContent(progress: Float, onCancel: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        CircularProgressIndicator()
        Text("Downloading army data…")
        if (progress >= 0f) {
            LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth())
        } else {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }
        OutlinedButton(onClick = onCancel) { Text("Cancel") }
    }
}

@Composable
private fun ParsingContent(factionName: String, progress: Float, onCancel: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        CircularProgressIndicator()
        Text("Parsing: $factionName")
        LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth())
        OutlinedButton(onClick = onCancel) { Text("Cancel") }
    }
}

@Composable
private fun CompleteContent(factionCount: Int, onBrowse: () -> Unit, onReimport: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text("Import Complete", style = MaterialTheme.typography.headlineMedium)
        Text(
            "$factionCount armies imported",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(8.dp))
        Button(onClick = onBrowse) { Text("Browse Armies") }
        OutlinedButton(onClick = onReimport) { Text("Re-import") }
    }
}

@Composable
private fun ErrorContent(message: String, onRetry: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text("Import Failed", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.error)
        Text(
            message,
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(8.dp))
        Button(onClick = onRetry) { Text("Retry") }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0D0D0D)
@Composable
private fun ImportIdlePreview() {
    WarbossTheme { ImportContent(ImportUiState.Idle, {}, {}, {}) }
}

@Preview(showBackground = true, backgroundColor = 0xFF0D0D0D)
@Composable
private fun ImportCompletePreview() {
    WarbossTheme { ImportContent(ImportUiState.Complete(42, 3), {}, {}, {}) }
}
