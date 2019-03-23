package de.eternalwings.vima.domain

import javax.persistence.Column
import javax.persistence.Entity

@Entity
data class Library(
        @field:Column(updatable = false)
        var path: String? = null
) : BasePersistable<Int>()
