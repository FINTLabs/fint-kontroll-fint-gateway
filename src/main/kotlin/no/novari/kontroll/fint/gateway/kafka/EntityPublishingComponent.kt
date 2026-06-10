package no.novari.kontroll.fint.gateway.kafka

import no.novari.kafka.producing.ParameterizedProducerRecord
import no.novari.kafka.producing.ParameterizedTemplate
import no.novari.kafka.producing.ParameterizedTemplateFactory
import no.novari.kafka.topic.EntityTopicService
import no.novari.kafka.topic.configuration.EntityCleanupFrequency
import no.novari.kafka.topic.configuration.EntityTopicConfiguration
import no.novari.kontroll.fint.gateway.authorization.FintClient
import no.novari.kontroll.fint.gateway.entity.EntityConfiguration
import no.novari.kontroll.fint.gateway.entity.EntityPipeline
import no.novari.kontroll.fint.gateway.entity.EntityPipelineConfiguration
import no.novari.kontroll.fint.gateway.entity.EntityPipelineFactory
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClientException
import java.time.Duration

@Component
@ConditionalOnProperty(
    prefix = "fint.kontroll.resource-gateway.resources.entity",
    name = ["enabled"],
    havingValue = "true",
)
class EntityPublishingComponent(
    entityTopicService: EntityTopicService,
    parameterizedTemplateFactory: ParameterizedTemplateFactory,
    entityConfiguration: EntityConfiguration,
    entityPipelineFactory: EntityPipelineFactory,
    private val fintClient: FintClient,
) {
    private val parameterizedTemplate: ParameterizedTemplate<Any> =
        parameterizedTemplateFactory.createTemplate(Any::class.java)

    private val entityPipelines: List<EntityPipeline> =
        createEntityPipelines(
            entityPipelineFactory,
            entityConfiguration.entityPipelines,
        )

    init {
        entityPipelines.forEach { entityPipeline ->
            entityTopicService.createOrModifyTopic(
                entityPipeline.topicNameParameters,
                EntityTopicConfiguration
                    .stepBuilder()
                    .partitions(1)
                    .lastValueRetainedForever()
                    .nullValueRetentionTime(Duration.ofDays(7))
                    .cleanupFrequency(EntityCleanupFrequency.NORMAL)
                    .build(),
            )
        }
    }

    private fun createEntityPipelines(
        entityPipelineFactory: EntityPipelineFactory,
        configs: List<EntityPipelineConfiguration>,
    ): List<EntityPipeline> = configs.map(entityPipelineFactory::create)

    @Scheduled(fixedRateString = "\${fint.kontroll.resource-gateway.resources.entity.refresh.interval-ms}")
    private fun resetLastUpdatedTimestamps() {
        log.warn("Resetting resource last updated timestamps")
        fintClient.resetLastUpdatedTimestamps()
    }

    @Scheduled(
        initialDelayString = "\${fint.kontroll.resource-gateway.resources.entity.pull.initial-delay-ms}",
        fixedDelayString = "\${fint.kontroll.resource-gateway.resources.entity.pull.fixed-delay-ms}",
    )
    private fun pullAllUpdatedEntityResources() {
        log.info("Starting pulling resources")
        entityPipelines.forEach(::pullUpdatedEntityResources)
        log.info("Completed pulling resources")
    }

    private fun pullUpdatedEntityResources(entityPipeline: EntityPipeline) {
        val resources = getUpdatedResources(entityPipeline.fintEndpoint)

        resources.forEach { resource ->
            val key = getKey(resource, entityPipeline.selfLinkKeyFilter)

            parameterizedTemplate.send(
                ParameterizedProducerRecord
                    .builder<Any>()
                    .topicNameParameters(entityPipeline.topicNameParameters)
                    .key(key)
                    .value(resource)
                    .build(),
            )
        }

        log.info(
            "${resources.size} entities sent to ${entityPipeline.topicNameParameters.resourceName}",
        )
    }

    private fun getUpdatedResources(endpointUrl: String): List<HashMap<String, Any>> =
        try {
            fintClient
                .getResourcesLastUpdated(endpointUrl)
                .map { it as HashMap<String, Any> }
        } catch (e: RestClientException) {
            log.error("Could not pull entities from endpoint=$endpointUrl", e)
            emptyList()
        }

    @Suppress("UNCHECKED_CAST")
    private fun getKey(
        resource: HashMap<String, Any>,
        selfLinkKeyFilter: String,
    ): String {
        val links = resource["_links"] as HashMap<String, Any>
        val selfLinks = links["self"] as List<HashMap<String, String>>

        return selfLinks
            .asSequence()
            .filter { it.containsKey("href") }
            .mapNotNull { it["href"] }
            .map { it.replaceFirst("^https:/\\/.+\\.felleskomponent.no".toRegex(), "") }
            .filter { it.lowercase().contains(selfLinkKeyFilter) }
            .minOrNull()
            ?: throw IllegalStateException(
                "No $selfLinkKeyFilter to generate key for resource=$resource",
            )
    }

    companion object {
        private val log = LoggerFactory.getLogger(EntityPublishingComponent::class.java)
    }
}
