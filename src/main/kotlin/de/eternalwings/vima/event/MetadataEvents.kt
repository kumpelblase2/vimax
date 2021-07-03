package de.eternalwings.vima.event

import org.springframework.context.ApplicationEvent

class MetadataSelectionOptionRemovedEvent(source: Any, val metadataId: Int, val removedOption: Int) : ApplicationEvent(source)

class MetadataCreateEvent(source: Any, val metadataId: Int) : ApplicationEvent(source)

class MetadataDeleteEvent(source: Any, val metadataId: Int) : ApplicationEvent(source)
