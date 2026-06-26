package com.lmfd.warboss.ui.unit

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lmfd.warboss.domain.model.ArmyList
import com.lmfd.warboss.domain.model.CategoryLink
import com.lmfd.warboss.domain.model.UnitDetail
import com.lmfd.warboss.domain.model.UnitProfile
import com.lmfd.warboss.domain.model.UnitSummary
import com.lmfd.warboss.ui.components.TacticalBackground
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
                        },
                        fontWeight = FontWeight.Bold,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
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
        Box(Modifier.fillMaxSize().padding(padding)) {
            TacticalBackground()
            when (uiState) {
                UnitDetailUiState.Loading -> Box(
                    Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) { CircularProgressIndicator(color = MaterialTheme.colorScheme.primary) }

                UnitDetailUiState.NotFound -> Box(
                    Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) { Text("Unit not found.", color = MaterialTheme.colorScheme.onSurfaceVariant) }

                is UnitDetailUiState.Success -> UnitDetailBody(uiState.detail)
            }
        }
    }
}

@Composable
private fun UnitDetailBody(detail: UnitDetail) {
    val accent = MaterialTheme.colorScheme.primary
    var selectedKeyword by remember { mutableStateOf<String?>(null) }
    if (selectedKeyword != null) {
        KeywordDialog(keyword = selectedKeyword!!, onDismiss = { selectedKeyword = null })
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Summary quick-stats row
        Card(
            Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        ) {
            Row(
                Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                StatPill(label = "POINTS", value = "${detail.summary.points}")
                VerticalDividerLine()
                StatPill(label = "TYPE", value = detail.summary.type.uppercase())
                VerticalDividerLine()
                val qty = if (detail.summary.minQuantity == detail.summary.maxQuantity)
                    "${detail.summary.minQuantity}"
                else
                    "${detail.summary.minQuantity}–${detail.summary.maxQuantity}"
                StatPill(label = "MODELS", value = qty)
            }
        }

        val unitProfiles   = detail.profiles.filter { it.typeName.equals("Unit", ignoreCase = true) }
        val rangedWeapons  = detail.profiles.filter { it.typeName.contains("Ranged", ignoreCase = true) }
        val meleeWeapons   = detail.profiles.filter { it.typeName.contains("Melee", ignoreCase = true) }
        val abilityProfiles = detail.profiles.filter {
            it.typeName.contains("Abilit", ignoreCase = true) ||
            it.typeName.contains("Transport", ignoreCase = true)
        }
        val otherProfiles  = detail.profiles.filter { p ->
            p !in unitProfiles && p !in rangedWeapons && p !in meleeWeapons && p !in abilityProfiles
        }

        if (unitProfiles.isNotEmpty()) {
            SectionHeader("Stat Block")
            unitProfiles.forEach { profile -> ProfileCard(profile) }
        }

        if (rangedWeapons.isNotEmpty()) {
            SectionHeader("Ranged Weapons")
            WeaponTable(
                weapons = rangedWeapons,
                columns = listOf("Range", "A", "BS", "S", "AP", "D"),
                onKeywordClick = { selectedKeyword = it },
            )
        }

        if (meleeWeapons.isNotEmpty()) {
            SectionHeader("Melee Weapons")
            WeaponTable(
                weapons = meleeWeapons,
                columns = listOf("Range", "A", "WS", "S", "AP", "D"),
                onKeywordClick = { selectedKeyword = it },
            )
        }

        if (abilityProfiles.isNotEmpty()) {
            SectionHeader("Abilities")
            abilityProfiles.forEach { AbilityCard(it) }
        }

        if (otherProfiles.isNotEmpty()) {
            SectionHeader("Other")
            otherProfiles.forEach { profile -> ProfileCard(profile) }
        }

        if (detail.keywords.isNotEmpty()) {
            SectionHeader("Keywords")
            KeywordChips(detail.keywords, onKeywordClick = { selectedKeyword = it })
        }

        if (detail.factionKeywords.isNotEmpty()) {
            SectionHeader("Faction Keywords")
            KeywordChips(detail.factionKeywords, onKeywordClick = { selectedKeyword = it })
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
private fun StatPill(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            letterSpacing = androidx.compose.ui.unit.TextUnit(1f, androidx.compose.ui.unit.TextUnitType.Sp),
        )
        Text(
            value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

@Composable
private fun VerticalDividerLine() {
    Box(
        Modifier
            .width(1.dp)
            .height(40.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant)
    )
}

@Composable
private fun SectionHeader(title: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier
                .width(3.dp)
                .height(18.dp)
                .background(MaterialTheme.colorScheme.primary)
        )
        Text(
            title.uppercase(),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = 8.dp),
            letterSpacing = androidx.compose.ui.unit.TextUnit(1.5f, androidx.compose.ui.unit.TextUnitType.Sp),
        )
    }
}

@Composable
private fun ProfileCard(profile: UnitProfile) {
    Card(
        Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(6.dp),
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // Profile name header with left accent
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .width(3.dp)
                        .height(18.dp)
                        .background(MaterialTheme.colorScheme.secondary)
                )
                Text(
                    "${profile.name} · ${profile.typeName}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(start = 8.dp),
                )
            }
            // Horizontal stat grid: label row then value row
            if (profile.characteristics.isNotEmpty()) {
                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                val entries = profile.characteristics.entries.toList()
                // Labels
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    entries.forEach { (key, _) ->
                        Text(
                            key,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = androidx.compose.ui.unit.TextUnit(0.5f, androidx.compose.ui.unit.TextUnitType.Sp),
                        )
                    }
                }
                // Values
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    entries.forEach { (_, value) ->
                        Text(
                            value,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun WeaponTable(
    weapons: List<UnitProfile>,
    columns: List<String>,
    onKeywordClick: (String) -> Unit = {},
) {
    // Determine which columns actually have data, plus any extra cols in the data
    val extraCols = weapons
        .flatMap { it.characteristics.keys }
        .distinct()
        .filter { it !in columns && !it.equals("Keywords", ignoreCase = true) }
    val shownCols = columns + extraCols
    val hasKeywords = weapons.any { w ->
        w.characteristics.keys.any { it.equals("Keywords", ignoreCase = true) }
    }

    Card(
        Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(6.dp),
    ) {
        Column(Modifier.fillMaxWidth()) {
            // Header row
            Row(
                Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
            ) {
                Text(
                    "NAME",
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                shownCols.forEach { col ->
                    Text(
                        col.uppercase(),
                        modifier = Modifier.width(if (col == "Range") 44.dp else 36.dp),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            weapons.forEachIndexed { index, weapon ->
                if (index > 0) HorizontalDivider(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    thickness = 0.5.dp,
                )
                // Weapon name row
                val keywords = weapon.characteristics.entries
                    .firstOrNull { it.key.equals("Keywords", ignoreCase = true) }?.value
                Column(Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 6.dp)) {
                    // Name + optional keywords on same line
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            weapon.name,
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 2,
                        )
                    }
                    // Stat values row
                    Row(
                        Modifier.fillMaxWidth().padding(top = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Spacer(Modifier.weight(1f))
                        shownCols.forEach { col ->
                            val value = weapon.characteristics.entries
                                .firstOrNull { it.key.equals(col, ignoreCase = true) }?.value ?: "-"
                            Text(
                                value,
                                modifier = Modifier.width(if (col == "Range") 44.dp else 36.dp),
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground,
                            )
                        }
                    }
                    if (!keywords.isNullOrBlank()) {
                        val tokens = keywords.split(",").map { it.trim() }.filter { it.isNotBlank() }
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(top = 4.dp),
                        ) {
                            tokens.forEach { token ->
                                SuggestionChip(
                                    onClick = { onKeywordClick(token) },
                                    label = {
                                        Text(
                                            token,
                                            style = MaterialTheme.typography.labelSmall,
                                        )
                                    },
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
private fun AbilityCard(profile: UnitProfile) {
    val description = profile.characteristics.entries
        .firstOrNull { it.key.equals("Description", ignoreCase = true) }?.value
        ?: profile.characteristics.values.firstOrNull()
    Card(
        Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(6.dp),
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .width(3.dp)
                        .height(14.dp)
                        .background(MaterialTheme.colorScheme.secondary)
                )
                Text(
                    profile.name,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(start = 8.dp),
                )
            }
            if (!description.isNullOrBlank()) {
                Text(
                    description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun KeywordDialog(keyword: String, onDismiss: () -> Unit) {
    val description = KeywordGlossary.lookup(keyword)
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(keyword, fontWeight = FontWeight.Bold) },
        text = {
            Text(
                description ?: "Faction or unit-specific keyword — no universal rule in WH40k 10th Edition.",
                style = MaterialTheme.typography.bodyMedium,
            )
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("OK") } },
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun KeywordChips(keywords: List<String>, onKeywordClick: (String) -> Unit = {}) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        keywords.forEach { keyword ->
            val hasEntry = KeywordGlossary.lookup(keyword) != null
            SuggestionChip(
                onClick = { onKeywordClick(keyword) },
                label = { Text(keyword) },
                icon = if (hasEntry) ({
                    Text("?", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                }) else null,
            )
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
