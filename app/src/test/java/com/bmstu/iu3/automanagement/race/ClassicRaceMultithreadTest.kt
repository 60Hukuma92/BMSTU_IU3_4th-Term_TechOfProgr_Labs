package com.bmstu.iu3.automanagement.race

import com.bmstu.iu3.automanagement.models.PitStopBox
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class ClassicRaceMultithreadTest {

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
    fun `zero random always returns zero`() {
        val random = ZeroRandom

        repeat(100) {
            assertEquals("Should return 0", 0, random.nextBits(32))
        }
    }

    @Test
    fun `sequence random cycles correctly`() {
        val random = SequenceRandom(arrayOf(10, 20, 30))

        assertEquals("First should be 10", 10, random.nextBits(32))
        assertEquals("Second should be 20", 20, random.nextBits(32))
        assertEquals("Third should be 30", 30, random.nextBits(32))
        assertEquals("Fourth should cycle to 10", 10, random.nextBits(32))
    }

    @Test
    fun `test random with seed is reproducible`() {
        val random1 = TestRandom(seed = 123)
        val random2 = TestRandom(seed = 123)

        repeat(50) {
            assertEquals("Should match with same seed", random1.nextBits(32), random2.nextBits(32))
        }
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

