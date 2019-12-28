package de.eternalwings.vima.event

import de.eternalwings.vima.domain.Library
import org.springframework.context.ApplicationEvent

class LibraryCreateEvent(source: Any, val library: Library) : ApplicationEvent(source)

class LibraryDeleteEvent(source: Any, val library: Library) : ApplicationEvent(source)