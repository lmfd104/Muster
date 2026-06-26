package com.lmfd.warboss.ui.game

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.collectAsState
import com.lmfd.warboss.ui.components.TacticalBackground
import com.lmfd.warboss.ui.theme.WarbossTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameTrackerScreen(viewModel: GameTrackerViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsState()
    val saveState by viewModel.saveState.collectAsState()
    GameTrackerContent(
        state = state,
        saveState = saveState,
        onSetRound = viewModel::setRound,
        onReset = viewModel::resetGame,
        onSetMyFaction = viewModel::setMyFaction,
        onSetOpponentFaction = viewModel::setOpponentFaction,
        onSaveGame = { viewModel.saveGame() },
        onDismissSave = viewModel::dismissSaveResult,
        onAdjustPlayerCpGained = viewModel::adjustPlayerCpGained,
        onAdjustPlayerCpSpent = viewModel::adjustPlayerCpSpent,
        onAdjustPlayerPrimary = viewModel::adjustPlayerPrimary,
        onAdjustPlayerSecondary = viewModel::adjustPlayerSecondary,
        onAdjustPlayerExtra = viewModel::adjustPlayerExtra,
        onAdjustOpponentCpGained = viewModel::adjustOpponentCpGained,
        onAdjustOpponentCpSpent = viewModel::adjustOpponentCpSpent,
        onAdjustOpponentPrimary = viewModel::adjustOpponentPrimary,
        onAdjustOpponentSecondary = viewModel::adjustOpponentSecondary,
        onAdjustOpponentExtra = viewModel::adjustOpponentExtra,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GameTrackerContent(
    state: GameTrackerState,
    saveState: SaveGameState = SaveGameState.Idle,
    onSetRound: (Int) -> Unit,
    onReset: () -> Unit,
    onSetMyFaction: (String) -> Unit = {},
    onSetOpponentFaction: (String) -> Unit = {},
    onSaveGame: () -> Unit = {},
    onDismissSave: () -> Unit = {},
    onAdjustPlayerCpGained: (Int) -> Unit,
    onAdjustPlayerCpSpent: (Int) -> Unit,
    onAdjustPlayerPrimary: (Int) -> Unit,
    onAdjustPlayerSecondary: (Int) -> Unit,
    onAdjustPlayerExtra: (Int) -> Unit,
    onAdjustOpponentCpGained: (Int) -> Unit,
    onAdjustOpponentCpSpent: (Int) -> Unit,
    onAdjustOpponentPrimary: (Int) -> Unit,
    onAdjustOpponentSecondary: (Int) -> Unit,
    onAdjustOpponentExtra: (Int) -> Unit,
) {
    var showResetDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Game Tracker", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = onSaveGame) {
                        Icon(Icons.Default.Save, contentDescription = "Save game")
                    }
                    IconButton(onClick = { showResetDialog = true }) {
                        Icon(Icons.Default.Refresh, contentDescription = "New game")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
    ) { padding ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            TacticalBackground()
            Column(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                // Faction name fields
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = state.myFactionName,
                        onValueChange = onSetMyFaction,
                        label = { Text("Your Faction") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                    )
                    OutlinedTextField(
                        value = state.opponentFactionName,
                        onValueChange = onSetOpponentFaction,
                        label = { Text("Opponent") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                    )
                }

                ScoreHeader(state)

                val playerRound = state.playerRounds[state.currentRound]
                val opponentRound = state.opponentRounds[state.currentRound]

                SideScoreSection(
                    label = "YOU",
                    roundScore = playerRound,
                    totalPrimary = state.playerPrimaryTotal,
                    totalSecondary = state.playerSecondaryTotal,
                    accentColor = MaterialTheme.colorScheme.primary,
                    onAdjustCpGained = onAdjustPlayerCpGained,
                    onAdjustCpSpent = onAdjustPlayerCpSpent,
                    onAdjustPrimary = onAdjustPlayerPrimary,
                    onAdjustSecondary = onAdjustPlayerSecondary,
                    onAdjustExtra = onAdjustPlayerExtra,
                )

                SideScoreSection(
                    label = "OPPONENT",
                    roundScore = opponentRound,
                    totalPrimary = state.opponentPrimaryTotal,
                    totalSecondary = state.opponentSecondaryTotal,
                    accentColor = MaterialTheme.colorScheme.error,
                    onAdjustCpGained = onAdjustOpponentCpGained,
                    onAdjustCpSpent = onAdjustOpponentCpSpent,
                    onAdjustPrimary = onAdjustOpponentPrimary,
                    onAdjustSecondary = onAdjustOpponentSecondary,
                    onAdjustExtra = onAdjustOpponentExtra,
                )

                RoundNavigation(
                    currentRound = state.currentRound,
                    onSetRound = onSetRound,
                )

                Button(
                    onClick = onSaveGame,
                    enabled = saveState !is SaveGameState.Saving,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(if (saveState is SaveGameState.Saving) "Saving…" else "End Game & Save")
                }

                Spacer(Modifier.height(8.dp))
            }
        }
    }

    if (saveState is SaveGameState.Saved) {
        AlertDialog(
            onDismissRequest = onDismissSave,
            title = { Text("Game Saved") },
            text = {
                val winner = if (state.playerTotal > state.opponentTotal) "You win!"
                else if (state.opponentTotal > state.playerTotal) "Opponent wins."
                else "Draw!"
                Text("${state.playerTotal} – ${state.opponentTotal}. $winner")
            },
            confirmButton = { TextButton(onClick = onDismissSave) { Text("OK") } },
        )
    }

    if (saveState is SaveGameState.Error) {
        AlertDialog(
            onDismissRequest = onDismissSave,
            title = { Text("Save Failed") },
            text = { Text((saveState as SaveGameState.Error).message) },
            confirmButton = { TextButton(onClick = onDismissSave) { Text("OK") } },
        )
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("New Game?") },
            text = { Text("This will clear all scores for the current game.") },
            confirmButton = {
                TextButton(onClick = {
                    onReset()
                    showResetDialog = false
                }) { Text("New Game") }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) { Text("Cancel") }
            },
        )
    }
}

