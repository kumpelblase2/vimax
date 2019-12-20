package de.eternalwings.vima.domain

import java.time.LocalDateTime
import javax.persistence.Column
import javax.persistence.Entity

@Entity
class PluginInformation(
        @Column(nullable = false)
        var name: String? = null,
        @Column(nullable = false)
        var enabled: Boolean = false,
        var disabledAt: LocalDateTime? = null,
        var enabledAt: LocalDateTime? = null,
        var settings: String? = "{}"
) : BasePersistable<Int>() {
    fun enable() {
        enabled = true
        disabledAt = null
        enabledAt = LocalDateTime.now()
    }

    fun disable() {
        enabled = false
        disabledAt = LocalDateTime.now()
        enabledAt = null
    }

    fun copy(): PluginInformation {
        return PluginInformation(name, enabled, disabledAt, enabledAt, settings)
    }
}
