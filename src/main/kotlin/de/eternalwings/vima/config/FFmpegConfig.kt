package de.eternalwings.vima.config

import net.bramp.ffmpeg.FFmpeg
import net.bramp.ffmpeg.FFprobe
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class FFmpegConfig {
    @Bean
    fun ffmpeg(@Value("\${ffpmeg-binary:ffmpeg}") ffmpegLocation: String): FFmpeg = FFmpeg(ffmpegLocation)

    @Bean
    fun ffprobe(@Value("\${ffprobe-binary:ffprobe}") ffprobeLocation: String): FFprobe = FFprobe(ffprobeLocation)
}