@Composable
private fun ScoreHeader(state: GameTrackerState) {
    val playerColor = MaterialTheme.colorScheme.primary
    val opponentColor = MaterialTheme.colorScheme.error
    val lead = state.playerTotal - state.opponentTotal

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("YOU", style = MaterialTheme.typography.labelMedium, color = playerColor)
                Text(
                    "${state.playerTotal}",
                    fontSize = 48.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = playerColor,
                )
                Text(
                    "VP",
                    style = MaterialTheme.typography.labelSmall,
                    color = playerColor.copy(alpha = 0.7f),
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                val leadText = when {
                    lead > 0 -> "+$lead"
                    lead < 0 -> "$lead"
                    else -> "="
                }
                val leadColor = when {
                    lead > 0 -> playerColor
                    lead < 0 -> opponentColor
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }
                Box(
                    Modifier
                        .size(48.dp)
                        .background(leadColor.copy(alpha = 0.12f), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(leadText, fontWeight = FontWeight.Bold, color = leadColor, fontSize = 14.sp)
                }
                Spacer(Modifier.height(4.dp))
                Text("LEAD", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("OPPONENT", style = MaterialTheme.typography.labelMedium, color = opponentColor)
                Text(
                    "${state.opponentTotal}",
                    fontSize = 48.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = opponentColor,
                )
                Text(
                    "VP",
                    style = MaterialTheme.typography.labelSmall,
                    color = opponentColor.copy(alpha = 0.7f),
                )
            }
        }
    }
}

@Composable
private fun SideScoreSection(
    label: String,
    roundScore: RoundScore,
    totalPrimary: Int,
    totalSecondary: Int,
    accentColor: Color,
    onAdjustCpGained: (Int) -> Unit,
    onAdjustCpSpent: (Int) -> Unit,
    onAdjustPrimary: (Int) -> Unit,
    onAdjustSecondary: (Int) -> Unit,
    onAdjustExtra: (Int) -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                label,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.ExtraBold,
                color = accentColor,
                modifier = Modifier.padding(bottom = 2.dp),
            )

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CpStepper(
                    label = "CP GAINED",
                    value = roundScore.cpGained,
                    accentColor = accentColor,
                    onDecrement = { onAdjustCpGained(-1) },
                    onIncrement = { onAdjustCpGained(1) },
                    modifier = Modifier.weight(1f),
                )
                CpStepper(
                    label = "CP SPENT",
                    value = roundScore.cpSpent,
                    accentColor = accentColor,
                    onDecrement = { onAdjustCpSpent(-1) },
                    onIncrement = { onAdjustCpSpent(1) },
                    modifier = Modifier.weight(1f),
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant, thickness = 0.5.dp)

            ScoreRow(
                label = "PRIMARY",
                roundValue = roundScore.primaryPts,
                total = totalPrimary,
                totalMax = 50,
                accentColor = accentColor,
                onDecrement = { onAdjustPrimary(-1) },
                onIncrement = { onAdjustPrimary(1) },
            )
            ScoreRow(
                label = "SECONDARY",
                roundValue = roundScore.secondaryPts,
                total = totalSecondary,
                totalMax = 40,
                accentColor = accentColor,
                onDecrement = { onAdjustSecondary(-1) },
                onIncrement = { onAdjustSecondary(1) },
            )
            ScoreRow(
                label = "EXTRA",
                roundValue = roundScore.extraPts,
                total = null,
                totalMax = null,
                accentColor = accentColor,
                onDecrement = { onAdjustExtra(-1) },
                onIncrement = { onAdjustExtra(1) },
            )
        }
    }
}

