package com.lmfd.warboss.ui.faction

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lmfd.warboss.domain.model.Faction
import com.lmfd.warboss.ui.theme.WarbossTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FactionListScreen(
    onFactionClick: (factionId: String) -> Unit,
    onNavigateToImport: () -> Unit,
    viewModel: FactionListViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    FactionListContent(uiState, onFactionClick, onNavigateToImport)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FactionListContent(
    uiState: FactionListUiState,
    onFactionClick: (String) -> Unit,
    onNavigateToImport: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Armies") },
                navigationIcon = {
                    IconButton(onClick = onNavigateToImport) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Import")
                    }
                }
            )
        }
    ) { padding ->
        when (uiState) {
            FactionListUiState.Loading -> Box(
                Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }

            is FactionListUiState.Success -> {
                if (uiState.factions.isEmpty()) {
                    Box(
                        Modifier.fillMaxSize().padding(padding),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "No armies found.",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            TextButton(onClick = onNavigateToImport) { Text("Go to Import") }
                        }
                    }
                } else {
                    LazyColumn(Modifier.fillMaxSize().padding(padding)) {
                        items(uiState.factions, key = { it.id }) { faction ->
                            FactionRow(faction, onClick = { onFactionClick(faction.id) })
                            HorizontalDivider()
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FactionRow(faction: Faction, onClick: () -> Unit) {
    ListItem(
        headlineContent = { Text(faction.name) },
        supportingContent = { Text("Rev. ${faction.revision}", style = MaterialTheme.typography.bodySmall) },
        trailingContent = {
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
        },
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF0D0D0D)
@Composable
private fun FactionListLoadingPreview() {
    WarbossTheme { FactionListContent(FactionListUiState.Loading, {}, {}) }
}

@Preview(showBackground = true, backgroundColor = 0xFF0D0D0D)
@Composable
private fun FactionListSuccessPreview() {
    WarbossTheme {
        FactionListContent(
            FactionListUiState.Success(listOf(
                Faction("f-1", "Orks", 162),
                Faction("f-2", "Space Marines", 480),
                Faction("f-3", "Tyranids", 201),
            )),
            onFactionClick = {},
            onNavigateToImport = {},
        )
    }
}
