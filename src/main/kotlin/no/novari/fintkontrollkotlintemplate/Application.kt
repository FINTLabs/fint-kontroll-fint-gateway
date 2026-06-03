package no.novari.no.novari.fintkontrollkotlintemplate




import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication(
    scanBasePackages = ["no.novari","no.fintlabs"]
)

@EnableScheduling
class Application
fun main(args: Array<String>) {
    runApplication<Application>(*args)
}