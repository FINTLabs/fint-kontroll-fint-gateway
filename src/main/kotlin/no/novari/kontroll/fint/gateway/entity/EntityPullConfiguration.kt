package no.novari.kontroll.fint.gateway.entity

data class EntityPullConfiguration(
    val initialDelayMs: Long = 0,
    val fixedDelayMs: Long = 0,
)
