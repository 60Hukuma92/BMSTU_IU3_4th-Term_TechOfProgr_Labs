package com.bmstu.iu3.automanagement

import com.bmstu.iu3.automanagement.data.GameState
import com.bmstu.iu3.automanagement.models.PitStopBox
import com.bmstu.iu3.automanagement.race.ClassicRaceSessionStore
import com.bmstu.iu3.automanagement.race.InMemoryEventSink
import com.bmstu.iu3.automanagement.race.RaceParticipant
import com.bmstu.iu3.automanagement.race.SemaphorePitStopManager
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.concurrent.atomic.AtomicInteger
import kotlin.collections.zipWithNext

class RaceMultiThreadingTests {

    @Before
    fun setUp() {
        GameState.clearInventory()
        GameState.resetTracksToDefault()
        GameState.setBudget(50000.0)
    }

    @Test
    fun `cannot start two races simultaneously`() {
        val track = GameState.getTracks().first()
        val pilot = com.bmstu.iu3.automanagement.models.Pilot().apply { setSkill(50) }
        val car = com.bmstu.iu3.automanagement.models.Car().apply { setPerformance(500.0) }

        val start1 = ClassicRaceSessionStore.startClassicRace(car, pilot, track, com.bmstu.iu3.automanagement.models.Weather.SUNNY)
        assertTrue("First race should start", start1)

        Thread.sleep(50)

        val start2 = ClassicRaceSessionStore.startClassicRace(car, pilot, track, com.bmstu.iu3.automanagement.models.Weather.SUNNY)
        assertTrue("Second race should fail", !start2)

        ClassicRaceSessionStore.stopCurrentRace()
    }

    @Test
    fun `stop clears isRunning flag`() {
        val track = GameState.getTracks().first()
        val pilot = com.bmstu.iu3.automanagement.models.Pilot().apply { setSkill(50) }
        val car = com.bmstu.iu3.automanagement.models.Car().apply { setPerformance(500.0) }

        ClassicRaceSessionStore.startClassicRace(car, pilot, track, com.bmstu.iu3.automanagement.models.Weather.SUNNY)
        Thread.sleep(50)
        ClassicRaceSessionStore.stopCurrentRace()
        Thread.sleep(100)

        assertTrue("Race should be stopped", !ClassicRaceSessionStore.state.value.isRunning)
    }

    @Test
    fun `pit stop boxes can be created with capacity`() {
        val box1 = PitStopBox(id = "BOX-1", capacity = 1)
        val box2 = PitStopBox(id = "BOX-2", capacity = 2)

        assertEquals("BOX-1", box1.id)
        assertEquals(1, box1.capacity)
        assertEquals("BOX-2", box2.id)
        assertEquals(2, box2.capacity)
    }

    @Test
    fun `pit stop manager initializes with boxes`() {
        val boxes = listOf(
            PitStopBox(id = "A", capacity = 1),
            PitStopBox(id = "B", capacity = 1)
        )
        val manager = SemaphorePitStopManager(boxes = boxes)

        assertEquals("Should have 2 boxes", 2, manager.getBoxes().size)
        assertEquals("Total capacity should be 2", 2, manager.getBoxes().sumOf { it.capacity })
    }

    @Test
    fun `race participant can be created`() {
        val participant = RaceParticipant(
            id = "car-1",
            displayName = "Test Car",
            basePace = 5.0,
            variance = 2.0
        )

        assertEquals("car-1", participant.id)
        assertEquals("Test Car", participant.displayName)
        assertEquals(5.0, participant.basePace, 0.01)
        assertEquals(2.0, participant.variance, 0.01)
    }

    @Test
    fun `pit stop manager enforces capacity limit`() {
        val manager = SemaphorePitStopManager(
            boxes = listOf(
                PitStopBox(id = "BOX-1", capacity = 1),
                PitStopBox(id = "BOX-2", capacity = 1)
            )
        )

        val granted = AtomicInteger(0)

        repeat(10) { i ->
            val thread = Thread {
                val result = runBlocking { manager.requestPitStop("car-$i") }
                if (result) {
                    granted.incrementAndGet()
                    Thread.sleep(5)
                    manager.releasePitStop("car-$i")
                }
            }
            thread.start()
        }

        Thread.sleep(500)

        assertTrue("Should grant <= 2 requests", granted.get() <= 2)
    }

    @Test
    fun `pit stop manager can release and re-grant capacity`() {
        val manager = SemaphorePitStopManager(
            boxes = listOf(PitStopBox(id = "BOX-1", capacity = 1))
        )

        assertTrue("First request should be granted",
            runBlocking { manager.requestPitStop("car-1") })

        assertFalse("Second request should be denied",
            runBlocking { manager.requestPitStop("car-2") })

        manager.releasePitStop("car-1")

        assertTrue("Third request should be granted after release",
            runBlocking { manager.requestPitStop("car-3") })
    }

    @Test
    fun `pit stop box capacity is respected across multiple boxes`() {
        val manager = SemaphorePitStopManager(
            boxes = listOf(
                PitStopBox(id = "BOX-1", capacity = 1),
                PitStopBox(id = "BOX-2", capacity = 1),
                PitStopBox(id = "BOX-3", capacity = 1)
            )
        )

        val granted = mutableListOf<String>()

        repeat(5) { i ->
            val success = runBlocking { manager.requestPitStop("car-$i") }
            if (success) granted.add("car-$i")
        }

        assertEquals("Should grant exactly 3 requests", 3, granted.size)

        granted.forEach { manager.releasePitStop(it) }

        val secondBatch = mutableListOf<String>()
        repeat(3) { i ->
            val success = runBlocking { manager.requestPitStop("car-${i + 100}") }
            if (success) secondBatch.add("car-${i + 100}")
        }

        assertEquals("All should be granted after release", 3, secondBatch.size)
    }

    @Test
    fun `event sink stores events in order`() {
        val sink = InMemoryEventSink()
        val eventCount = 100

        repeat(eventCount) { i ->
            sink.publish(
                com.bmstu.iu3.automanagement.models.RaceLogEntry(
                    timestampMs = (i * 100L),
                    source = "Source-${i % 5}",
                    message = "Event $i"
                )
            )
        }

        val stored = sink.getEvents()
        assertEquals("All events should be stored", eventCount, stored.size)

        stored.zipWithNext().forEach { (a, b) ->
            assertTrue("Events should be in order", a.timestampMs <= b.timestampMs)
        }
    }

    @Test
    fun `pit stop boxes list is returned correctly`() {
        val boxes = listOf(
            PitStopBox(id = "A", capacity = 1),
            PitStopBox(id = "B", capacity = 2),
            PitStopBox(id = "C", capacity = 1)
        )
        val manager = SemaphorePitStopManager(boxes = boxes)

        assertEquals("Should return correct boxes", boxes, manager.getBoxes())
    }
}