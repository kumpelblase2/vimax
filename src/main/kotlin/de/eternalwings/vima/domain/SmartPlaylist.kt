package de.eternalwings.vima.domain

import org.springframework.data.domain.Sort.Direction
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.EnumType.STRING
import javax.persistence.Enumerated

@Entity
class SmartPlaylist(
    @Column(nullable = false)
    var name: String? = null,
    @Column(nullable = false)
    var query: String? = null,
    var orderBy: String? = null,
    @Enumerated(STRING)
    var orderDirection: Direction? = null
) : BasePersistable<Int>()
