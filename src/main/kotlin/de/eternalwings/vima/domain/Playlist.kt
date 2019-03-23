package de.eternalwings.vima.domain

import javax.persistence.Entity
import javax.persistence.ManyToMany

@Entity
class Playlist(
    var name: String? = null,
    @ManyToMany
    var videos: MutableList<Video> = ArrayList()
) : BasePersistable<Int>()
