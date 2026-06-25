package com.lmfd.warboss.ui.unit

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lmfd.warboss.domain.model.ArmyList
import com.lmfd.warboss.domain.model.CategoryLink
import com.lmfd.warboss.domain.model.UnitDetail
import com.lmfd.warboss.domain.model.UnitProfile
import com.lmfd.warboss.domain.model.UnitSummary
import com.lmfd.warboss.ui.theme.WarbossTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnitDetailScreen(
    onBack: () -> Unit,
    viewModel: UnitDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val showAddSheet by viewModel.showAddSheet.collectAsState()
    val armyListsForFaction by viewModel.armyListsForFaction.collectAsState()
    val addResult by viewModel.addResult.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(addResult) {
        when (val result = addResult) {
            is AddToListResult.Success -> {
                snackbarHostState.showSnackbar("Added to ${result.listName}")
                viewModel.clearAddResult()
            }
            is AddToListResult.Error -> {
                snackbarHostState.showSnackbar(result.message)
                viewModel.clearAddResult()
            }
            null -> Unit
        }
    }

    UnitDetailContent(
        uiState = uiState,
        onBack = onBack,
        snackbarHostState = snackbarHostState,
        onOpenAddSheet = viewModel::openAddSheet,
    )

    if (showAddSheet) {
        AddToListSheet(
            armyLists = armyListsForFaction,
            onDismiss = viewModel::closeAddSheet,
            onPickList = { list -> viewModel.addToList(list.id, list.name) },
            onCreateAndAdd = { name, pts -> viewModel.createAndAdd(name, pts) },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UnitDetailContent(
    uiState: UnitDetailUiState,
    onBack: () -> Unit,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onOpenAddSheet: () -> Unit = {},
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        when (uiState) {
                            is UnitDetailUiState.Success -> uiState.detail.summary.name
                            else -> "Unit Detail"
                        }
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            if (uiState is UnitDetailUiState.Success) {
                FloatingActionButton(onClick = onOpenAddSheet) {
                    Icon(Icons.Default.Add, contentDescription = "Add to army list")
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        when (uiState) {
            UnitDetailUiState.Loading -> Box(
                Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center,
            ) { CircularProgressIndicator() }

            UnitDetailUiState.NotFound -> Box(
                Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center,
            ) { Text("Unit not found.", color = MaterialTheme.colorScheme.onSurfaceVariant) }

            is UnitDetailUiState.Success -> UnitDetailBody(uiState.detail, padding)
        }
    }
}

@Composable
private fun UnitDetailBody(detail: UnitDetail, padding: androidx.compose.foundation.layout.PaddingValues) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                DetailRow("Points", "${detail.summary.points}")
                DetailRow("Type", detail.summary.type)
                val qty = if (detail.summary.minQuantity == detail.summary.maxQuantity) {
                    "${detail.summary.minQuantity}"
                } else {
                    "${detail.summary.minQuantity}–${detail.summary.maxQuantity}"
                }
                DetailRow("Models", qty)
            }
        }

        if (detail.profiles.isNotEmpty()) {
            SectionHeader("Stat Blocks")
            detail.profiles.forEach { profile -> ProfileCard(profile) }
        }

        if (detail.keywords.isNotEmpty()) {
            SectionHeader("Keywords")
            KeywordChips(detail.keywords)
        }

        if (detail.factionKeywords.isNotEmpty()) {
            SectionHeader("Faction Keywords")
            KeywordChips(detail.factionKeywords)
        }

        if (detail.categoryLinks.isNotEmpty()) {
            SectionHeader("Categories")
            detail.categoryLinks.forEach { link ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(link.categoryName, style = MaterialTheme.typography.bodyMedium)
                    if (link.isPrimary) {
                        Text(
                            "Primary",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.secondary,
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(80.dp)) // FAB clearance
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddToListSheet(
    armyLists: List<ArmyList>,
    onDismiss: () -> Unit,
    onPickList: (ArmyList) -> Unit,
    onCreateAndAdd: (name: String, pts: Int) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showCreateDialog by rememberSaveable { mutableStateOf(false) }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(Modifier.padding(bottom = 32.dp)) {
            Text(
                "Add to Army List",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
            HorizontalDivider()

            if (armyLists.isEmpty()) {
                Text(
                    "No lists for this faction yet.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(16.dp),
                )
            } else {
                armyLists.forEach { list ->
                    ListItem(
                        headlineContent = { Text(list.name) },
                        supportingContent = { Text("${list.pointsTotal} / ${list.pointsLimit} pts · ${list.unitCount} units") },
                        modifier = Modifier.clickable { onPickList(list) },
                    )
                }
                HorizontalDivider()
            }

            TextButton(
                onClick = { showCreateDialog = true },
                modifier = Modifier.padding(horizontal = 8.dp),
            ) { Text("+ Create new list for this faction") }
        }
    }

    if (showCreateDialog) {
        CreateListDialog(
            onDismiss = { showCreateDialog = false },
            onCreate = { name, pts ->
                showCreateDialog = false
                onCreateAndAdd(name, pts)
            },
        )
    }
}

@Composable
private fun CreateListDialog(
    onDismiss: () -> Unit,
    onCreate: (name: String, pts: Int) -> Unit,
) {
    var name by rememberSaveable { mutableStateOf("") }
    var ptsText by rememberSaveable { mutableStateOf("") }

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
                )
                OutlinedTextField(
                    value = ptsText,
                    onValueChange = { ptsText = it.filter { c -> c.isDigit() } },
                    label = { Text("Points limit (optional)") },
                    singleLine = true,
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { if (name.isNotBlank()) onCreate(name.trim(), ptsText.toIntOrNull() ?: 0) },
                enabled = name.isNotBlank(),
            ) { Text("Create & Add") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun SectionHeader(title: String) {
    Column {
        Text(title, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
        HorizontalDivider()
    }
}

@Composable
private fun ProfileCard(profile: UnitProfile) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("${profile.name} (${profile.typeName})", style = MaterialTheme.typography.titleSmall)
            profile.characteristics.entries.forEach { (key, value) ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(key, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun KeywordChips(keywords: List<String>) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        keywords.forEach { keyword ->
            SuggestionChip(onClick = {}, label = { Text(keyword) })
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0D0D0D)
@Composable
private fun UnitDetailPreview() {
    WarbossTheme {
        UnitDetailContent(
            uiState = UnitDetailUiState.Success(
                UnitDetail(
                    summary = UnitSummary("u-1", "f-1", "Warboss", "unit", 85, 1, 1, false),
                    profiles = listOf(
                        UnitProfile(
                            "p-1", "Warboss", "Infantry",
                            mapOf("M" to "5\"", "T" to "6", "Sv" to "3+", "W" to "7", "Ld" to "7+", "OC" to "2")
                        )
                    ),
                    keywords = listOf("Infantry", "Character", "Warboss"),
                    factionKeywords = listOf("Orks"),
                    categoryLinks = listOf(CategoryLink("cat-1", "Characters", true)),
                )
            ),
            onBack = {},
        )
    }
}
