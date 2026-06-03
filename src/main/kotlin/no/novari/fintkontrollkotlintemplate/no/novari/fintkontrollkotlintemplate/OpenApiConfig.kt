package no.novari.no.novari.fintkontrollkotlintemplate.no.novari.fintkontrollkotlintemplate

import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType
import io.swagger.v3.oas.annotations.info.Info

import io.swagger.v3.oas.annotations.info.License
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.security.SecurityScheme
import org.springframework.context.annotation.Configuration


@SecurityScheme(
    name = "bearer-jwt",
    type = SecuritySchemeType.HTTP,
    scheme = "bearer",
    bearerFormat = "JWT",
    paramName = "Authorization",
    `in` = SecuritySchemeIn.HEADER
)

@OpenAPIDefinition(
    security = [SecurityRequirement(name = "bearer-jwt")],
    info = Info(
        title = "Kontroll Kotlin-template",
        version = "0.0.1",
        description = "REST API for Kotlin-template",
        license = License(name = "MIT")
    )
)
@Configuration
class OpenApiConfig