@Composable
private fun CpStepper(
    label: String,
    value: Int,
    accentColor: Color,
    onDecrement: () -> Unit,
    onIncrement: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = accentColor.copy(alpha = 0.08f)),
        shape = RoundedCornerShape(8.dp),
        modifier = modifier,
    ) {
        Column(
            Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(verticalAlignment = Alignment.CenterVertically) {
                StepButton(label = "−", onClick = onDecrement, color = accentColor)
                Text(
                    "$value",
                    modifier = Modifier.width(32.dp),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyLarge,
                    color = accentColor,
                )
                StepButton(label = "+", onClick = onIncrement, color = accentColor)
            }
        }
    }
}

@Composable
private fun ScoreRow(
    label: String,
    roundValue: Int,
    total: Int?,
    totalMax: Int?,
    accentColor: Color,
    onDecrement: () -> Unit,
    onIncrement: () -> Unit,
) {
    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (total != null && totalMax != null) {
                Text(
                    "$total / $totalMax total",
                    style = MaterialTheme.typography.labelSmall,
                    color = accentColor.copy(alpha = 0.7f),
                )
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            StepButton(label = "−", onClick = onDecrement, color = accentColor)
            Text(
                "$roundValue",
                modifier = Modifier.width(40.dp),
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 20.sp,
                color = accentColor,
            )
            StepButton(label = "+", onClick = onIncrement, color = accentColor)
        }
    }
}

@Composable
private fun StepButton(label: String, onClick: () -> Unit, color: Color) {
    IconButton(onClick = onClick, modifier = Modifier.size(40.dp)) {
        Box(
            Modifier
                .size(32.dp)
                .background(color.copy(alpha = 0.15f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(label, fontWeight = FontWeight.Bold, color = color, fontSize = 18.sp)
        }
    }
}

@Composable
private fun RoundNavigation(currentRound: Int, onSetRound: (Int) -> Unit) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(
                onClick = { onSetRound(currentRound - 1) },
                enabled = currentRound > 0,
            ) {
                Text(
                    "‹",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (currentRound > 0) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "ROUND ${currentRound + 1}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    repeat(5) { i ->
                        Box(
                            Modifier
                                .size(6.dp)
                                .background(
                                    if (i == currentRound) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                                    CircleShape,
                                )
                        )
                    }
                }
            }

            IconButton(
                onClick = { onSetRound(currentRound + 1) },
                enabled = currentRound < 4,
            ) {
                Text(
                    "›",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (currentRound < 4) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                )
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0D0D0D)
@Composable
private fun GameTrackerPreview() {
    WarbossTheme {
        GameTrackerContent(
            state = GameTrackerState(
                playerRounds = listOf(
                    RoundScore(cpGained = 3, cpSpent = 1, primaryPts = 8, secondaryPts = 6, extraPts = 0),
                    RoundScore(cpGained = 2, cpSpent = 2, primaryPts = 7, secondaryPts = 5, extraPts = 2),
                    RoundScore(), RoundScore(), RoundScore(),
                ),
                opponentRounds = listOf(
                    RoundScore(cpGained = 2, cpSpent = 0, primaryPts = 6, secondaryPts = 4, extraPts = 0),
                    RoundScore(cpGained = 3, cpSpent = 1, primaryPts = 5, secondaryPts = 6, extraPts = 0),
                    RoundScore(), RoundScore(), RoundScore(),
                ),
                currentRound = 1,
            ),
            onSetRound = {},
            onReset = {},
            onAdjustPlayerCpGained = {},
            onAdjustPlayerCpSpent = {},
            onAdjustPlayerPrimary = {},
            onAdjustPlayerSecondary = {},
            onAdjustPlayerExtra = {},
            onAdjustOpponentCpGained = {},
            onAdjustOpponentCpSpent = {},
            onAdjustOpponentPrimary = {},
            onAdjustOpponentSecondary = {},
            onAdjustOpponentExtra = {},
        )
    }
}
