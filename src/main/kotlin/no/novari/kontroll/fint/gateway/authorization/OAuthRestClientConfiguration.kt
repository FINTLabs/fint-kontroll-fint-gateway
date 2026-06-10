package no.novari.kontroll.fint.gateway

import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.ClientHttpRequestFactory
import org.springframework.http.client.JdkClientHttpRequestFactory
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.OAuth2AuthorizationContext
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.oauth2.client.web.client.OAuth2ClientHttpRequestInterceptor
import org.springframework.web.client.RestClient
import java.net.http.HttpClient
import java.time.Duration
import java.util.Optional

private val logger = LoggerFactory.getLogger(OAuthRestClientConfiguration::class.java)

@Configuration
@ConditionalOnProperty(name = ["fint.kontroll.datainput"], havingValue = "fint")
@ConfigurationProperties(prefix = "fint.client")
class OAuthRestClientConfiguration {
    lateinit var baseUrl: String
    lateinit var username: String
    lateinit var password: String
    lateinit var registrationId: String

    @Bean
    fun dummyAuthentication(): Authentication = UsernamePasswordAuthenticationToken("fint", "client", emptyList())

    @Bean
    @ConditionalOnProperty(name = ["fint.resource-gateway.authorization"], havingValue = "enabled")
    fun authorizedClientManager(
        clientRegistrationRepository: ClientRegistrationRepository,
        authorizedClientService: OAuth2AuthorizedClientService,
    ): OAuth2AuthorizedClientManager {
        val authorizedClientProvider =
            OAuth2AuthorizedClientProviderBuilder
                .builder()
                .password()
                .refreshToken()
                .build()

        val authorizedClientManager =
            AuthorizedClientServiceOAuth2AuthorizedClientManager(
                clientRegistrationRepository,
                authorizedClientService,
            )

        authorizedClientManager.setAuthorizedClientProvider(authorizedClientProvider)
        authorizedClientManager.setContextAttributesMapper(::contextAttributesMapper)

        return authorizedClientManager
    }

    private fun contextAttributesMapper(authorizeRequest: OAuth2AuthorizeRequest): Map<String, Any> =
        mapOf(
            OAuth2AuthorizationContext.USERNAME_ATTRIBUTE_NAME to username,
            OAuth2AuthorizationContext.PASSWORD_ATTRIBUTE_NAME to password,
        )

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
        authorizedClientManager: Optional<OAuth2AuthorizedClientManager>,
        clientHttpRequestFactory: ClientHttpRequestFactory,
        dummyAuthentication: Authentication,
    ): RestClient {
        authorizedClientManager.ifPresent { presentAuthorizedClientManager ->
            val oauth2ClientHttpRequestInterceptor =
                OAuth2ClientHttpRequestInterceptor(presentAuthorizedClientManager)

            oauth2ClientHttpRequestInterceptor.setClientRegistrationIdResolver { registrationId }
            oauth2ClientHttpRequestInterceptor.setPrincipalResolver { dummyAuthentication }
            builder.requestInterceptor(oauth2ClientHttpRequestInterceptor)
        }

        logger.info("oAuth restclient created")

        return builder
            .requestFactory(clientHttpRequestFactory)
            .baseUrl(baseUrl)
            .build()
    }
}
