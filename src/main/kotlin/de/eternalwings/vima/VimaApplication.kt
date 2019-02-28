package de.eternalwings.vima

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class VimaApplication

fun main(args: Array<String>) {
    runApplication<VimaApplication>(*args)
}
