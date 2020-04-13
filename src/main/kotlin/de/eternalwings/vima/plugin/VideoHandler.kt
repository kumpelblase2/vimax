package de.eternalwings.vima.plugin

typealias VideoHandlerCall = (VideoContainer) -> Unit

data class VideoHandler(val priority: PluginPriority, val call: VideoHandlerCall)

enum class PluginPriority {
    EARLIEST,
    EARLIER,
    NORMAL,
    LATER,
    LAST
}
