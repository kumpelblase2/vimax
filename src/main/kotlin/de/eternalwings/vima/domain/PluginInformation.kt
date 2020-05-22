package de.eternalwings.vima.domain

import java.time.LocalDateTime
import javax.persistence.Column
import javax.persistence.Embedded
import javax.persistence.Entity

@Entity
class PluginInformation(
        @Column(nullable = false)
        var name: String? = null,
        @Column(nullable = false)
        var enabled: Boolean = false,
        @Embedded
        var information: PluginDescription? = PluginDescription(),
        var disabledAt: LocalDateTime? = null,
        var enabledAt: LocalDateTime? = null,
        var settings: String? = "{}"
) : BasePersistable<Int>() {

    val description: PluginDescription
        get() {
            if(information == null) {
                information = PluginDescription()
            }
            return information!!
        }

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
}
