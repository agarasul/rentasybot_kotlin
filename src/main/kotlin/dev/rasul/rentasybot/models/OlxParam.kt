package dev.rasul.rentasybot.models

data class OlxParam(
    val key: String,
    val name: String,
    val type: String,
    val value: OlxParamValue
)

data class OlxParamValue(
    val key: String,
    val label: String,
    val value: String? = null
)