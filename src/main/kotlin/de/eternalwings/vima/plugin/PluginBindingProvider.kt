package de.eternalwings.vima.plugin

import net.bramp.ffmpeg.FFmpeg
import net.bramp.ffmpeg.FFprobe
import org.springframework.stereotype.Component

@Component
class PluginBindingProvider(private val ffmpeg: FFmpeg, private val ffprobe: FFprobe) {

    fun createBindings(): PluginBindings {
        return PluginBindings(ffmpeg, ffprobe)
    }

}
