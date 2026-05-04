package com.bmstu.iu3.automanagement.models

data class Tactic(
    val id: String,
    val name: String,
    val description: String = "",
    val weatherModifiers: Map<String, Double> = emptyMap()
)


