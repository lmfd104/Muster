package com.lmfd.warboss.ui.armylist

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import android.content.Intent
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lmfd.warboss.domain.model.ArmyList
import com.lmfd.warboss.domain.model.ArmyListEntry
import com.lmfd.warboss.domain.model.ListAnalysis
import com.lmfd.warboss.ui.theme.WarbossTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArmyListDetailScreen(
    onBack: () -> Unit,
    viewModel: ArmyListDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val isPro by viewModel.isPro.collectAsState()
    val analysisState by viewModel.analysisState.collectAsState()

    ArmyListDetailContent(
        uiState = uiState,
        isPro = isPro,
        analysisState = analysisState,
        onBack = onBack,
        onRemoveEntry = viewModel::removeEntry,
        onIncrementQty = viewModel::incrementQuantity,
        onDecrementQty = viewModel::decrementQuantity,
        onRateList = viewModel::analyzeList,
        onDismissAnalysis = viewModel::dismissAnalysis,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ArmyListDetailContent(
    uiState: ArmyListDetailUiState,
    isPro: Boolean,
    analysisState: AiAnalysisUiState,
    onBack: () -> Unit,
    onRemoveEntry: (String) -> Unit = {},
    onIncrementQty: (entryId: String, currentQty: Int) -> Unit = { _, _ -> },
    onDecrementQty: (entryId: String, currentQty: Int) -> Unit = { _, _ -> },
    onRateList: () -> Unit = {},
    onDismissAnalysis: () -> Unit = {},
) {
    val title = when (uiState) {
        is ArmyListDetailUiState.Success -> uiState.list.name
        else -> "Army List"
    }

    val hasEntries = uiState is ArmyListDetailUiState.Success && uiState.entries.isNotEmpty()
    var showUpgradeDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    if (showUpgradeDialog) {
        AlertDialog(
            onDismissRequest = { showUpgradeDialog = false },
            title = { Text("Muster Pro") },
            text = { Text("AI army analysis is a Pro feature. Upgrade to get competitive ratings, meta insights, and unit swap suggestions for every list.") },
            confirmButton = { TextButton(onClick = { showUpgradeDialog = false }) { Text("OK") } },
        )
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val showSheet = analysisState != AiAnalysisUiState.Idle

    if (showSheet) {
        ModalBottomSheet(
            onDismissRequest = onDismissAnalysis,
            sheetState = sheetState,
        ) {
            AnalysisSheetContent(
                state = analysisState,
                onRetry = onRateList,
                onDismiss = onDismissAnalysis,
            )
        }
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
                actions = {
                    if (hasEntries) {
                        val successState = uiState as? ArmyListDetailUiState.Success
                        if (successState != null) {
                            IconButton(onClick = {
                                val text = formatShareText(successState.list, successState.entries)
                                val intent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, text)
                                    putExtra(Intent.EXTRA_SUBJECT, successState.list.name)
                                }
                                context.startActivity(Intent.createChooser(intent, "Share army list"))
                            }) {
                                Icon(
                                    Icons.Default.Share,
                                    contentDescription = "Share list",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                        IconButton(onClick = {
                            if (isPro) onRateList() else showUpgradeDialog = true
                        }) {
                            Icon(
                                Icons.Default.AutoAwesome,
                                contentDescription = "Rate my list",
                                tint = if (isPro) Color(0xFFFFD700) else MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
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
                isPro = isPro,
                onRemoveEntry = onRemoveEntry,
                onIncrementQty = onIncrementQty,
                onDecrementQty = onDecrementQty,
                onRateList = { if (isPro) onRateList() else showUpgradeDialog = true },
                modifier = Modifier.padding(padding),
            )
        }
    }
}

@Composable
private fun ArmyListDetailBody(
    list: ArmyList,
    entries: List<ArmyListEntry>,
    isPro: Boolean,
    onRemoveEntry: (String) -> Unit,
    onIncrementQty: (entryId: String, currentQty: Int) -> Unit,
    onDecrementQty: (entryId: String, currentQty: Int) -> Unit,
    onRateList: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(modifier.fillMaxSize()) {
        item {
            PointsHeader(list = list, modifier = Modifier.padding(16.dp))
        }

        item {
            RateMyListBanner(isPro = isPro, onClick = onRateList, modifier = Modifier.padding(horizontal = 16.dp))
            Spacer(Modifier.height(8.dp))
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
                EntryRow(
                    entry = entry,
                    onRemove = { onRemoveEntry(entry.id) },
                    onIncrement = { onIncrementQty(entry.id, entry.quantity) },
                    onDecrement = { onDecrementQty(entry.id, entry.quantity) },
                )
            }
        }
    }
}

@Composable
private fun RateMyListBanner(isPro: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val gold = Color(0xFFFFD700)
    val crimson = MaterialTheme.colorScheme.primary
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (isPro) gold else crimson.copy(alpha = 0.4f)),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = if (isPro) gold else MaterialTheme.colorScheme.onSurfaceVariant,
        ),
    ) {
        Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(8.dp))
        Text(
            if (isPro) "Rate My List" else "Rate My List  •  Pro",
            style = MaterialTheme.typography.labelLarge,
        )
    }
}

@Composable
private fun AnalysisSheetContent(
    state: AiAnalysisUiState,
    onRetry: () -> Unit,
    onDismiss: () -> Unit,
) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 24.dp, bottom = 40.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        when (state) {
            AiAnalysisUiState.Idle -> Unit

            AiAnalysisUiState.Loading -> {
                Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        CircularProgressIndicator(color = Color(0xFFFFD700))
                        Text("Analysing your list...", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            is AiAnalysisUiState.Error -> {
                Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(40.dp))
                        Text(state.message, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(onClick = onDismiss) { Text("Cancel") }
                            Button(onClick = onRetry) { Text("Retry") }
                        }
                    }
                }
            }

            is AiAnalysisUiState.Success -> AnalysisResults(state.analysis)
        }
    }
}

