package de.eternalwings.vima.plugin

typealias VideoHandlerCall = (VideoContainer) -> Unit
typealias CallbackVideoHandlerCall = (VideoContainer, () -> Unit) -> Unit

data class VideoHandler(val priority: PluginPriority, val call: VideoHandlerCall)
data class AsyncVideoHandler(val call: CallbackVideoHandlerCall)

enum class PluginPriority {
    EARLIEST,
    EARLIER,
    NORMAL,
    LATER,
    LAST
}
