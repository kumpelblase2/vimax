package de.eternalwings.vima.plugin

import net.bramp.ffmpeg.FFmpeg
import net.bramp.ffmpeg.FFprobe
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class DefaultPluginBindingProvider(private val ffmpeg: FFmpeg, private val ffprobe: FFprobe): PluginBindingsProvider {

    override fun createBindingsFor(name: String): PluginBindings {
        return PluginBindings(ffmpeg, ffprobe, LoggerFactory.getLogger("plugin-${name}"))
    }

}
