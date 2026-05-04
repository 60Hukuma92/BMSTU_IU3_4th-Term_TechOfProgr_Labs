package com.bmstu.iu3.automanagement.models

data class RaceLogEntry(
    val timestampMs: Long,
    val source: String,
    val severity: Severity = Severity.INFO,
    val message: String,
    val raceTick: Int? = null,
    val relatedEntityId: String? = null
)

enum class Severity {
    DEBUG, INFO, WARNING, ERROR
}


