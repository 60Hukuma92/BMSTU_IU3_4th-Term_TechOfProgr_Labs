package com.bmstu.iu3.automanagement.models

class Incident {
    private var reason: String = ""
    private var severity: String = ""
    private var fineAmount: Double = 0.0

    fun getReason(): String = reason
    fun setReason(value: String) { reason = value }

    fun getSeverity(): String = severity
    fun setSeverity(value: String) { severity = value }
    
    fun getFineAmount(): Double = fineAmount
    fun setFineAmount(value: Double) { fineAmount = value }
}
