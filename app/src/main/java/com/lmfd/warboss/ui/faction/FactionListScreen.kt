package com.lmfd.warboss.ui.faction

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lmfd.warboss.domain.model.Faction
import com.lmfd.warboss.ui.components.TacticalBackground
import com.lmfd.warboss.ui.theme.WarbossTheme
import androidx.compose.material.icons.filled.Download

private fun allianceName(factionName: String): String =
    if (factionName.contains(" - ")) factionName.substringBefore(" - ") else "Other"

private fun shortFactionName(factionName: String): String =
    if (factionName.contains(" - ")) factionName.substringAfter(" - ") else factionName

private fun allianceColor(alliance: String): Color = when {
    alliance.contains("Chaos", ignoreCase = true)    -> Color(0xFFCF1924)
    alliance.contains("Imperium", ignoreCase = true) -> Color(0xFF2255AA)
    alliance.contains("Xenos", ignoreCase = true)    -> Color(0xFF1E7A3A)
    else                                              -> Color(0xFFCC8800)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FactionListScreen(
    onFactionClick: (factionId: String) -> Unit,
    onNavigateToImport: () -> Unit,
    viewModel: FactionListViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    FactionListContent(uiState, searchQuery, viewModel::onSearchQueryChange, onFactionClick, onNavigateToImport)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FactionListContent(
    uiState: FactionListUiState,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onFactionClick: (String) -> Unit,
    onNavigateToImport: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Armies", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = onNavigateToImport) {
                        Icon(
                            Icons.Default.Download,
                            contentDescription = "Import",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            TacticalBackground()
            Column(Modifier.fillMaxSize()) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    placeholder = { Text("Search armies…") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onSearchQueryChange("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear search")
                            }
                        }
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant,
                    ),
                )
            when (uiState) {
                FactionListUiState.Loading -> Box(
                    Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) { CircularProgressIndicator(color = MaterialTheme.colorScheme.primary) }

                is FactionListUiState.Success -> {
                    when {
                        uiState.factions.isEmpty() && searchQuery.isNotEmpty() -> {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text(
                                    "No armies match \"$searchQuery\"",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                        uiState.factions.isEmpty() -> {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        "No armies found.",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                    TextButton(onClick = onNavigateToImport) { Text("Go to Import") }
                                }
                            }
                        }
                        else -> {
                            val grouped = uiState.factions.groupBy { allianceName(it.name) }
                            LazyColumn(Modifier.fillMaxSize()) {
                                grouped.forEach { (alliance, factions) ->
                                    item(key = "header_$alliance") { AllianceHeader(alliance) }
                                    items(factions, key = { it.id }) { faction ->
                                        FactionRow(
                                            faction = faction,
                                            accentColor = allianceColor(alliance),
                                            onClick = { onFactionClick(faction.id) },
                                        )
                                        HorizontalDivider(
                                            color = MaterialTheme.colorScheme.surfaceVariant,
                                            thickness = 0.5.dp,
                                        )
                                    }
                                    item(key = "spacer_$alliance") { Spacer(Modifier.height(8.dp)) }
                                }
                            }
                        }
                    }
                }
            }
            } // close Column
        }
    }
}

@Composable
private fun AllianceHeader(alliance: String) {
    val color = allianceColor(alliance)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.95f))
            .padding(start = 0.dp, top = 16.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .width(4.dp)
                .height(20.dp)
                .background(color)
        )
        Text(
            text = alliance.uppercase(),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = color,
            modifier = Modifier.padding(start = 12.dp),
            letterSpacing = androidx.compose.ui.unit.TextUnit(
                2f, androidx.compose.ui.unit.TextUnitType.Sp
            ),
        )
    }
}

@Composable
private fun FactionRow(faction: Faction, accentColor: Color, onClick: () -> Unit) {
    val displayName = shortFactionName(faction.name)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .background(MaterialTheme.colorScheme.background.copy(alpha = 0.6f))
            .padding(vertical = 0.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .width(3.dp)
                .height(56.dp)
                .background(accentColor.copy(alpha = 0.6f))
        )
        Column(
            Modifier
                .weight(1f)
                .padding(start = 16.dp, top = 12.dp, bottom = 12.dp),
        ) {
            Text(
                displayName,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                "Rev. ${faction.revision}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Icon(
            Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            tint = accentColor.copy(alpha = 0.7f),
            modifier = Modifier.padding(end = 16.dp),
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0D0D0D)
@Composable
private fun FactionListSuccessPreview() {
    WarbossTheme {
        FactionListContent(
            uiState = FactionListUiState.Success(listOf(
                Faction("f-1", "Chaos - Death Guard", 114),
                Faction("f-2", "Chaos - World Eaters", 95),
                Faction("f-3", "Imperium - Space Marines", 480),
                Faction("f-4", "Xenos - Tyranids", 201),
            )),
            searchQuery = "",
            onSearchQueryChange = {},
            onFactionClick = {},
            onNavigateToImport = {},
        )
    }
}
