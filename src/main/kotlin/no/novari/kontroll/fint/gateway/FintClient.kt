package no.novari.kontroll.fint.gateway

import no.novari.no.novari.kontroll.fint.gateway.ObjectResources
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import org.springframework.web.client.body
import java.util.concurrent.ConcurrentHashMap

@Component
class FintClient(
    private val restClient: RestClient,
) {
    private val sinceTimestamp: MutableMap<String, Long> = ConcurrentHashMap()

    fun getResourcesLastUpdated(endpoint: String): List<Any> = getLastUpdated(endpoint).content

    fun resetLastUpdatedTimestamps() {
        sinceTimestamp.clear()
    }

    private fun getLastUpdated(endpoint: String): ObjectResources {
        val lastUpdated =
            restClient
                .get()
                .uri("$endpoint/last-updated")
                .retrieve()
                .body<LastUpdated>()

        val objectResources =
            restClient
                .get()
                .uri { uriBuilder ->
                    uriBuilder
                        .path(endpoint)
                        .queryParam("sinceTimeStamp", sinceTimestamp.getOrDefault(endpoint, 0L))
                        .build()
                }.retrieve()
                .body<ObjectResources>()

        sinceTimestamp[endpoint] = lastUpdated!!.lastUpdated

        return objectResources!!
    }

    fun getResource(endpoint: String): Any? =
        restClient
            .get()
            .uri(endpoint)
            .retrieve()
            .body<Any>()

    fun <T> getResource(
        endpoint: String,
        clazz: Class<T>,
    ): T? =
        restClient
            .get()
            .uri(endpoint)
            .retrieve()
            .body(clazz)

    fun reset() {
        sinceTimestamp.clear()
    }

    private data class LastUpdated(
        var lastUpdated: Long = 0L,
    )
}
