package no.novari.kontroll.fint.gateway.entity

import no.novari.kafka.topic.name.EntityTopicNameParameters
import no.novari.kafka.topic.name.TopicNamePrefixParameters
import org.springframework.stereotype.Service

@Service
class EntityPipelineFactory {
    fun create(entityPipelineConfiguration: EntityPipelineConfiguration): EntityPipeline {
        val resourceName: String = entityPipelineConfiguration.resourceReference.replace(" ", "-")

        val topicNameParameters: EntityTopicNameParameters =
            EntityTopicNameParameters
                .builder()
                .topicNamePrefixParameters(
                    TopicNamePrefixParameters
                        .stepBuilder()
                        .orgIdApplicationDefault()
                        .domainContextApplicationDefault()
                        .build(),
                ).resourceName(resourceName)
                .build()

        val fintEndpoint =
            entityPipelineConfiguration.fintEndpoint
                .takeIf { it.isNotEmpty() }
                ?: "/${entityPipelineConfiguration.resourceReference.replace(".", "/")}"

        val selfLinkKeyFilter =
            entityPipelineConfiguration.selfLinkKeyFilter
                .takeIf { it.isNotEmpty() }
                ?: "systemid"

        return EntityPipeline(topicNameParameters, fintEndpoint, selfLinkKeyFilter)
    }
}
