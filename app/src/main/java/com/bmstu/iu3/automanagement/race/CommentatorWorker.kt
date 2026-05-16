package com.bmstu.iu3.automanagement.race

import com.bmstu.iu3.automanagement.models.CommentatorMessage
import com.bmstu.iu3.automanagement.models.RaceLogEntry
import com.bmstu.iu3.automanagement.models.Severity
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.delay
import java.util.Locale
import kotlin.random.Random

class CommentatorWorker(
    private val sourceEvents: ReceiveChannel<RaceLogEntry>,
    private val outputMessages: MutableList<CommentatorMessage>,
    private val onMessageGenerated: (CommentatorMessage) -> Unit = {}
) : RaceWorker {

    override val id: String = "commentator"
    @Volatile
    private var active: Boolean = true
    private var lastLeader: String? = null

    override suspend fun start() {
        while (active) {
            val event = sourceEvents.receiveCatching().getOrNull() ?: break
            
            val flavoredText = generateFlavorText(event)
            if (flavoredText == null) continue

            val commentatorMsg = event.copy(message = flavoredText).toCommentatorMessage()
            
            // Ритм трансляции — без долгой задержки, чтобы гонка не подвисала на комментариях
            delay(50L)

            outputMessages.add(commentatorMsg)
            onMessageGenerated(commentatorMsg)
        }
    }

    private fun generateFlavorText(event: RaceLogEntry): String? {
        val raw = event.message
        if (!raw.startsWith("STATUS_")) return raw

        val parts = raw.split("|")
        val code = parts[0]

        return when (code) {
            "STATUS_RACE_START" -> {
                val track = parts.getOrNull(1) ?: "the track"
                listOf(
                    "Welcome to $track! The engines are screaming as we wait for the lights!",
                    "Hello racing fans! We are live at $track for today's Grand Prix!",
                    "The grid is formed here at $track. Strategy will be key today!"
                ).random()
            }
            
            "STATUS_WEATHER" -> {
                val condition = parts.getOrNull(1) ?: "normal"
                when (condition.uppercase()) {
                    "SUNNY" -> listOf(
                        "The sun is beating down on the track. Expect record-breaking lap times!",
                        "Perfect conditions today! The asphalt is hot and the grip is at its peak.",
                        "Blue skies above! Teams will be pushing their cooling systems to the limit."
                    ).random()
                    "RAINY" -> listOf(
                        "WET TRACK! Drivers are struggling for grip as the rain starts to fall!",
                        "The heavens have opened! We're seeing huge plumes of spray behind the cars!",
                        "Tactical nightmare! The track is getting slippery, who will dive for the pits first?"
                    ).random()
                    "STORM" -> listOf(
                        "EXTREME CONDITIONS! A massive storm is hitting the circuit!",
                        "Visibility is almost zero! This is a real test of courage for the pilots.",
                        "Total chaos! The drivers are just trying to keep the cars on the black stuff!"
                    ).random()
                    else -> "The weather is shifting. Teams are monitoring the clouds closely."
                }
            }

            "STATUS_LEADER_CHANGE" -> {
                val name = parts.getOrNull(1) ?: "A driver"
                if (name == lastLeader) return null
                lastLeader = name
                listOf(
                    "DRAMA! $name has just snatched the lead!",
                    "WE HAVE A NEW LEADER! $name powers through to the front!",
                    "Masterclass move! $name is now leading the pack!",
                    "The crowd is on their feet as $name takes P1!"
                ).random()
            }

            "STATUS_OVERTAKE" -> {
                val over = parts.getOrNull(1) ?: "A driver"
                val under = parts.getOrNull(2) ?: "someone"
                listOf(
                    "BRILLIANT! $over dives down the inside of $under!",
                    "$over makes it stick! A clinical overtake on $under.",
                    "$under is under pressure... and $over goes through! Beautiful racing!",
                    "Wheel-to-wheel combat! $over emerges ahead of $under!"
                ).random()
            }

            "STATUS_DNF" -> {
                val name = parts.getOrNull(1) ?: "A driver"
                val reason = parts.getOrNull(2) ?: "mechanical issues"
                listOf(
                    "HEARTBREAK! $name has pulled over. Smoke trailing from the back!",
                    "DISASTER! $name is OUT of the race. The car has given up!",
                    "Yellow flag! $name is slow on track... they are retiring with $reason!"
                ).random()
            }

            "STATUS_FINE" -> {
                val name = parts.getOrNull(1) ?: "A driver"
                listOf(
                    "The stewards are not happy! $name gets a fine for speeding!",
                    "Speed trap triggered! That's going to be a penalty for $name.",
                    "Race control confirms: $name was too fast in the restricted zone!"
                ).random()
            }

            "STATUS_WORKER" -> {
                val name = parts.getOrNull(1) ?: "Driver"
                when (parts.getOrNull(2)) {
                    "STATUS_PUSHING" -> "$name is absolutely on the limit, pushing every single horsepower!"
                    "STATUS_LOCKUP" -> "Tyre smoke! $name locks the front-left into the corner!"
                    "STATUS_OVERHEATING" -> "Critical temperatures! $name has to back off to save the engine!"
                    else -> null
                }
            }

            "STATUS_RACE_FINISH" -> "CHECKERED FLAG! ${parts.getOrNull(1)} crosses the line to take a HISTORIC victory!"

            else -> raw.substringAfter("|")
        }
    }

    override suspend fun stop() { active = false }

    private fun RaceLogEntry.toCommentatorMessage() = CommentatorMessage(
        displayTime = String.format(Locale.US, "[%02d:%02d]", (timestampMs/60000)%60, (timestampMs/1000)%60),
        source = source,
        message = message,
        severity = severity
    )
}
