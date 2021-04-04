package de.eternalwings.vima.domain

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.ManyToOne
import javax.persistence.Table

@Entity
@Table(name = "playlist_videos")
class VideoInPlaylist(
        @ManyToOne
        var playlist: Playlist? = null,
        @ManyToOne
        var video: Video? = null,
        var position: Int? = null
) {
    @Id
    @GeneratedValue
    @Column(updatable = false, nullable = false)
    private var id: Int? = null
}
