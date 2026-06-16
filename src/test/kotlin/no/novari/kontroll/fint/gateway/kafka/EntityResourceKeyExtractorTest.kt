package no.novari.kontroll.fint.gateway.kafka

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class EntityResourceKeyExtractorTest {
    private val keyExtractor = EntityResourceKeyExtractor()

    @Test
    fun `extracts matching self link and strips FINT host prefix`() {
        val resource =
            resourceWithSelfLinks(
                "https://api.felleskomponent.no/administrasjon/personal/person/systemid/123",
            )

        val key = keyExtractor.getKey(resource, "systemid")

        assertEquals("/administrasjon/personal/person/systemid/123", key)
    }

    @Test
    fun `selects lexicographically smallest matching self link`() {
        val resource =
            resourceWithSelfLinks(
                "https://api.felleskomponent.no/administrasjon/personal/person/systemid/200",
                "https://api.felleskomponent.no/administrasjon/personal/person/systemid/100",
            )

        val key = keyExtractor.getKey(resource, "systemid")

        assertEquals("/administrasjon/personal/person/systemid/100", key)
    }

    @Test
    fun `ignores self links without href`() {
        val resource =
            hashMapOf<String, Any>(
                "_links" to
                    hashMapOf(
                        "self" to
                            listOf(
                                hashMapOf<String, String>(),
                                hashMapOf(
                                    "href" to
                                        "https://api.felleskomponent.no" +
                                        "/administrasjon/personal/person/systemid/123",
                                ),
                            ),
                    ),
            )

        val key = keyExtractor.getKey(resource, "systemid")

        assertEquals("/administrasjon/personal/person/systemid/123", key)
    }

    @Test
    fun `throws when no self link matches filter`() {
        val resource =
            resourceWithSelfLinks(
                "https://api.felleskomponent.no/administrasjon/personal/person/fodselsnummer/123",
            )

        assertFailsWith<IllegalStateException> {
            keyExtractor.getKey(resource, "systemid")
        }
    }

    private fun resourceWithSelfLinks(vararg hrefs: String): HashMap<String, Any> =
        hashMapOf(
            "_links" to
                hashMapOf(
                    "self" to hrefs.map { href -> hashMapOf("href" to href) },
                ),
        )
}
