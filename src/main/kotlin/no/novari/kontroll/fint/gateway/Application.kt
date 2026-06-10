package no.novari.kontroll.fint.gateway

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@EnableScheduling
@ConfigurationPropertiesScan(basePackages = ["no.novari.kontroll.fint.gateway"])
@SpringBootApplication(
    scanBasePackages = ["no.novari", "no.fintlabs"],
)
class Application

fun main(args: Array<String>) {
    runApplication<Application>(*args)
}
