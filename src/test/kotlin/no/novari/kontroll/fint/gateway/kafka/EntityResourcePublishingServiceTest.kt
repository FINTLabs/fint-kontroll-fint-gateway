package no.novari.kontroll.fint.gateway.kafka

import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import no.novari.kafka.producing.ParameterizedProducerRecord
import no.novari.kafka.producing.ParameterizedTemplate
import no.novari.kafka.producing.ParameterizedTemplateFactory
import no.novari.kafka.topic.name.EntityTopicNameParameters
import no.novari.kafka.topic.name.TopicNamePrefixParameters
import no.novari.kontroll.fint.gateway.entity.EntityPipeline
import org.springframework.kafka.support.SendResult
import java.util.concurrent.CompletableFuture
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

class EntityResourcePublishingServiceTest {
    private val templateFactory = mockk<ParameterizedTemplateFactory>()
    private val parameterizedTemplate = mockk<ParameterizedTemplate<Any>>()
    private val keyExtractor = mockk<EntityResourceKeyExtractor>()

    @Test
    fun `publishes resources with extracted key and pipeline topic parameters`() {
        every { templateFactory.createTemplate(Any::class.java) } returns parameterizedTemplate
        every { keyExtractor.getKey(any(), "systemid") } returns "resource-key"
        every { parameterizedTemplate.send(any()) } returns
            CompletableFuture.completedFuture(mockk<SendResult<String, Any>>())

        val service = EntityResourcePublishingService(templateFactory, keyExtractor)
        val topicNameParameters = topicNameParameters("administrasjon-personal-person")
        val pipeline = EntityPipeline(topicNameParameters, "/administrasjon/personal/person", "systemid")
        val resource = hashMapOf<String, Any>("name" to "Ola")
        val recordSlot = slot<ParameterizedProducerRecord<Any>>()

        service.publish(pipeline, listOf(resource))

        verify(exactly = 1) { parameterizedTemplate.send(capture(recordSlot)) }
        assertSame(topicNameParameters, recordSlot.captured.topicNameParameters)
        assertEquals("resource-key", recordSlot.captured.key)
        assertSame(resource, recordSlot.captured.value)
    }

    @Test
    fun `does not send records when resource list is empty`() {
        every { templateFactory.createTemplate(Any::class.java) } returns parameterizedTemplate

        val service = EntityResourcePublishingService(templateFactory, keyExtractor)
        val pipeline = EntityPipeline(topicNameParameters("resource"), "/resource", "systemid")

        service.publish(pipeline, emptyList())

        verify(exactly = 0) { parameterizedTemplate.send(any()) }
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
