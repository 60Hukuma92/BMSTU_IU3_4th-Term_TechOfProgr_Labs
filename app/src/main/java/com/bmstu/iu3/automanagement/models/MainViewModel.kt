package com.bmstu.iu3.automanagement.models

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.State
import androidx.lifecycle.ViewModel
import com.bmstu.iu3.automanagement.data.GameState

class MainViewModel : ViewModel() {
    val budgetDisplay: State<String> = derivedStateOf {
        GameState.getBudgetObject().toString()
    }

    val playerName: androidx.compose.runtime.State<String> = GameState.getCurrentPlayerState()
}
