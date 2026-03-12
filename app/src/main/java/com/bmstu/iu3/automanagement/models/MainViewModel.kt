package com.bmstu.iu3.automanagement.models

import androidx.lifecycle.ViewModel
import com.bmstu.iu3.automanagement.data.GameState
import java.util.Locale

class MainViewModel : ViewModel() {
    fun getBudgetDisplay(): String {
        return String.format(Locale.US, "%.2f $", GameState.getBudgetObject().getAmount())
    }
}
