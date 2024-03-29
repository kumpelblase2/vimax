package de.eternalwings.vima.config

import net.bramp.ffmpeg.FFmpeg
import net.bramp.ffmpeg.FFprobe
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.io.IOException
import java.lang.IllegalStateException

@Configuration
class FFmpegConfig {
    @Bean
    fun ffmpeg(@Value("\${ffmpeg-binary:ffmpeg}") ffmpegLocation: String): FFmpeg {
        try {
            return FFmpeg(ffmpegLocation)
        } catch (exception: IOException) {
            throw IllegalStateException("Couldn't find ffmpeg binary! Specified path: $ffmpegLocation")
        }
    }

    @Bean
    fun ffprobe(@Value("\${ffprobe-binary:ffprobe}") ffprobeLocation: String): FFprobe {
        try {
            return FFprobe(ffprobeLocation)
        } catch (exception: IOException) {
            throw IllegalStateException("Couldn't find ffprobe binary! Specified path: $ffprobeLocation")
        }
    }
}
