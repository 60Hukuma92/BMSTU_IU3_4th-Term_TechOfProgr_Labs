@file:Suppress("unused")

package com.bmstu.iu3.automanagement.models

class CompromisingEvidence {
    private var playerName: String = ""
    private var pushBackValue: Int = 0
    private var issuedAt: Long = 0L

    fun getPlayerName(): String = playerName
    fun setPlayerName(value: String) { playerName = value }

    fun getPushBackValue(): Int = pushBackValue
    fun setPushBackValue(value: Int) { pushBackValue = value.coerceAtLeast(0) }

    fun getIssuedAt(): Long = issuedAt
    fun setIssuedAt(value: Long) { issuedAt = value }
}




