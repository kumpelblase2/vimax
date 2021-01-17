package de.eternalwings.vima.event

import de.eternalwings.vima.domain.Video
import org.springframework.context.ApplicationEvent

class VideoDeleteEvent(source: Any, val video: Video) : ApplicationEvent(source)

class VideoCreateEvent(source: Any, val video: Video) : ApplicationEvent(source)

class VideoUpdateEvent(source: Any, val video: Int) : ApplicationEvent(source)
