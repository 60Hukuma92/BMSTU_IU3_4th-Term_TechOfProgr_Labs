package com.bmstu.iu3.automanagement.race

import com.bmstu.iu3.automanagement.models.PitStopBox
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.concurrent.atomic.AtomicInteger
import kotlin.random.Random

class ConcurrencySafetyTest {

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
    fun `deterministic random always returns same value`() {
        val random = ZeroRandom

        repeat(100) {
            assertEquals("Should always be 0", 0, random.nextBits(32))
        }
    }

    @Test
    fun `sequence random cycles through array`() {
        val sequence = arrayOf(1, 2, 3)
        val random = SequenceRandom(sequence)

        val results = mutableListOf<Int>()
        repeat(9) {
            results.add(random.nextBits(32))
        }

        val expected = listOf(1, 2, 3, 1, 2, 3, 1, 2, 3)
        assertEquals("Should cycle through sequence", expected, results)
    }

    @Test
    fun `test random with seed produces consistent sequence`() {
        val random1 = TestRandom(seed = 42)
        val random2 = TestRandom(seed = 42)

        repeat(10) {
            assertEquals("Should produce same values with same seed",
                random1.nextBits(32),
                random2.nextBits(32)
            )
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

    private fun runBlocking(block: suspend () -> Boolean): Boolean {
        val result = AtomicInteger(-1)
        val thread = Thread {
            kotlinx.coroutines.runBlocking {
                result.set(if (block()) 1 else 0)
            }
        }
        thread.start()
        thread.join()
        return result.get() == 1
    }

    private fun assertFalse(message: String, value: Boolean) {
        assertTrue(message, !value)
    }

    private object ZeroRandom : Random() {
        override fun nextBits(bitCount: Int): Int = 0
    }

    private class TestRandom(val seed: Long) : Random() {
        private var state = seed

        override fun nextBits(bitCount: Int): Int {
            state = (state * 1103515245L + 12345) and 0x7fffffffL
            return (state shr (31 - bitCount)).toInt()
        }
    }

    private class SequenceRandom(private val sequence: Array<Int>) : Random() {
        private var index = 0

        override fun nextBits(bitCount: Int): Int {
            val result = sequence[index % sequence.size]
            index++
            return result
        }
    }
}