@Composable
private fun AnalysisResults(analysis: ListAnalysis) {
    val ratingColor = when {
        analysis.rating >= 8 -> Color(0xFF4CAF50)
        analysis.rating >= 5 -> Color(0xFFFFD700)
        else -> MaterialTheme.colorScheme.error
    }

    // Header: rating + tier
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column {
            Text(
                "${analysis.rating}/10",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = ratingColor,
            )
            Text("AI Rating", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Box(
            modifier = Modifier
                .background(ratingColor.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                .padding(horizontal = 12.dp, vertical = 6.dp),
        ) {
            Text(
                analysis.tier.uppercase(),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = ratingColor,
                letterSpacing = androidx.compose.ui.unit.TextUnit(1.5f, androidx.compose.ui.unit.TextUnitType.Sp),
            )
        }
    }

    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

    AnalysisSection(
        label = "STRENGTHS",
        items = analysis.strengths,
        icon = { Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF4CAF50), modifier = Modifier.size(16.dp)) },
    )

    AnalysisSection(
        label = "WEAKNESSES",
        items = analysis.weaknesses,
        icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFFF9800), modifier = Modifier.size(16.dp)) },
    )

    AnalysisSection(
        label = "SUGGESTIONS",
        items = analysis.suggestions,
        icon = { Icon(Icons.Default.Lightbulb, contentDescription = null, tint = Color(0xFFFFD700), modifier = Modifier.size(16.dp)) },
    )

    if (analysis.caveat.isNotBlank()) {
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        ) {
            Row(Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("⚠", style = MaterialTheme.typography.bodySmall)
                Text(analysis.caveat, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun AnalysisSection(
    label: String,
    items: List<String>,
    icon: @Composable () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            letterSpacing = androidx.compose.ui.unit.TextUnit(1.5f, androidx.compose.ui.unit.TextUnitType.Sp),
        )
        items.forEach { item ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.Top) {
                Box(Modifier.padding(top = 2.dp)) { icon() }
                Text(item, style = MaterialTheme.typography.bodyMedium)
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
private fun EntryRow(
    entry: ArmyListEntry,
    onRemove: () -> Unit,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 4.dp, top = 6.dp, bottom = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(entry.unitName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            Text(
                "${entry.unitPoints * entry.quantity} pts",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onDecrement, enabled = entry.quantity > 1) {
                Text(
                    "−",
                    style = MaterialTheme.typography.titleMedium,
                    color = if (entry.quantity > 1) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                )
            }
            Text(
                "${entry.quantity}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.width(24.dp),
                textAlign = TextAlign.Center,
            )
            IconButton(onClick = onIncrement) {
                Text("+", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            }
            IconButton(onClick = onRemove) {
                Icon(Icons.Default.Delete, contentDescription = "Remove", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

private fun formatShareText(list: ArmyList, entries: List<ArmyListEntry>): String {
    val sb = StringBuilder()
    sb.appendLine("== ${list.name} ==")
    sb.appendLine("Faction: ${list.factionName}")
    val ptsLine = if (list.pointsLimit > 0) "${list.pointsTotal} / ${list.pointsLimit} pts"
    else "${list.pointsTotal} pts"
    sb.appendLine("Points: $ptsLine")
    sb.appendLine()
    sb.appendLine("UNITS (${entries.size}):")
    entries.forEach { e ->
        val total = e.unitPoints * e.quantity
        val each = if (e.quantity > 1) " (${e.unitPoints} pts each)" else ""
        sb.appendLine("• ${e.quantity}× ${e.unitName} — $total pts$each")
    }
    sb.appendLine()
    sb.appendLine("Built with Muster")
    return sb.toString().trimEnd()
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
            isPro = true,
            analysisState = AiAnalysisUiState.Idle,
            onBack = {},
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0D0D0D)
@Composable
private fun AnalysisResultsPreview() {
    WarbossTheme {
        Column(Modifier.padding(24.dp)) {
            AnalysisResults(
                ListAnalysis(
                    rating = 7,
                    tier = "Tournament",
                    strengths = listOf(
                        "Mortarion provides excellent aura coverage for Disgustingly Resilient",
                        "Strong mid-board control with Blightlord screening",
                        "Resilient objective holders with Plague Marines",
                    ),
                    weaknesses = listOf(
                        "Limited ranged anti-tank — no dedicated AT outside melee range",
                        "Slow movement profile hurts early objective scoring",
                        "Few command point re-rolls relative to army size",
                    ),
                    suggestions = listOf(
                        "Swap one Chaos Spawn unit for a Foetid Bloat-drone for ranged punch",
                        "Add a Noxious Blightbringer to boost charge reliability",
                        "Consider a second Plague Marine squad for redundancy on objectives",
                    ),
                    caveat = "Meta knowledge may be slightly outdated — verify points against the latest GW update.",
                )
            )
        }
    }
}
