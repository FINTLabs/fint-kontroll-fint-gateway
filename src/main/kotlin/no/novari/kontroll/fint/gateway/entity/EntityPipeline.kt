package no.novari.kontroll.fint.gateway.entity

import no.novari.kafka.topic.name.EntityTopicNameParameters

data class EntityPipeline(
    val topicNameParameters: EntityTopicNameParameters,
    val fintEndpoint: String,
    val selfLinkKeyFilter: String,
)
