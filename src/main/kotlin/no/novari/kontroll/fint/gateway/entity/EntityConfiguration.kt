package no.novari.kontroll.fint.gateway.entity

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "fint.kontroll.resource-gateway.resources.entity")
data class EntityConfiguration(
    val enabled: Boolean = false,
    val refresh: EntityRefreshConfiguration = EntityRefreshConfiguration(),
    val pull: EntityPullConfiguration = EntityPullConfiguration(),
    val entityPipelines: List<EntityPipelineConfiguration> = emptyList(),
)
