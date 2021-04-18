package de.eternalwings.vima.domain

import javax.persistence.Column
import javax.persistence.Embeddable

@Embeddable
class PluginDescription {
    var description: String? = null
    var author: String? = null
    @Column(name = "PLUGIN_VERSION")
    var version: String? = null

    fun copy(): PluginDescription {
        return PluginDescription().also {
            it.description = this.description
            it.author = this.author
            it.version = this.version
        }
    }
}
