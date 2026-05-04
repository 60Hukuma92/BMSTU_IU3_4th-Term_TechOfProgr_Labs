package com.bmstu.iu3.automanagement.models

data class CommentatorMessage(
    val displayTime: String,
    val source: String,
    val message: String,
    val severity: Severity = Severity.INFO
)


