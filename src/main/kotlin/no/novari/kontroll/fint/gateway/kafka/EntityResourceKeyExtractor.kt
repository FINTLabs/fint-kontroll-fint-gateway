package no.novari.kontroll.fint.gateway.kafka

import org.springframework.stereotype.Component

@Component
class EntityResourceKeyExtractor {
    @Suppress("UNCHECKED_CAST")
    fun getKey(
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
}
