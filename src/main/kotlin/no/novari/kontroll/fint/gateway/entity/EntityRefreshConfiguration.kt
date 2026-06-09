package no.novari.kontroll.fint.gateway.entity

data class EntityRefreshConfiguration(
    val intervalMs: Long = 0,
    val topicRetentionTimeOffsetMs: Long = 0,
)
