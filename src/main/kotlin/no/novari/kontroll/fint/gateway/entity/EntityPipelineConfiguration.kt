package no.novari.kontroll.fint.gateway.entity

data class EntityPipelineConfiguration(
    val resourceReference: String,
    val kafkaTopic: String,
    val fintEndpoint: String,
    val selfLinkKeyFilter: String,
)
