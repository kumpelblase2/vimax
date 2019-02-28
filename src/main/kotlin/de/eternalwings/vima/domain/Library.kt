package de.eternalwings.vima.domain

import javax.persistence.Entity

@Entity
data class Library(
    var path: String? = null
) : BasePersistable<Int>()
