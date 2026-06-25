package com.lmfd.warboss.ui.unit

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
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
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
    UnitDetailContent(uiState, onBack)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UnitDetailContent(uiState: UnitDetailUiState, onBack: () -> Unit) {
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
        }
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
        // Summary card
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

        // Profiles
        if (detail.profiles.isNotEmpty()) {
            SectionHeader("Stat Blocks")
            detail.profiles.forEach { profile ->
                ProfileCard(profile)
            }
        }

        // Keywords
        if (detail.keywords.isNotEmpty()) {
            SectionHeader("Keywords")
            KeywordChips(detail.keywords)
        }

        // Faction keywords
        if (detail.factionKeywords.isNotEmpty()) {
            SectionHeader("Faction Keywords")
            KeywordChips(detail.factionKeywords)
        }

        // Category links
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

        Spacer(Modifier.height(16.dp))
    }
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
            Text(
                "${profile.name} (${profile.typeName})",
                style = MaterialTheme.typography.titleSmall,
            )
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
            UnitDetailUiState.Success(
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
