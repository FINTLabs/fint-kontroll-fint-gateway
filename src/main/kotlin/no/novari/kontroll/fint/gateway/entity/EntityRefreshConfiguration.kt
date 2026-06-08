package no.novari.kontroll.fint.gateway.entity

data class EntityRefreshConfiguration(
    val intervalMs: Long,
    val topicRetentionTimeOffsetMs: Long,
)
