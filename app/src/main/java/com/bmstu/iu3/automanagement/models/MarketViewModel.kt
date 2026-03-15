package com.bmstu.iu3.automanagement.models

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import com.bmstu.iu3.automanagement.data.GameState

class MarketViewModel(application: Application) : AndroidViewModel(application) {
    val availableComponents: List<Component> = GameState.getMarketComponents()

    val availableEngineers: List<Engineer> = GameState.getMarketEngineers()
    val availablePilots: List<Pilot> = GameState.getMarketPilots()

    fun buyComponent(component: Component) {
        if (!GameState.buyComponent(component)) {
            Toast.makeText(getApplication(), "Cannot afford", Toast.LENGTH_SHORT).show()
        }
    }

    fun hireEngineer(engineer: Engineer) {
        if (!GameState.hireEngineer(engineer)) {
            Toast.makeText(getApplication(), "Cannot afford", Toast.LENGTH_SHORT).show()
        }
    }

    fun hirePilot(pilot: Pilot) {
        if (!GameState.hirePilot(pilot)) {
            Toast.makeText(getApplication(), "Cannot afford", Toast.LENGTH_SHORT).show()
        }
    }
}
