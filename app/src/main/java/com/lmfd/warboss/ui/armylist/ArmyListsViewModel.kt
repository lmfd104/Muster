package com.lmfd.warboss.ui.armylist

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lmfd.warboss.data.billing.BillingManager
import com.lmfd.warboss.domain.usecase.CreateArmyListUseCase
import com.lmfd.warboss.domain.usecase.GetArmyListsUseCase
import com.lmfd.warboss.domain.usecase.GetFactionsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ArmyListsViewModel @Inject constructor(
    getArmyListsUseCase: GetArmyListsUseCase,
    getFactions: GetFactionsUseCase,
    private val createArmyListUseCase: CreateArmyListUseCase,
    private val billingManager: BillingManager,
) : ViewModel() {

    val uiState: StateFlow<ArmyListsUiState> = getArmyListsUseCase()
        .map { ArmyListsUiState.Success(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ArmyListsUiState.Loading)

    val factions = getFactions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val isPro: StateFlow<Boolean> = billingManager.isPro
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    fun createList(name: String, factionId: String, factionName: String, pointsLimit: Int) {
        viewModelScope.launch {
            createArmyListUseCase(name, factionId, factionName, pointsLimit)
        }
    }

    fun launchBillingFlow(activity: Activity) {
        billingManager.launchBillingFlow(activity)
    }
}
