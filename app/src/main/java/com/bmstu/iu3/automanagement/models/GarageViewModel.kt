package com.bmstu.iu3.automanagement.models

import android.app.Application
import android.widget.Toast
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import com.bmstu.iu3.automanagement.data.GameState

class GarageViewModel(application: Application) : AndroidViewModel(application) {

    // Current Car draft
    var selectedEngine = mutableStateOf<Engine?>(null)
    var selectedGearbox = mutableStateOf<Gearbox?>(null)
    var selectedChassis = mutableStateOf<Chassis?>(null)
    var selectedSuspension = mutableStateOf<Suspension?>(null)
    var selectedAero = mutableStateOf<Aerodynamics?>(null)
    var selectedTyres = mutableStateOf<Tyres?>(null)

    var selectedEngineer = mutableStateOf<Engineer?>(null)

    val inventory: List<Component> = GameState.getOwnedComponents()
    val hiredEngineers: List<Engineer> = GameState.getHiredEngineers()

    fun selectComponent(component: Component) {
        when (component) {
            is Engine -> {
                selectedEngine.value = if (selectedEngine.value == component) null else component
            }
            is Gearbox -> {
                selectedGearbox.value = if (selectedGearbox.value == component) null else component
            }
            is Chassis -> {
                selectedChassis.value = if (selectedChassis.value == component) null else component
            }
            is Suspension -> {
                selectedSuspension.value = if (selectedSuspension.value == component) null else component
            }
            is Aerodynamics -> {
                selectedAero.value = if (selectedAero.value == component) null else component
            }
            is Tyres -> {
                selectedTyres.value = if (selectedTyres.value == component) null else component
            }
        }
    }
    
    fun selectEngineer(engineer: Engineer) {
        selectedEngineer.value = if (selectedEngineer.value == engineer) null else engineer
    }

    fun assemble() {
        val engine = selectedEngine.value
        val gearbox = selectedGearbox.value
        val chassis = selectedChassis.value
        val suspension = selectedSuspension.value
        val aerodynamics = selectedAero.value
        val tyres= selectedTyres.value
        val engineer = selectedEngineer.value

        if (engine == null || gearbox == null || chassis == null || suspension == null || aerodynamics == null || tyres == null) {
            Toast.makeText(getApplication(), "Missing components", Toast.LENGTH_SHORT).show()
            return
        }
        
        if (engineer == null) {
            Toast.makeText(getApplication(), "Select an engineer to assemble the car", Toast.LENGTH_SHORT).show()
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
            setName("Formula - ${engineer.getName()}'s Build")
            setEngine(engine)
            setGearbox(gearbox)
            setChassis(chassis)
            setSuspension(suspension)
            setAerodynamics(aerodynamics)
            setTyres(tyres)
            
            val skillBonus = engineer.getSkill() / 100.0
            setPerformance(getTotalPerformance() * (1.0 + skillBonus))
        }

        GameState.addCar(newCar)

        GameState.removeComponentFromInventory(engine)
        GameState.removeComponentFromInventory(gearbox)
        GameState.removeComponentFromInventory(chassis)
        GameState.removeComponentFromInventory(suspension)
        GameState.removeComponentFromInventory(aerodynamics)
        GameState.removeComponentFromInventory(tyres)

        clearSelection()
        
        Toast.makeText(getApplication(), "Car Assembled by ${engineer.getName()}!", Toast.LENGTH_SHORT).show()
    }

    private fun clearSelection() {
        selectedEngine.value = null
        selectedGearbox.value = null
        selectedChassis.value = null
        selectedSuspension.value = null
        selectedAero.value = null
        selectedTyres.value = null
        selectedEngineer.value = null
    }
}
