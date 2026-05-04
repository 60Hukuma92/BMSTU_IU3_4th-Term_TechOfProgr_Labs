package com.bmstu.iu3.automanagement.survival

import com.bmstu.iu3.automanagement.models.Car
import com.bmstu.iu3.automanagement.models.MeleeWeapon
import com.bmstu.iu3.automanagement.models.OpponentTeam
import com.bmstu.iu3.automanagement.models.Pilot
import com.bmstu.iu3.automanagement.models.RaceResult
import com.bmstu.iu3.automanagement.models.RangedWeapon
import com.bmstu.iu3.automanagement.models.Track
import com.bmstu.iu3.automanagement.models.Weather
import com.bmstu.iu3.automanagement.models.Weapon
import kotlin.math.max
import kotlin.random.Random
import java.util.Locale

interface SurvivalRandom {
    fun nextDouble(): Double
}

class DefaultSurvivalRandom(
    private val random: Random = Random.Default
) : SurvivalRandom {
    override fun nextDouble(): Double = random.nextDouble()
}

data class SurvivalCompetitorState(
    val name: String,
    val isPlayer: Boolean,
    val car: Car,
    val pilot: Pilot,
    var progress: Double = 0.0,
    var alive: Boolean = true
)

data class SurvivalTurnResult(
    val logs: List<String>,
    val finished: Boolean,
    val winnerName: String? = null
)

