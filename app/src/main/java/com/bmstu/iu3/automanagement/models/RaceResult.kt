package com.bmstu.iu3.automanagement.models

import java.util.Locale

class RaceResult {
    private var teamName: String = ""
    private var time: Double = 0.0
    private var position: Int = 0
    private var prizeMoney: Double = 0.0
    private var incident: Incident? = null

    fun getTeamName(): String = teamName
    fun setTeamName(value: String) { teamName = value }

    fun getTime(): Double = time
    fun setTime(value: Double) { time = value }

    fun getPosition(): Int = position
    fun setPosition(value: Int) { position = value }

    fun getPrizeMoney(): Double = prizeMoney
    fun setPrizeMoney(value: Double) { prizeMoney = value }

    fun getIncident(): Incident? = incident
    fun setIncident(value: Incident?) { incident = value }

    fun getTimeFormatted(): String {
        val minutes = (time / 60).toInt()
        val seconds = (time % 60).toInt()
        val millis = ((time % 1) * 1000).toInt()
        return String.format(Locale.US, "%02d:%02d.%03d", minutes, seconds, millis)
    }
}
