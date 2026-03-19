package com.bmstu.iu3.automanagement.models

class Track {
    private var name: String = ""
    private var length: Double = 0.0
    private var straightsRatio: Double = 0.0 // 0.0 .. 1.0
    private var cornersRatio: Double = 0.0 // 0.0 .. 1.0
    private var elevationChange: Double = 0.0 // 4 difficulty

    fun getName(): String = name
    fun setName(value: String) { name = value }

    fun getLength(): Double = length
    fun setLength(value: Double) { length = value }

    fun getStraightsRatio(): Double = straightsRatio
    fun setStraightsRatio(value: Double) { straightsRatio = value }

    fun getCornersRatio(): Double = cornersRatio
    fun setCornersRatio(value: Double) { cornersRatio = value }

    fun getElevationChange(): Double = elevationChange
    fun setElevationChange(value: Double) { elevationChange = value }
}

enum class Weather(val gripMultiplier: Double) {
    SUNNY(1.0),
    CLOUDY(0.95),
    RAINY(0.7),
    STORM(0.5)
}
