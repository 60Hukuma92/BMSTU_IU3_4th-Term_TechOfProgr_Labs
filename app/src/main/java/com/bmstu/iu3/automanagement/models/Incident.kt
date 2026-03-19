package com.bmstu.iu3.automanagement.models

class Incident {
    private var reason: String = ""
    private var severity: String = ""

    fun getReason(): String = reason
    fun setReason(value: String) { reason = value }

    fun getSeverity(): String = severity
    fun setSeverity(value: String) { severity = value }
}
