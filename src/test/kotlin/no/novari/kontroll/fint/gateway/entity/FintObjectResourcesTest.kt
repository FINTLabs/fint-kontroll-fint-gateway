package no.novari.kontroll.fint.gateway.entity

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FintObjectResourcesTest {
    @Test
    fun `stores generic resources in collection content`() {
        val resources = FintObjectResources()
        val resource = mapOf("systemid" to "123")

        resources.addResource(resource)

        assertEquals(listOf(resource), resources.content)
        assertEquals(1, resources.size)
    }

    @Test
    @Suppress("DEPRECATION")
    fun `type reference represents list of any objects`() {
        val typeName = FintObjectResources().typeReference.type.typeName

        assertTrue(typeName.contains("java.util.List"))
    }
}
