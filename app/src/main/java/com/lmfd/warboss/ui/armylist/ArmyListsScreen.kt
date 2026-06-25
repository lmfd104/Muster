package com.lmfd.warboss.ui.armylist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lmfd.warboss.domain.model.ArmyList
import com.lmfd.warboss.domain.model.Faction
import com.lmfd.warboss.ui.theme.WarbossTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArmyListsScreen(
    onListClick: (listId: String) -> Unit,
    viewModel: ArmyListsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val factions by viewModel.factions.collectAsState()
    var showCreateDialog by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("My Army Lists") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showCreateDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "New army list")
            }
        },
    ) { padding ->
        when (uiState) {
            ArmyListsUiState.Loading -> Box(
                Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center,
            ) { CircularProgressIndicator() }

            is ArmyListsUiState.Success -> {
                val lists = (uiState as ArmyListsUiState.Success).lists
                if (lists.isEmpty()) {
                    EmptyListsContent(Modifier.fillMaxSize().padding(padding))
                } else {
                    LazyColumn(Modifier.fillMaxSize().padding(padding)) {
                        items(lists, key = { it.id }) { list ->
                            ArmyListRow(list = list, onClick = { onListClick(list.id) })
                        }
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        CreateArmyListDialog(
            factions = factions,
            onDismiss = { showCreateDialog = false },
            onCreate = { name, factionId, factionName, pts ->
                viewModel.createList(name, factionId, factionName, pts)
                showCreateDialog = false
            },
        )
    }
}

@Composable
private fun EmptyListsContent(modifier: Modifier = Modifier) {
    Box(modifier, contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(32.dp),
        ) {
            Text("No army lists yet", style = MaterialTheme.typography.headlineSmall)
            Text(
                "Browse armies and tap + on a unit to add it to a list.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun ArmyListRow(list: ArmyList, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(list.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(
                    list.factionName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(4.dp))
                val ptsText = if (list.pointsLimit > 0) {
                    "${list.pointsTotal} / ${list.pointsLimit} pts"
                } else {
                    "${list.pointsTotal} pts"
                }
                Text(
                    "$ptsText · ${list.unitCount} units",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.secondary,
                )
            }
            Icon(
                Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateArmyListDialog(
    factions: List<Faction>,
    onDismiss: () -> Unit,
    onCreate: (name: String, factionId: String, factionName: String, pts: Int) -> Unit,
) {
    var name by rememberSaveable { mutableStateOf("") }
    var ptsText by rememberSaveable { mutableStateOf("") }
    var expanded by rememberSaveable { mutableStateOf(false) }
    var selectedFaction by rememberSaveable { mutableStateOf<Faction?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Army List") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("List name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it },
                ) {
                    OutlinedTextField(
                        value = selectedFaction?.name ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Faction") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                    ) {
                        factions.forEach { faction ->
                            DropdownMenuItem(
                                text = { Text(faction.name) },
                                onClick = {
                                    selectedFaction = faction
                                    expanded = false
                                },
                            )
                        }
                    }
                }
                OutlinedTextField(
                    value = ptsText,
                    onValueChange = { ptsText = it.filter { c -> c.isDigit() } },
                    label = { Text("Points limit (optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            val faction = selectedFaction
            TextButton(
                onClick = {
                    if (faction != null && name.isNotBlank()) {
                        onCreate(name.trim(), faction.id, faction.name, ptsText.toIntOrNull() ?: 0)
                    }
                },
                enabled = name.isNotBlank() && selectedFaction != null,
            ) { Text("Create") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF0D0D0D)
@Composable
private fun ArmyListsEmptyPreview() {
    WarbossTheme { EmptyListsContent(Modifier.fillMaxSize()) }
}

@Preview(showBackground = true, backgroundColor = 0xFF0D0D0D)
@Composable
private fun ArmyListRowPreview() {
    WarbossTheme {
        ArmyListRow(
            list = ArmyList("1", "Nurgle Crusade", "f-dg", "Death Guard", 820, 1000, 7),
            onClick = {},
        )
    }
}
