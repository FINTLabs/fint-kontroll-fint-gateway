package no.novari.kontroll.fint.gateway

import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.ClientHttpRequestFactory
import org.springframework.http.client.JdkClientHttpRequestFactory
import org.springframework.web.client.RestClient
import java.net.http.HttpClient
import java.time.Duration

private val logger = LoggerFactory.getLogger(NoOAuthWebClientConfiguration::class.java)

@Configuration
@ConditionalOnProperty(name = ["fint.kontroll.datainput"], havingValue = "mock")
@ConfigurationProperties(prefix = "fint.client")
class NoOAuthWebClientConfiguration {
    lateinit var baseUrl: String

    @Bean
    fun clientHttpRequestFactory(): ClientHttpRequestFactory {
        val requestFactory =
            JdkClientHttpRequestFactory(
                HttpClient
                    .newBuilder()
                    .connectTimeout(Duration.ofMinutes(5))
                    .build(),
            )
        requestFactory.setReadTimeout(Duration.ofMinutes(5))

        return requestFactory
    }

    @Bean
    fun restClient(
        builder: RestClient.Builder,
        clientHttpRequestFactory: ClientHttpRequestFactory,
    ): RestClient {
        val restClient =
            builder
                .requestFactory(clientHttpRequestFactory)
                .baseUrl(baseUrl)
                .build()
        logger.info("simple restclient without authentication created")

        return restClient
    }
}
