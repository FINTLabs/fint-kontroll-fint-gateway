package no.novari.kontroll.fint.gateway.entity

import jdk.jfr.Enabled

data class EntityConfiguration(
    val enabled: Boolean,
    val refresh: EntityRefreshConfiguration,
    val pull: EntityPullConfiguration,
    val entityPipelines: List<EntityPipelineConfiguration>,
)
