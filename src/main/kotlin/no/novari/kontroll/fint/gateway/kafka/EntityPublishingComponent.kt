package no.novari.kontroll.fint.gateway.kafka

import no.novari.kafka.producing.ParameterizedProducerRecord
import no.novari.kafka.producing.ParameterizedTemplate
import no.novari.kafka.producing.ParameterizedTemplateFactory
import no.novari.kafka.topic.EntityTopicService
import no.novari.kafka.topic.configuration.EntityCleanupFrequency
import no.novari.kafka.topic.configuration.EntityTopicConfiguration
import no.novari.kafka.topic.name.EntityTopicNameParameters
import no.novari.kafka.topic.name.TopicNameParameters
import no.novari.kafka.topic.name.TopicNamePrefixParameters
import no.novari.kontroll.fint.gateway.ObjectResources
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

@Component
@ConditionalOnProperty(prefix = "fint.gateway.kafka", name = "enabled", havingValue = "true")
class EntityPublishingComponent(
    parameterizedTemplateFactory: ParameterizedTemplateFactory,
    entityTopicService: EntityTopicService,
) {
    private val parameterizedTemplate: ParameterizedTemplate<Any> =
        parameterizedTemplateFactory.createTemplate<Any>(
            ObjectResources,
        )

    private val entityTopicNameParameters: EntityTopicNameParameters =
        EntityTopicNameParameters
            .builder()
            .resourceName(ObjectResources)
            .topicNamePrefixParameters(topicNameParameters())
            .build()

    fun topicNameParameters() =
        TopicNamePrefixParameters
            .stepBuilder()
            .orgIdApplicationDefault()
            .domainContextApplicationDefault()
            .build()
}
