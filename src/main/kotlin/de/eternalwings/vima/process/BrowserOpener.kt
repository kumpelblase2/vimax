package de.eternalwings.vima.process

import org.slf4j.LoggerFactory
import java.awt.Desktop
import java.net.URL

object BrowserOpener {

    private val LOGGER = LoggerFactory.getLogger(BrowserOpener::class.java)

    fun openUrl(url: URL) {
        if (Desktop.isDesktopSupported()) {
            LOGGER.debug("Desktop supported, opening url that way.")
            Desktop.getDesktop().browse(url.toURI())
        } else {
            val os = System.getProperty("os.name")
            val runtime = Runtime.getRuntime()
            val command = when {
                os.indexOf("win", ignoreCase = true) >= 0 -> "rundll32 url.dll,FileProtocolHandler $url"
                os.indexOf("mac", ignoreCase = true) >= 0 -> "open $url"
                os.indexOf("nux", ignoreCase = true) >= 0 || os.indexOf("nix", ignoreCase = true) >= 0 -> "xdg-open $url"
                else -> throw IllegalStateException("Could not figure out OS")
            }

            LOGGER.debug("Desktop not supported, using '$command' to launch browser.")
            runtime.exec(command)
        }
    }
}
