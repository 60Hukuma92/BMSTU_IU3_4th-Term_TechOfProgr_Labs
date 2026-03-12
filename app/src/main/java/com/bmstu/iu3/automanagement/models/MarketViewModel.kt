package com.bmstu.iu3.automanagement.models

import androidx.lifecycle.ViewModel
import com.bmstu.iu3.automanagement.data.GameState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class MarketViewModel : ViewModel() {
    private val _availableComponents = MutableStateFlow<List<Component>>(
        listOf(
            Engine().apply {
                setName("V6 Basic")
                setPrice(2000.0)
                setPower(500)
                setType("V6")
            },
            Engine().apply {
                setName("V8 Sport")
                setPrice(5000.0)
                setPower(750)
                setType("V8")
            }
        )
    )
    val availableComponents: StateFlow<List<Component>> = _availableComponents.asStateFlow()

    fun buyComponent(component: Component) {
        if (GameState.spendMoney(component.getPrice())) {
            // Logic to add to inventory would go here
        }
    }
}
