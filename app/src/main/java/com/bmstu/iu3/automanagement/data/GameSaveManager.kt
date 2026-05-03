package com.bmstu.iu3.automanagement.data

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.bmstu.iu3.automanagement.models.*
import com.bmstu.iu3.automanagement.utils.ComponentComparator
import kotlin.random.Random

class GameSaveManager(context: Context) {
    private val appContext = context.applicationContext
    private val sharedPreferences: SharedPreferences =
        appContext.getSharedPreferences("game_saves", Context.MODE_PRIVATE)
    private val compromisingEvidenceStore = CompromisingEvidenceSecureStore(appContext)

    private val gson: Gson = GsonBuilder()
        .setPrettyPrinting()
        .create()

    fun saveGame(playerName: String) {
        try {
            val saveData = GameStateSaveData(
                playerName = playerName,
                budget = GameState.getBudgetObject().getAmount(),
                ownedComponents = GameState.getOwnedComponents().map { convertComponent(it) },
                assembledCars = GameState.getAssembledCars().map { convertCar(it) },
                hiredEngineers = GameState.getHiredEngineers().map { convertWorker(it, "Engineer") },
                hiredPilots = GameState.getHiredPilots().map { convertWorker(it, "Pilot") },
                jailedPilots = GameState.getJailedPilots().map { convertWorker(it, "Pilot") },
                tracks = GameState.getTracks().map { convertTrack(it) },
                raceHistory = GameState.getRaceHistory().map { raceList ->
                    raceList.map { race ->
                        RaceResultSaveData(
                            pilotName = race.getTeamName(),
                            position = race.getPosition(),
                            time = race.getTime(),
                            incidents = race.getIncident()?.getReason() ?: ""
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
            if (!gameExists(playerName)) {
                false
            } else {
                val json = sharedPreferences.getString("save_$playerName", null)
                if (json.isNullOrEmpty()) {
                    false
                } else {
                    val saveData = gson.fromJson(json, GameStateSaveData::class.java)

                    GameState.setCurrentPlayer(playerName)
                    GameState.setBudget(saveData.budget)
                    GameState.clearInventory()

                    saveData.ownedComponents.forEach { data ->
                        val component = createComponentFromSaveData(data)
                        GameState.addComponent(component)
                    }

                    saveData.assembledCars.forEach { data ->
                        val car = createCarFromSaveData(data)
                        GameState.addCar(car)
                    }

                    saveData.hiredEngineers.forEach { data ->
                        val engineer = Engineer().apply {
                            setName(data.name)
                            setSkill(data.skill)
                            setSalary(data.salary)
                        }
                        GameState.addEngineerDirectly(engineer)
                    }

                    saveData.hiredPilots.forEach { data ->
                        val pilot = createPilotFromSaveData(data)
                        GameState.addPilotDirectly(pilot)
                    }

                    saveData.jailedPilots.forEach { data ->
                        val pilot = createPilotFromSaveData(data)
                        GameState.addJailedPilotDirectly(pilot)
                    }

                    val loadedTracks = saveData.tracks?.map { createTrackFromSaveData(it) }.orEmpty()
                    GameState.setTracks(loadedTracks)

                    sharedPreferences.edit().putString("current_player", playerName).apply()
                    true
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun createNewGame(playerName: String): Boolean {
        return try {
            GameState.setCurrentPlayer(playerName)
            GameState.setBudget(18000.0)
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

    fun awardCompromisingEvidenceToPlayer(playerName: String, pushBackValue: Int = Random.nextInt(5, 16)): CompromisingEvidence? {
        return compromisingEvidenceStore.awardCompromisingEvidence(playerName, pushBackValue)
    }

    fun awardCompromisingEvidenceToCurrentPlayer(pushBackValue: Int = Random.nextInt(5, 16)): CompromisingEvidence? {
        return getCurrentPlayer()?.let { awardCompromisingEvidenceToPlayer(it, pushBackValue) }
    }

    fun getCompromisingEvidenceForPlayer(playerName: String): CompromisingEvidence? = compromisingEvidenceStore.loadCompromisingEvidence(playerName)

    fun consumeCompromisingEvidenceForPlayer(playerName: String): CompromisingEvidence? = compromisingEvidenceStore.consumeCompromisingEvidence(playerName)

    fun hasCompromisingEvidenceForPlayer(playerName: String): Boolean = compromisingEvidenceStore.hasCompromisingEvidence(playerName)

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
            compromisingEvidenceStore.deleteCompromisingEvidence(playerName)
            true
        } catch (_: Exception) {
            false
        }
    }

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
                is MeleeWeapon -> "MeleeWeapon"
                is RangedWeapon -> "RangedWeapon"
                is Weapon -> "Weapon"
            },
            power = if (component is Engine) component.getPower() else null,
            weight = when (component) {
                is Engine -> component.getWeight()
                is Weapon -> component.getWeight()
                else -> null
            },
            gears = if (component is Gearbox) component.getGears() else null,
            componentType = when (component) {
                is Engine -> component.getType()
                is Gearbox -> component.getType()
                is Suspension -> component.getType()
                is Chassis -> component.getSuspensionType()
                else -> null
            },
            maxEngineWeight = if (component is Chassis) component.getMaxEngineWeight() else null,
            suspensionType = if (component is Chassis) component.getSuspensionType() else null,
            grip = if (component is Tyres) component.getGrip() else null,
            accuracy = if (component is Weapon) component.getAccuracy() else null,
            impact = if (component is MeleeWeapon) component.getImpact() else null,
            range = if (component is RangedWeapon) component.getRange() else null
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
            tyres = car.getTyres()?.let { convertComponent(it) },
            meleeWeapon1 = car.getMeleeWeapon1()?.let { convertComponent(it) },
            meleeWeapon2 = car.getMeleeWeapon2()?.let { convertComponent(it) },
            rangedWeapon = car.getRangedWeapon()?.let { convertComponent(it) }
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

    private fun convertTrack(track: Track): TrackSaveData {
        return TrackSaveData(
            name = track.getName(),
            length = track.getLength(),
            straightsRatio = track.getStraightsRatio(),
            cornersRatio = track.getCornersRatio(),
            elevationChange = track.getElevationChange()
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
                setWeight(ComponentComparator.normalizeEngineWeight(data.weight))
                setType(data.componentType ?: "Bolt-On")
            }
            "Gearbox" -> Gearbox().apply {
                setId(data.id)
                setName(data.name)
                setPrice(data.price)
                setPerformance(data.performance)
                setWear(data.wear)
                setDestroyed(data.isDestroyed)
                data.gears?.let { setGears(it) }
                setType(data.componentType ?: "Bolt-On")
            }
            "Chassis" -> Chassis().apply {
                setId(data.id)
                setName(data.name)
                setPrice(data.price)
                setPerformance(data.performance)
                setWear(data.wear)
                setDestroyed(data.isDestroyed)
                setMaxEngineWeight(ComponentComparator.normalizeChassisEngineLimit(data.maxEngineWeight))
                setSuspensionType(data.suspensionType ?: data.componentType ?: "Standard")
            }
            "Suspension" -> Suspension().apply {
                setId(data.id)
                setName(data.name)
                setPrice(data.price)
                setPerformance(data.performance)
                setWear(data.wear)
                setDestroyed(data.isDestroyed)
                setType(data.componentType ?: "Standard")
            }
            "Aerodynamics" -> Aerodynamics().apply {
                setId(data.id)
                setName(data.name)
                setPrice(data.price)
                setPerformance(data.performance)
                setWear(data.wear)
                setDestroyed(data.isDestroyed)
            }
            "MeleeWeapon" -> MeleeWeapon().apply {
                setId(data.id)
                setName(data.name)
                setPrice(data.price)
                setPerformance(data.performance)
                setWear(data.wear)
                setDestroyed(data.isDestroyed)
                setWeight((data.weight ?: 0).coerceAtLeast(1))
                setAccuracy(data.accuracy ?: 0.5)
                setImpact(data.impact ?: 40)
            }
            "RangedWeapon" -> RangedWeapon().apply {
                setId(data.id)
                setName(data.name)
                setPrice(data.price)
                setPerformance(data.performance)
                setWear(data.wear)
                setDestroyed(data.isDestroyed)
                setWeight((data.weight ?: 0).coerceAtLeast(1))
                setAccuracy(data.accuracy ?: 0.5)
                setRange(data.range ?: 300)
            }
            "Weapon" -> MeleeWeapon().apply {
                setId(data.id)
                setName(data.name)
                setPrice(data.price)
                setPerformance(data.performance)
                setWear(data.wear)
                setDestroyed(data.isDestroyed)
                setWeight((data.weight ?: 0).coerceAtLeast(1))
                setAccuracy(data.accuracy ?: 0.5)
                setImpact(data.impact ?: 40)
            }
            "Tyres" -> Tyres().apply {
                setId(data.id)
                setName(data.name)
                setPrice(data.price)
                setPerformance(data.performance)
                setWear(data.wear)
                setDestroyed(data.isDestroyed)
                setGrip(data.grip ?: 1.0)
            }
            else -> Engine()
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
            data.meleeWeapon1?.let { setMeleeWeapon1(createComponentFromSaveData(it) as MeleeWeapon) }
            data.meleeWeapon2?.let { setMeleeWeapon2(createComponentFromSaveData(it) as MeleeWeapon) }
            data.rangedWeapon?.let { setRangedWeapon(createComponentFromSaveData(it) as RangedWeapon) }
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

    private fun createTrackFromSaveData(data: TrackSaveData): Track {
        return Track().apply {
            setName(data.name)
            setLength(data.length)
            setStraightsRatio(data.straightsRatio)
            setCornersRatio(data.cornersRatio)
            setElevationChange(data.elevationChange)
        }
    }

}
