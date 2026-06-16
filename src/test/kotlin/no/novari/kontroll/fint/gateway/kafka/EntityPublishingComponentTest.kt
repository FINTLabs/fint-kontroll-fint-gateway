package no.novari.kontroll.fint.gateway.kafka

import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import no.novari.kafka.topic.EntityTopicService
import no.novari.kafka.topic.configuration.EntityCleanupFrequency
import no.novari.kafka.topic.configuration.EntityTopicConfiguration
import no.novari.kafka.topic.name.EntityTopicNameParameters
import no.novari.kafka.topic.name.TopicNamePrefixParameters
import no.novari.kontroll.fint.gateway.authorization.FintClient
import no.novari.kontroll.fint.gateway.entity.EntityConfiguration
import no.novari.kontroll.fint.gateway.entity.EntityPipeline
import no.novari.kontroll.fint.gateway.entity.EntityPipelineConfiguration
import no.novari.kontroll.fint.gateway.entity.EntityPipelineFactory
import org.springframework.web.client.RestClientException
import java.time.Duration
import kotlin.test.Test
import kotlin.test.assertEquals

class EntityPublishingComponentTest {
    private val entityTopicService = mockk<EntityTopicService>()
    private val entityPipelineFactory = mockk<EntityPipelineFactory>()
    private val publishingService = mockk<EntityResourcePublishingService>()
    private val fintClient = mockk<FintClient>()

    @Test
    fun `creates entity topic for configured pipeline on init`() {
        val config = EntityPipelineConfiguration(resourceReference = "administrasjon.personal.person")
        val pipeline = EntityPipeline(topicNameParameters("administrasjon-personal-person"), "/person", "systemid")
        val topicConfigurationSlot = slot<EntityTopicConfiguration>()

        every { entityPipelineFactory.create(config) } returns pipeline
        justRun {
            entityTopicService.createOrModifyTopic(
                pipeline.topicNameParameters,
                capture(topicConfigurationSlot),
            )
        }

        createComponent(config)

        assertEquals(1, topicConfigurationSlot.captured.partitions)
        assertEquals(Duration.ofDays(7), topicConfigurationSlot.captured.nullValueRetentionTime)
        assertEquals(EntityCleanupFrequency.NORMAL, topicConfigurationSlot.captured.cleanupFrequency)
    }

    @Test
    fun `reset delegates to fint client`() {
        justRun { fintClient.resetLastUpdatedTimestamps() }
        justRun { entityTopicService.createOrModifyTopic(any(), any()) }

        val component = createComponent()

        component.resetLastUpdatedTimestamps()

        verify(exactly = 1) { fintClient.resetLastUpdatedTimestamps() }
    }

    @Test
    fun `pull delegates fetched resources to publishing service`() {
        val pipeline = EntityPipeline(topicNameParameters("resource"), "/resource", "systemid")
        val resource = hashMapOf<String, Any>("name" to "Ola")

        every { fintClient.getResourcesLastUpdated("/resource") } returns listOf(resource)
        justRun { publishingService.publish(pipeline, listOf(resource)) }
        justRun { entityTopicService.createOrModifyTopic(any(), any()) }

        val component = createComponent()

        component.pullUpdatedEntityResources(pipeline)

        verify(exactly = 1) { publishingService.publish(pipeline, listOf(resource)) }
    }

    @Test
    fun `pull publishes empty list when FINT client fails`() {
        val pipeline = EntityPipeline(topicNameParameters("resource"), "/resource", "systemid")

        every { fintClient.getResourcesLastUpdated("/resource") } throws RestClientException("FINT unavailable")
        justRun { publishingService.publish(pipeline, emptyList()) }
        justRun { entityTopicService.createOrModifyTopic(any(), any()) }

        val component = createComponent()

        component.pullUpdatedEntityResources(pipeline)

        verify(exactly = 1) { publishingService.publish(pipeline, emptyList()) }
    }

    private fun createComponent(vararg configs: EntityPipelineConfiguration): EntityPublishingComponent {
        configs.forEach { config ->
            every { entityPipelineFactory.create(config) } returns
                EntityPipeline(
                    topicNameParameters(config.resourceReference.replace(".", "-")),
                    "/${config.resourceReference.replace(".", "/")}",
                    "systemid",
                )
        }

        return EntityPublishingComponent(
            entityTopicService = entityTopicService,
            entityConfiguration = EntityConfiguration(entityPipelines = configs.toList()),
            entityPipelineFactory = entityPipelineFactory,
            entityResourcePublishingService = publishingService,
            fintClient = fintClient,
        )
    }

    private fun topicNameParameters(resourceName: String): EntityTopicNameParameters =
        EntityTopicNameParameters
            .builder()
            .topicNamePrefixParameters(
                TopicNamePrefixParameters
                    .stepBuilder()
                    .orgId("fintlabs-no")
                    .domainContext("kontroll")
                    .build(),
            ).resourceName(resourceName)
            .build()
}
