package de.eternalwings.vima.config

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableScheduling

@Configuration
@EnableScheduling
@ConditionalOnProperty(name = ["watch-files"], havingValue = "true")
class EnableSchedulingConfig {
}
