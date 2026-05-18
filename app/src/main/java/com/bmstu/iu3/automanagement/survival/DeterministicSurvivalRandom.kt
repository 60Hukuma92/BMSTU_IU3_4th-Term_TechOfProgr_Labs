package com.bmstu.iu3.automanagement.survival

class DeterministicSurvivalRandom(private val sequence: DoubleArray) : SurvivalRandom {
    private var index = 0
    override fun nextDouble(): Double {
        if (sequence.isEmpty()) return 0.0
        val v = sequence[index % sequence.size]
        index += 1
        return v
    }
}

