package com.bmstu.iu3.automanagement.models

import android.app.Application
import android.widget.Toast
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import com.bmstu.iu3.automanagement.data.GameState
import com.bmstu.iu3.automanagement.utils.ComponentComparator

class GarageViewModel(application: Application) : AndroidViewModel(application) {

    // Current Car draft
    var selectedEngine = mutableStateOf<Engine?>(null)
    var selectedGearbox = mutableStateOf<Gearbox?>(null)
    var selectedChassis = mutableStateOf<Chassis?>(null)
    var selectedSuspension = mutableStateOf<Suspension?>(null)
    var selectedAero = mutableStateOf<Aerodynamics?>(null)
    var selectedTyres = mutableStateOf<Tyres?>(null)
    var selectedMeleeWeapon1 = mutableStateOf<MeleeWeapon?>(null)
    var selectedMeleeWeapon2 = mutableStateOf<MeleeWeapon?>(null)
    var selectedRangedWeapon = mutableStateOf<RangedWeapon?>(null)

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
            is MeleeWeapon -> {
                when {
                    selectedMeleeWeapon1.value == component -> selectedMeleeWeapon1.value = null
                    selectedMeleeWeapon2.value == component -> selectedMeleeWeapon2.value = null
                    selectedMeleeWeapon1.value == null -> selectedMeleeWeapon1.value = component
                    selectedMeleeWeapon2.value == null -> selectedMeleeWeapon2.value = component
                    else -> selectedMeleeWeapon2.value = component
                }
            }
            is RangedWeapon -> {
                selectedRangedWeapon.value = if (selectedRangedWeapon.value == component) null else component
            }
            else -> {
                // Fallback for base Weapon/other future component subtypes.
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
        val meleeWeapon1 = selectedMeleeWeapon1.value
        val meleeWeapon2 = selectedMeleeWeapon2.value
        val rangedWeapon = selectedRangedWeapon.value
        val engineer = selectedEngineer.value

        if (engine == null || gearbox == null || chassis == null || suspension == null || aerodynamics == null || tyres == null) {
            Toast.makeText(getApplication(), "Missing components", Toast.LENGTH_SHORT).show()
            return
        }
        
        if (engineer == null) {
            Toast.makeText(getApplication(), "Select an engineer to assemble the car", Toast.LENGTH_SHORT).show()
            return
        }

        val baseValidation = ComponentComparator.validateAssembly(engine, gearbox, chassis, suspension)
        if (!baseValidation.isValid) {
            Toast.makeText(getApplication(), baseValidation.message ?: "Invalid build", Toast.LENGTH_SHORT).show()
            return
        }

        val selectedWeapons = listOfNotNull(meleeWeapon1, meleeWeapon2, rangedWeapon)
        var currentWeaponWeight = 0
        selectedWeapons.forEach { weapon ->
            val weaponValidation = ComponentComparator.validateWeaponLoad(chassis, engine, currentWeaponWeight, weapon)
            if (!weaponValidation.isValid) {
                Toast.makeText(getApplication(), weaponValidation.message ?: "Invalid weapon load", Toast.LENGTH_SHORT).show()
                return
            }
            currentWeaponWeight += weapon.getWeight()
        }

        val newCar = Car().apply {
            setName("Formula - ${engineer.getName()}'s Build")
            setEngine(engine)
            setGearbox(gearbox)
            setChassis(chassis)
            setSuspension(suspension)
            setAerodynamics(aerodynamics)
            setTyres(tyres)
            setMeleeWeapon1(meleeWeapon1)
            setMeleeWeapon2(meleeWeapon2)
            setRangedWeapon(rangedWeapon)

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
        meleeWeapon1?.let { GameState.removeComponentFromInventory(it) }
        meleeWeapon2?.let { GameState.removeComponentFromInventory(it) }
        rangedWeapon?.let { GameState.removeComponentFromInventory(it) }

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
        selectedMeleeWeapon1.value = null
        selectedMeleeWeapon2.value = null
        selectedRangedWeapon.value = null
        selectedEngineer.value = null
    }
}
