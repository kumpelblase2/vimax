package de.eternalwings.vima

import de.eternalwings.vima.process.BrowserOpener
import org.slf4j.LoggerFactory
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.core.env.Environment
import java.net.URL

@SpringBootApplication
class VimaApplication(private val env: Environment) : ApplicationRunner {
    override fun run(args: ApplicationArguments?) {
        if (shouldOpenWebpage(args)) {
            LOGGER.info("Opening browser")
            BrowserOpener.openUrl(getInterfaceUrl())
        }
    }

    private fun getInterfaceUrl(): URL {
        val isDev = !env.activeProfiles.contains("production")
        val port = if (isDev) 8081 else env.getRequiredProperty("local.server.port").toInt()
        return URL("http", "localhost", port, "/")
    }

    private fun shouldOpenWebpage(args: ApplicationArguments?): Boolean {
        val browserArgument = args?.getOptionValues("no-browser")
        return browserArgument == null
    }

    companion object {
        private val LOGGER = LoggerFactory.getLogger(VimaApplication::class.java)
    }
}

fun main(args: Array<String>) {
    runApplication<VimaApplication>(*args)
}
