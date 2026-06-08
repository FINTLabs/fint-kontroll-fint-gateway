# FINT Kontroll FINT Gateway

## FINT Client Authorisation Flow

`FintClient` retrieves resources from FINT through the shared Spring `RestClient` bean. The client method itself does not fetch or attach access tokens; that is handled by the `RestClient` configuration before the HTTP request is sent.

```mermaid
sequenceDiagram
    participant Function as Gateway function
    participant FintClient
    participant RestClient
    participant Interceptor as OAuth2 interceptor
    participant IdP as FINT IdP
    participant FINT as FINT API

    Function->>FintClient: Request FINT resource
    FintClient->>RestClient: Execute HTTP request

    alt Authorisation enabled
        RestClient->>Interceptor: Intercept outgoing request
        Interceptor->>IdP: Authorise client with configured credentials
        IdP-->>Interceptor: Access token
        Interceptor->>RestClient: Add bearer token
    else Authorisation disabled or mock data input
        RestClient->>RestClient: Continue without bearer token
    end

    RestClient->>FINT: Send request
    FINT-->>RestClient: Resource response
    RestClient-->>FintClient: Deserialize response
    FintClient-->>Function: Return resource data
```

When a function retrieves resources through `FintClient`, the flow is:

1. Spring injects the shared `RestClient` bean into `FintClient`.
2. If `fint.kontroll.datainput=fint` and `fint.resource-gateway.authorization=enabled`, `OAuthRestClientConfiguration` attaches an `OAuth2ClientHttpRequestInterceptor` to the `RestClient`.
3. For each outgoing FINT request, the interceptor resolves the configured client registration from `fint.client.registration-id`.
4. Spring Security authorises the client using the configured FINT username and password from `fint.client.username` and `fint.client.password`.
5. The interceptor adds the resulting bearer token to the outgoing request. Refresh tokens are reused by Spring Security when available.
6. `FintClient` executes the request and deserializes the response body, for example into `ObjectResources` for collection responses.

If authorisation is disabled, or `fint.kontroll.datainput=mock` is active, the `RestClient` is created without the OAuth2 interceptor. The same `FintClient` methods are then used, but requests are sent without a bearer token.

`ObjectResources` is only a generic wrapper for FINT collection responses where the concrete resource type is not known at compile time. It does not participate in authorisation.
