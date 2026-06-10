package no.novari.kontroll.fint.gateway.kafka

import no.novari.kafka.producing.ParameterizedProducerRecord
import no.novari.kafka.producing.ParameterizedTemplate
import no.novari.kafka.producing.ParameterizedTemplateFactory
import no.novari.kontroll.fint.gateway.entity.EntityPipeline
import org.springframework.stereotype.Component

@Component
class EntityResourcePublisher(
    parameterizedTemplateFactory: ParameterizedTemplateFactory,
    private val entityResourceKeyExtractor: EntityResourceKeyExtractor,
) {
    private val parameterizedTemplate: ParameterizedTemplate<Any> =
        parameterizedTemplateFactory.createTemplate(Any::class.java)

    fun publish(
        entityPipeline: EntityPipeline,
        resources: List<HashMap<String, Any>>,
    ) {
        resources.forEach { resource ->
            val key = entityResourceKeyExtractor.getKey(resource, entityPipeline.selfLinkKeyFilter)

            parameterizedTemplate.send(
                ParameterizedProducerRecord
                    .builder<Any>()
                    .topicNameParameters(entityPipeline.topicNameParameters)
                    .key(key)
                    .value(resource)
                    .build(),
            )
        }
    }
}
