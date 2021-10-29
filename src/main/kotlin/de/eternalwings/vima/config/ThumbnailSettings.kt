package de.eternalwings.vima.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import java.nio.file.Path

@Configuration
class ThumbnailSettings {

    @Value("\${thumbnail-relative-dir:.thumbnails}")
    private lateinit var thumbnailDir: Path

    val relativePath: Path
        get() =  thumbnailDir

    @Value("\${thumbnail-amount:3}")
    private var thumbnails: Int = 3

    val count: Int
        get() = thumbnails

    @Value("\${sync-thumbnail-count:false}")
    private var syncThumbnailCount: Boolean = false

    val generateMissing: Boolean
        get() = syncThumbnailCount

    @Value("\${cleanup-lingering-thumbnails:false}")
    private var removeLingeringThumbnails: Boolean = false

    val removeLingering: Boolean
        get() = removeLingeringThumbnails
}
