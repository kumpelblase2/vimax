package de.eternalwings.vima.domain

import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType.IDENTITY
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
    @GeneratedValue(strategy = IDENTITY)
    private var id: Int? = null
}