class SurvivalRaceEngine(
    private val track: Track,
    private val weather: Weather,
    playerCar: Car,
    playerPilot: Pilot,
    opponents: List<OpponentTeam>,
    private val random: SurvivalRandom = DefaultSurvivalRandom()
) {
    private val finishDistance = max(track.getLength() * 150.0, 300.0)
    private val turnLogs = mutableListOf<String>()
    private val competitors = mutableListOf<SurvivalCompetitorState>()

    var finished: Boolean = false
        private set

    var winnerName: String? = null
        private set

    var turnNumber: Int = 1
        private set

    init {
        competitors.add(
            SurvivalCompetitorState(
                name = "YOU",
                isPlayer = true,
                car = playerCar,
                pilot = playerPilot,
                progress = 0.0,
                alive = true
            )
        )

        opponents.forEach { team ->
            val car = team.getCar()
            val pilot = team.getPilot()
            if (car != null && pilot != null) {
                competitors.add(
                    SurvivalCompetitorState(
                        name = team.getName(),
                        isPlayer = false,
                        car = car,
                        pilot = pilot,
                        progress = 0.0,
                        alive = true
                    )
                )
            }
        }

        competitors.sortByDescending { baseCompetitorStrength(it) }
        turnLogs.add("Survival race started on ${track.getName()} (${String.format(Locale.US, "%.1f", finishDistance)} m)")
    }

    fun getStandings(): List<SurvivalCompetitorState> =
        competitors.filter { it.alive }.sortedByDescending { it.progress }

    fun getTurnLogs(): List<String> = turnLogs.toList()

    fun getPlayerState(): SurvivalCompetitorState = competitors.first { it.isPlayer }

    fun getAliveOpponentsCount(): Int = competitors.count { it.alive && !it.isPlayer }

    fun canPlayerAttack(targetIndex: Int): Boolean {
        val standings = getStandings()
        if (targetIndex !in standings.indices) return false
        val target = standings[targetIndex]
        if (target.isPlayer || !target.alive) return false
        return canAttackTarget(getPlayerState(), target)
    }

    fun performPlayerAttack(targetIndex: Int): SurvivalTurnResult {
        if (finished) return snapshotResult()

        val standings = getStandings()
        if (targetIndex !in standings.indices) {
            val names = standings.mapIndexed { i, s -> "$i:${s.name}${if (s.isPlayer) "(YOU)" else ""}${if (!s.alive) "[dead]" else ""}" }
            turnLogs.add("Invalid target selected: index=$targetIndex, standings=${names.joinToString(", ")}")
            return appendAndAdvance("Invalid target selected.")
        }

        val target = standings[targetIndex]
        if (target.isPlayer || !target.alive) {
            val names = standings.mapIndexed { i, s -> "$i:${s.name}${if (s.isPlayer) "(YOU)" else ""}${if (!s.alive) "[dead]" else ""}" }
            turnLogs.add("Invalid target chosen: index=$targetIndex, target=${target.name}, isPlayer=${target.isPlayer}, alive=${target.alive}, standings=${names.joinToString(", ")}")
            return appendAndAdvance("Invalid target selected.")
        }

        val player = getPlayerState()
        val weapons = playerWeapons(player.car)
        if (weapons.isEmpty()) {
            return appendAndAdvance("No weapons installed.")
        }

        var attacksResolved = 0
        var hits = 0
        weapons.forEach { weapon ->
            if (!target.alive) return@forEach
            if (weapon is MeleeWeapon && !isAdjacent(target)) {
                turnLogs.add("${weapon.getName()} cannot reach ${target.name}.")
                attacksResolved++
                return@forEach
            }

            attacksResolved++
            if (attemptHit(player, target, weapon, adjacentBonus = weapon is MeleeWeapon && isAdjacent(target))) {
                target.alive = false
                hits++
                turnLogs.add("${player.name} destroyed ${target.name} with ${weapon.getName()}.")
            } else {
                turnLogs.add("${player.name} missed ${target.name} with ${weapon.getName()}.")
            }
        }

        if (attacksResolved == 0) {
            turnLogs.add("No valid attacks were possible.")
        } else if (hits == 0) {
            turnLogs.add("Attack phase ended without a hit.")
        }

        return resolveTurn("attack")
    }

    fun performPlayerCompromisingEvidence(targetIndex: Int, pushBackValue: Int): SurvivalTurnResult {
        if (finished) return snapshotResult()

        val standings = getStandings()
        if (targetIndex !in standings.indices) {
            turnLogs.add("Invalid target selected for compromising evidence.")
            return appendAndAdvance("Invalid target selected.")
        }

        val target = standings[targetIndex]
        if (target.isPlayer || !target.alive) {
            turnLogs.add("Invalid target selected for compromising evidence: ${target.name}.")
            return appendAndAdvance("Invalid target selected.")
        }

        val actualPushBack = pushBackValue.coerceAtLeast(0).toDouble()
        target.progress = max(0.0, target.progress - actualPushBack)
        turnLogs.add("YOU used compromising evidence against ${target.name}, pushing them back by ${formatProgress(actualPushBack)}.")

        return resolveTurn("compromising evidence")
    }

    fun performPlayerOvertake(): SurvivalTurnResult {
        if (finished) return snapshotResult()

        val player = getPlayerState()
        val standings = getStandings()
        val playerIndex = standings.indexOfFirst { it.isPlayer }
        if (playerIndex < 0) return snapshotResult()

        if (playerIndex == 0) {
            val bonus = calculateProgressBonus(player)
            player.progress += bonus
            turnLogs.add("${player.name} is already in front and gains extra momentum (+${formatProgress(bonus)}).")
        } else {
            val target = standings[playerIndex - 1]
            val chance = calculateOvertakeChance(player, target)
            if (random.nextDouble() <= chance) {
                val gained = max(5.0, calculateProgressBonus(player) * 1.2)
                player.progress += gained
                target.progress = max(0.0, target.progress - 2.0)
                turnLogs.add("${player.name} overtook ${target.name} (+${formatProgress(gained)}).")
            } else {
                val consolation = calculateProgressBonus(player) * 0.45
                player.progress += consolation
                turnLogs.add("${player.name} failed to overtake ${target.name}.")
            }
        }

        return resolveTurn("overtake")
    }

    fun buildResults(): List<RaceResult> {
        val standings = competitors.sortedWith(
            compareByDescending<SurvivalCompetitorState> { it.alive }
                .thenByDescending { it.progress }
        )
        val results = mutableListOf<RaceResult>()
        standings.forEachIndexed { index, competitor ->
            results.add(
                RaceResult().apply {
                    setTeamName(competitor.name)
                    setPosition(index + 1)
                    setTime(if (competitor.alive) max(0.0, finishDistance - competitor.progress) else 999999.0)
                }
            )
        }
        return results
    }

    private fun resolveTurn(actionName: String): SurvivalTurnResult {
        if (finished) return snapshotResult()

        performBotActions()
        advanceAllCompetitors()
        turnNumber += 1
        resolveFinishCondition()
        turnLogs.add("Turn ${turnNumber - 1} completed after $actionName.")
        return snapshotResult()
    }

    private fun performBotActions() {
        val snapshot = getStandings()
        snapshot.filter { !it.isPlayer }.forEach { bot ->
            if (!bot.alive || finished) return@forEach

            val player = getPlayerState()
            val canAttackPlayer = player.alive && canAttackTarget(bot, player)
            val weapons = playerWeapons(bot.car)

            if (weapons.isNotEmpty() && canAttackPlayer && random.nextDouble() < botAttackBias(bot)) {
                val weapon = weapons.first()
                if (attemptHit(bot, player, weapon, adjacentBonus = weapon is MeleeWeapon && isAdjacent(bot, player))) {
                    player.alive = false
                    turnLogs.add("${bot.name} eliminated YOU with ${weapon.getName()}.")
                    return
                }
                turnLogs.add("${bot.name} attacked YOU with ${weapon.getName()} but missed.")
            } else {
                attemptBotOvertake(bot)
            }
        }
    }

    private fun attemptBotOvertake(bot: SurvivalCompetitorState) {
        val standings = getStandings()
        val index = standings.indexOfFirst { it === bot }
        if (index <= 0) {
            bot.progress += calculateProgressBonus(bot) * 0.5
            return
        }

        val target = standings[index - 1]
        val chance = calculateOvertakeChance(bot, target)
        if (random.nextDouble() <= chance) {
            val gained = max(4.0, calculateProgressBonus(bot))
            bot.progress += gained
            target.progress = max(0.0, target.progress - 1.5)
            turnLogs.add("${bot.name} overtook ${target.name}.")
        } else {
            bot.progress += calculateProgressBonus(bot) * 0.3
        }
    }

    private fun advanceAllCompetitors() {
        competitors.filter { it.alive }.forEach { competitor ->
            competitor.progress += calculateProgressBonus(competitor)
        }
    }

    private fun resolveFinishCondition() {
        val player = getPlayerState()
        if (!player.alive) {
            finished = true
            winnerName = competitors.filter { it.alive }.maxByOrNull { it.progress }?.name
            turnLogs.add("YOU were eliminated.")
            return
        }

        val alive = competitors.filter { it.alive }
        if (alive.isEmpty()) {
            finished = true
            winnerName = null
            return
        }

        val winnerByDistance = alive.firstOrNull { it.progress >= finishDistance }
        if (winnerByDistance != null) {
            finished = true
            winnerName = winnerByDistance.name
            turnLogs.add("${winnerByDistance.name} reached the finish line.")
            return
        }

        if (alive.size == 1) {
            finished = true
            winnerName = alive.first().name
            turnLogs.add("${winnerName} remains the only car on track.")
        }
    }

    private fun snapshotResult(): SurvivalTurnResult =
        SurvivalTurnResult(
            logs = turnLogs.takeLast(8),
            finished = finished,
            winnerName = winnerName
        )

    private fun appendAndAdvance(message: String): SurvivalTurnResult {
        turnLogs.add(message)
        return resolveTurn("invalid")
    }

    private fun playerWeapons(car: Car): List<Weapon> = listOfNotNull(
        car.getMeleeWeapon1(),
        car.getMeleeWeapon2(),
        car.getRangedWeapon()
    )

    private fun canAttackTarget(attacker: SurvivalCompetitorState, target: SurvivalCompetitorState): Boolean {
        val adjacent = isAdjacent(attacker, target)
        return playerWeapons(attacker.car).any { weapon ->
            when (weapon) {
                is MeleeWeapon -> adjacent
                is RangedWeapon -> true
                else -> true
            }
        }
    }

    private fun isAdjacent(target: SurvivalCompetitorState): Boolean {
        val standings = getStandings()
        val playerIndex = standings.indexOfFirst { it.isPlayer }
        val targetIndex = standings.indexOfFirst { it === target }
        return playerIndex >= 0 && targetIndex >= 0 && kotlin.math.abs(playerIndex - targetIndex) == 1
    }

    private fun isAdjacent(attacker: SurvivalCompetitorState, target: SurvivalCompetitorState): Boolean {
        val standings = getStandings()
        val attackerIndex = standings.indexOfFirst { it === attacker }
        val targetIndex = standings.indexOfFirst { it === target }
        return attackerIndex >= 0 && targetIndex >= 0 && kotlin.math.abs(attackerIndex - targetIndex) == 1
    }

    private fun attemptHit(
        attacker: SurvivalCompetitorState,
        target: SurvivalCompetitorState,
        weapon: Weapon,
        adjacentBonus: Boolean
    ): Boolean {
        val chance = calculateAttackChance(attacker, target, weapon, adjacentBonus)
        return random.nextDouble() <= chance
    }

    private fun calculateAttackChance(
        attacker: SurvivalCompetitorState,
        target: SurvivalCompetitorState,
        weapon: Weapon,
        adjacentBonus: Boolean
    ): Double {
        var chance = weapon.getAccuracy()
        chance += attacker.pilot.getSkill() / 250.0
        chance += attacker.car.getPerformance().coerceAtLeast(attacker.car.getTotalPerformance()) / 5000.0
        chance -= target.car.getPerformance().coerceAtLeast(target.car.getTotalPerformance()) / 7000.0
        chance += if (adjacentBonus) 0.15 else 0.0
        chance -= track.getCornersRatio() * 0.10
        chance += weather.gripMultiplier * 0.05
        return chance.coerceIn(0.05, 0.95)
    }

    private fun calculateOvertakeChance(attacker: SurvivalCompetitorState, target: SurvivalCompetitorState): Double {
        var chance = attacker.car.getPerformance().coerceAtLeast(attacker.car.getTotalPerformance()) / 900.0
        chance += attacker.pilot.getSkill() / 180.0
        chance += track.getStraightsRatio() * 0.20
        chance -= track.getCornersRatio() * 0.12
        chance -= target.car.getPerformance().coerceAtLeast(target.car.getTotalPerformance()) / 1400.0
        return chance.coerceIn(0.10, 0.90)
    }

    private fun calculateProgressBonus(competitor: SurvivalCompetitorState): Double {
        val carPower = competitor.car.getPerformance().coerceAtLeast(competitor.car.getTotalPerformance())
        val pilotSkill = competitor.pilot.getSkill().toDouble()
        val base = carPower / 45.0 + pilotSkill / 6.0
        val trackFactor = (track.getStraightsRatio() * 2.4) - (track.getCornersRatio() * 1.2)
        val weatherFactor = weather.gripMultiplier
        return max(1.0, (base + trackFactor) * weatherFactor)
    }

    private fun botAttackBias(bot: SurvivalCompetitorState): Double {
        return (0.30 + bot.pilot.getSkill() / 250.0 + bot.car.getPerformance().coerceAtLeast(bot.car.getTotalPerformance()) / 4000.0)
            .coerceIn(0.25, 0.80)
    }

    private fun baseCompetitorStrength(competitor: SurvivalCompetitorState): Double {
        return competitor.car.getPerformance().coerceAtLeast(competitor.car.getTotalPerformance()) + competitor.pilot.getSkill() * 5.0
    }

    private fun formatProgress(value: Double): String = String.format(java.util.Locale.US, "%.1f", value)
}

