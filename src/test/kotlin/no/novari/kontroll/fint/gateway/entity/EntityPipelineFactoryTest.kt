package no.novari.kontroll.fint.gateway.entity

import kotlin.test.Test
import kotlin.test.assertEquals

class EntityPipelineFactoryTest {
    private val factory = EntityPipelineFactory()

    @Test
    fun `creates pipeline with normalized resource name and generated endpoint`() {
        val pipeline =
            factory.create(
                EntityPipelineConfiguration(
                    resourceReference = "Administrasjon.Personal.Personalressurs",
                ),
            )

        assertEquals("administrasjon-personal-personalressurs", pipeline.topicNameParameters.resourceName)
        assertEquals("/Administrasjon/Personal/Personalressurs", pipeline.fintEndpoint)
        assertEquals("systemid", pipeline.selfLinkKeyFilter)
    }

    @Test
    fun `uses configured endpoint and self link key filter when provided`() {
        val pipeline =
            factory.create(
                EntityPipelineConfiguration(
                    resourceReference = "administrasjon.personal.person",
                    fintEndpoint = "/custom/person",
                    selfLinkKeyFilter = "fodselsnummer",
                ),
            )

        assertEquals("administrasjon-personal-person", pipeline.topicNameParameters.resourceName)
        assertEquals("/custom/person", pipeline.fintEndpoint)
        assertEquals("fodselsnummer", pipeline.selfLinkKeyFilter)
    }
}
