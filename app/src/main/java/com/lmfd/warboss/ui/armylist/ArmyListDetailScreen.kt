package com.lmfd.warboss.ui.armylist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lmfd.warboss.domain.model.ArmyList
import com.lmfd.warboss.domain.model.ArmyListEntry
import com.lmfd.warboss.ui.theme.WarbossTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArmyListDetailScreen(
    onBack: () -> Unit,
    viewModel: ArmyListDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    ArmyListDetailContent(
        uiState = uiState,
        onBack = onBack,
        onRemoveEntry = viewModel::removeEntry,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ArmyListDetailContent(
    uiState: ArmyListDetailUiState,
    onBack: () -> Unit,
    onRemoveEntry: (String) -> Unit = {},
) {
    val title = when (uiState) {
        is ArmyListDetailUiState.Success -> uiState.list.name
        else -> "Army List"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        when (uiState) {
            ArmyListDetailUiState.Loading -> Box(
                Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center,
            ) { CircularProgressIndicator() }

            ArmyListDetailUiState.NotFound -> Box(
                Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center,
            ) { Text("List not found.", color = MaterialTheme.colorScheme.onSurfaceVariant) }

            is ArmyListDetailUiState.Success -> ArmyListDetailBody(
                list = uiState.list,
                entries = uiState.entries,
                onRemoveEntry = onRemoveEntry,
                modifier = Modifier.padding(padding),
            )
        }
    }
}

@Composable
private fun ArmyListDetailBody(
    list: ArmyList,
    entries: List<ArmyListEntry>,
    onRemoveEntry: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(modifier.fillMaxSize()) {
        item {
            PointsHeader(list = list, modifier = Modifier.padding(16.dp))
        }

        if (entries.isEmpty()) {
            item {
                Box(
                    Modifier.fillMaxWidth().padding(32.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        "No units yet.\nBrowse armies and tap + on a unit to add it here.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        } else {
            items(entries, key = { it.id }) { entry ->
                EntryRow(entry = entry, onRemove = { onRemoveEntry(entry.id) })
            }
        }
    }
}

@Composable
private fun PointsHeader(list: ArmyList, modifier: Modifier = Modifier) {
    Card(modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(list.factionName, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("${list.unitCount} units", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (list.pointsLimit > 0) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("${list.pointsTotal} pts", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(
                        "/ ${list.pointsLimit} pts",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                val progress = (list.pointsTotal.toFloat() / list.pointsLimit).coerceIn(0f, 1f)
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth(),
                    color = if (list.pointsTotal > list.pointsLimit) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.primary,
                )
            } else {
                Text("${list.pointsTotal} pts", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun EntryRow(entry: ArmyListEntry, onRemove: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(entry.unitName, style = MaterialTheme.typography.bodyLarge)
            Text(
                "×${entry.quantity} · ${entry.unitPoints * entry.quantity} pts",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        IconButton(onClick = onRemove) {
            Icon(
                Icons.Default.Delete,
                contentDescription = "Remove",
                tint = MaterialTheme.colorScheme.error,
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0D0D0D)
@Composable
private fun ArmyListDetailPreview() {
    WarbossTheme {
        ArmyListDetailContent(
            uiState = ArmyListDetailUiState.Success(
                list = ArmyList("1", "Nurgle Crusade", "f-dg", "Death Guard", 820, 1000, 3),
                entries = listOf(
                    ArmyListEntry("e1", "1", "u-bt", "Blightlord Terminators", 115, 1),
                    ArmyListEntry("e2", "1", "u-cs", "Chaos Spawn", 80, 2),
                    ArmyListEntry("e3", "1", "u-bn", "Beasts of Nurgle", 75, 1),
                ),
            ),
            onBack = {},
        )
    }
}
