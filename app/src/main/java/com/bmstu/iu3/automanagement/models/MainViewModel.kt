package com.bmstu.iu3.automanagement.models

import androidx.lifecycle.ViewModel
import com.bmstu.iu3.automanagement.data.GameState

class MainViewModel : ViewModel() {
    fun getBudgetDisplay(): String {
        return GameState.getBudgetObject().toString()
    }
}
