package de.eternalwings.vima.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration

@Configuration
class QuerySettings {

    @Value("\${duration-query-tolerance:3}")
    private var toleranceValue: Short = 3

    val durationTolerance: Short
        get() = toleranceValue

}
