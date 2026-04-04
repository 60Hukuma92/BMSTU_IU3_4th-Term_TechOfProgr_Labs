package com.bmstu.iu3.automanagement.data

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.bmstu.iu3.automanagement.models.*

/**
 * Менеджер для сохранения и загрузки полного состояния игры для каждого игрока.
 * Использует SharedPreferences и JSON сериализацию через Gson.
 */
class GameSaveManager(private val context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("game_saves", Context.MODE_PRIVATE)

    private val gson: Gson = GsonBuilder()
        .setPrettyPrinting()
        .create()

    fun saveGame(playerName: String) {
        try {
            // Конвертируем текущее состояние в сохраняемый формат
            val saveData = GameStateSaveData(
                playerName = playerName,
                budget = GameState.getBudgetObject().getAmount(),
                ownedComponents = GameState.getOwnedComponents().map { convertComponent(it) },
                assembledCars = GameState.getAssembledCars().map { convertCar(it) },
                hiredEngineers = GameState.getHiredEngineers().map { convertWorker(it, "Engineer") },
                hiredPilots = GameState.getHiredPilots().map { convertWorker(it, "Pilot") },
                jailedPilots = GameState.getJailedPilots().map { convertWorker(it, "Pilot") },
                raceHistory = GameState.getRaceHistory().map { raceList ->
                    raceList.map { race ->
                        RaceResultSaveData(
                            pilotName = race.getTeamName(), // Используем team name вместо pilot name
                            position = race.getPosition(),
                            time = race.getTime(),
                            incidents = race.getIncident()?.getReason() ?: "" // Один инцидент, не список
                        )
                    }
                },
                timestamp = System.currentTimeMillis()
            )

            val json = gson.toJson(saveData)
            sharedPreferences.edit().apply {
                putString("save_$playerName", json)
                putString("current_player", playerName)
                putStringSet(
                    "all_players",
                    (getAllPlayers() + playerName).toSet()
                )
            }.apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun loadGame(playerName: String): Boolean {
        return try {
            if (!gameExists(playerName)) return false

            val json = sharedPreferences.getString("save_$playerName", null) ?: return false
            val saveData = gson.fromJson(json, GameStateSaveData::class.java)

            // Загружаем состояние из сохранённых данных
            GameState.setCurrentPlayer(playerName)
            GameState.setBudget(saveData.budget)
            GameState.clearInventory()

            // Восстанавливаем компоненты
            saveData.ownedComponents.forEach { data ->
                val component = createComponentFromSaveData(data)
                GameState.addComponent(component)
            }

            // Восстанавливаем машины
            saveData.assembledCars.forEach { data ->
                val car = createCarFromSaveData(data)
                GameState.addCar(car)
            }

            // Восстанавливаем инженеров
            saveData.hiredEngineers.forEach { data ->
                val engineer = Engineer().apply {
                    setName(data.name)
                    setSkill(data.skill)
                    setSalary(data.salary)
                }
                GameState.addEngineerDirectly(engineer)
            }

            // Восстанавливаем пилотов
            saveData.hiredPilots.forEach { data ->
                val pilot = createPilotFromSaveData(data)
                GameState.addPilotDirectly(pilot)
            }

            // Восстанавливаем заключённых пилотов
            saveData.jailedPilots.forEach { data ->
                val pilot = createPilotFromSaveData(data)
                GameState.addJailedPilotDirectly(pilot)
            }

            sharedPreferences.edit().putString("current_player", playerName).apply()
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    fun createNewGame(playerName: String): Boolean {
        return try {
            GameState.setCurrentPlayer(playerName)
            GameState.setBudget(10000.0)
            GameState.clearInventory()

            saveGame(playerName)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun gameExists(playerName: String): Boolean {
        return sharedPreferences.contains("save_$playerName")
    }

    fun getAllPlayers(): Set<String> {
        return sharedPreferences.getStringSet("all_players", emptySet()) ?: emptySet()
    }

    fun getCurrentPlayer(): String? {
        val current = sharedPreferences.getString("current_player", null)
        return if (current.isNullOrEmpty()) null else current
    }

    fun deleteGame(playerName: String): Boolean {
        return try {
            sharedPreferences.edit().apply {
                remove("save_$playerName")
                val allPlayers = getAllPlayers().toMutableSet()
                allPlayers.remove(playerName)
                putStringSet("all_players", allPlayers)
                if (getCurrentPlayer() == playerName) {
                    putString("current_player", "")
                }
            }.apply()
            true
        } catch (e: Exception) {
            false
        }
    }

    // === Вспомогательные фу��кции конвертации ===

    private fun convertComponent(component: Component): ComponentSaveData {
        return ComponentSaveData(
            id = component.getId(),
            name = component.getName(),
            price = component.getPrice(),
            performance = component.getPerformance(),
            wear = component.getWear(),
            isDestroyed = component.isDestroyed(),
            type = when (component) {
                is Engine -> "Engine"
                is Gearbox -> "Gearbox"
                is Chassis -> "Chassis"
                is Suspension -> "Suspension"
                is Aerodynamics -> "Aerodynamics"
                is Tyres -> "Tyres"
                else -> "Unknown"
            },
            power = if (component is Engine) component.getPower() else null,
            weight = if (component is Engine) component.getWeight() else null,
            gears = if (component is Gearbox) component.getGears() else null
        )
    }

    private fun convertCar(car: Car): CarSaveData {
        return CarSaveData(
            name = car.getName(),
            performance = car.getPerformance(),
            engine = car.getEngine()?.let { convertComponent(it) },
            gearbox = car.getGearbox()?.let { convertComponent(it) },
            chassis = car.getChassis()?.let { convertComponent(it) },
            suspension = car.getSuspension()?.let { convertComponent(it) },
            aerodynamics = car.getAerodynamics()?.let { convertComponent(it) },
            tyres = car.getTyres()?.let { convertComponent(it) }
        )
    }

    private fun convertWorker(worker: Worker, type: String): WorkerSaveData {
        return WorkerSaveData(
            name = worker.getName(),
            skill = worker.getSkill(),
            salary = worker.getSalary(),
            type = type,
            fineAmount = if (worker is Pilot) worker.getFineAmount() else null,
            fineDeadline = if (worker is Pilot) worker.getFineDeadline() else null,
            jailSentence = if (worker is Pilot) worker.getJailSentence() else null
        )
    }

    private fun createComponentFromSaveData(data: ComponentSaveData): Component {
        return when (data.type) {
            "Engine" -> Engine().apply {
                setId(data.id)
                setName(data.name)
                setPrice(data.price)
                setPerformance(data.performance)
                setWear(data.wear)
                setDestroyed(data.isDestroyed)
                data.power?.let { setPower(it) }
                data.weight?.let { setWeight(it) }
            }
            "Gearbox" -> Gearbox().apply {
                setId(data.id)
                setName(data.name)
                setPrice(data.price)
                setPerformance(data.performance)
                setWear(data.wear)
                setDestroyed(data.isDestroyed)
                data.gears?.let { setGears(it) }
            }
            "Chassis" -> Chassis().apply {
                setId(data.id)
                setName(data.name)
                setPrice(data.price)
                setPerformance(data.performance)
                setWear(data.wear)
                setDestroyed(data.isDestroyed)
            }
            "Suspension" -> Suspension().apply {
                setId(data.id)
                setName(data.name)
                setPrice(data.price)
                setPerformance(data.performance)
                setWear(data.wear)
                setDestroyed(data.isDestroyed)
            }
            "Aerodynamics" -> Aerodynamics().apply {
                setId(data.id)
                setName(data.name)
                setPrice(data.price)
                setPerformance(data.performance)
                setWear(data.wear)
                setDestroyed(data.isDestroyed)
            }
            "Tyres" -> Tyres().apply {
                setId(data.id)
                setName(data.name)
                setPrice(data.price)
                setPerformance(data.performance)
                setWear(data.wear)
                setDestroyed(data.isDestroyed)
            }
            else -> Engine() // Fallback
        }
    }

    private fun createCarFromSaveData(data: CarSaveData): Car {
        return Car().apply {
            setName(data.name)
            setPerformance(data.performance)
            data.engine?.let { setEngine(createComponentFromSaveData(it) as Engine) }
            data.gearbox?.let { setGearbox(createComponentFromSaveData(it) as Gearbox) }
            data.chassis?.let { setChassis(createComponentFromSaveData(it) as Chassis) }
            data.suspension?.let { setSuspension(createComponentFromSaveData(it) as Suspension) }
            data.aerodynamics?.let { setAerodynamics(createComponentFromSaveData(it) as Aerodynamics) }
            data.tyres?.let { setTyres(createComponentFromSaveData(it) as Tyres) }
        }
    }

    private fun createPilotFromSaveData(data: WorkerSaveData): Pilot {
        return Pilot().apply {
            setName(data.name)
            setSkill(data.skill)
            setSalary(data.salary)
            data.fineAmount?.let { setFineAmount(it) }
            data.fineDeadline?.let { setFineDeadline(it) }
            data.jailSentence?.let { setJailSentence(it) }
        }
    }
}
