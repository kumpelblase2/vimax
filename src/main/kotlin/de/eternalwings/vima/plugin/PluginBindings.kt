package de.eternalwings.vima.plugin

import net.bramp.ffmpeg.FFmpeg
import net.bramp.ffmpeg.FFprobe
import org.slf4j.Logger

class PluginBindings(val ffmpeg: FFmpeg, val ffprobe: FFprobe, val logger: Logger) {
}
