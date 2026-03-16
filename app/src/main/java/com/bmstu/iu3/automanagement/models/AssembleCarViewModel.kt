package com.bmstu.iu3.automanagement.models

import android.app.Application
import android.widget.Toast
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import com.bmstu.iu3.automanagement.data.GameState

class AssembleCarViewModel(application: Application) : AndroidViewModel(application) {

    // Current Car draft
    var selectedEngine = mutableStateOf<Engine?>(null)
    var selectedGearbox = mutableStateOf<Gearbox?>(null)
    var selectedChassis = mutableStateOf<Chassis?>(null)
    var selectedSuspension = mutableStateOf<Suspension?>(null)
    var selectedAero = mutableStateOf<Aerodynamics?>(null)
    var selectedTyres = mutableStateOf<Tyres?>(null)

    val inventory: List<Component> = GameState.getOwnedComponents()

    fun selectComponent(component: Component) {
        when (component) {
            is Engine -> selectedEngine.value = component
            is Gearbox -> selectedGearbox.value = component
            is Chassis -> selectedChassis.value = component
            is Suspension -> selectedSuspension.value = component
            is Aerodynamics -> selectedAero.value = component
            is Tyres -> selectedTyres.value = component
        }
    }

    fun assemble() {
        val engine = selectedEngine.value
        val gearbox = selectedGearbox.value
        val chassis = selectedChassis.value
        val suspension = selectedSuspension.value
        val aerodynamics = selectedAero.value
        val tyres= selectedTyres.value

        if (engine == null || gearbox == null || chassis == null || suspension == null || aerodynamics == null || tyres == null) {
            Toast.makeText(getApplication(), "Missing components", Toast.LENGTH_SHORT).show()
            return
        }

        if (engine.getType() != gearbox.getType()) {
            Toast.makeText(getApplication(), "Engine and Gearbox mismatch (${engine.getType()} vs ${gearbox.getType()})", Toast.LENGTH_SHORT).show()
            return
        }

        if (engine.getWeight() > chassis.getMaxEngineWeight()) {
            Toast.makeText(getApplication(), "Engine too heavy for this chassis", Toast.LENGTH_SHORT).show()
            return
        }

        if (suspension.getType() != chassis.getSuspensionType()) {
            Toast.makeText(getApplication(), "Suspension not compatible with chassis (got ${suspension.getType()} required ${chassis.getSuspensionType()})", Toast.LENGTH_SHORT).show()
            return
        }

        val newCar = Car().apply {
            setName("My Custom Formula")
            setEngine(engine)
            setGearbox(gearbox)
            setChassis(chassis)
            setSuspension(suspension)
            setAerodynamics(aerodynamics)
            setTyres(tyres)
        }

        GameState.addCar(newCar)

        GameState.removeComponentFromInventory(engine)
        GameState.removeComponentFromInventory(gearbox)
        GameState.removeComponentFromInventory(chassis)
        GameState.removeComponentFromInventory(suspension)
        GameState.removeComponentFromInventory(aerodynamics)
        GameState.removeComponentFromInventory(tyres)

        clearSelection()
        
        Toast.makeText(getApplication(), "Car Assembled!", Toast.LENGTH_SHORT).show()
    }

    private fun clearSelection() {
        selectedEngine.value = null
        selectedGearbox.value = null
        selectedChassis.value = null
        selectedSuspension.value = null
        selectedAero.value = null
        selectedTyres.value = null
    }
}
