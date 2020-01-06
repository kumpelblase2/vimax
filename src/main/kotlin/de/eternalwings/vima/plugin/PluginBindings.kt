package de.eternalwings.vima.plugin

import net.bramp.ffmpeg.FFmpeg
import net.bramp.ffmpeg.FFprobe
import org.springframework.stereotype.Component
import javax.script.Bindings
import javax.script.SimpleBindings

@Component
class PluginBindings(private val ffmpeg: FFmpeg, private val ffprobe: FFprobe) {

    fun createBindings(): PluginExecutionContext {
        return PluginExecutionContext(ffmpeg, ffprobe)
    }

}
