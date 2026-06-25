package com.lmfd.warboss.ui.unit

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import com.lmfd.warboss.domain.model.UnitSummary
import com.lmfd.warboss.ui.theme.WarbossTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnitListScreen(
    onUnitClick: (unitId: String) -> Unit,
    onBack: () -> Unit,
    viewModel: UnitListViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    UnitListContent(uiState, onUnitClick, onBack)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UnitListContent(
    uiState: UnitListUiState,
    onUnitClick: (String) -> Unit,
    onBack: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Units") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        when (uiState) {
            UnitListUiState.Loading -> Box(
                Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }

            is UnitListUiState.Success -> {
                if (uiState.units.isEmpty()) {
                    Box(
                        Modifier.fillMaxSize().padding(padding),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            "No units in this army.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                } else {
                    LazyColumn(Modifier.fillMaxSize().padding(padding)) {
                        items(uiState.units, key = { it.id }) { unit ->
                            UnitRow(unit, onClick = { onUnitClick(unit.id) })
                            HorizontalDivider()
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun UnitRow(unit: UnitSummary, onClick: () -> Unit) {
    ListItem(
        headlineContent = { Text(unit.name) },
        supportingContent = {
            val qty = if (unit.minQuantity == unit.maxQuantity) {
                "${unit.minQuantity} model${if (unit.minQuantity != 1) "s" else ""}"
            } else {
                "${unit.minQuantity}–${unit.maxQuantity} models"
            }
            Text(qty, style = MaterialTheme.typography.bodySmall)
        },
        trailingContent = {
            androidx.compose.foundation.layout.Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("${unit.points} pts", style = MaterialTheme.typography.labelMedium)
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF0D0D0D)
@Composable
private fun UnitListPreview() {
    WarbossTheme {
        UnitListContent(
            UnitListUiState.Success(listOf(
                UnitSummary("u-1", "f-1", "Boyz", "unit", 95, 10, 20, false),
                UnitSummary("u-2", "f-1", "Warboss", "unit", 85, 1, 1, false),
                UnitSummary("u-3", "f-1", "Deff Dreads", "unit", 110, 1, 3, false),
            )),
            onUnitClick = {},
            onBack = {},
        )
    }
}
