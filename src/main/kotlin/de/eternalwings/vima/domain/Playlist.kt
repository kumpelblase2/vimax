package de.eternalwings.vima.domain

import javax.persistence.CascadeType
import javax.persistence.Entity
import javax.persistence.OneToMany
import javax.persistence.OrderColumn

@Entity
class Playlist(
        var name: String? = null,
        @OneToMany(cascade = [CascadeType.ALL], mappedBy = "playlist")
        @OrderColumn(name = "position")
        var videos: MutableList<VideoInPlaylist> = ArrayList()
) : BasePersistable<Int>() {
    fun addVideos(videos: List<Video>, startIndex: Int = 0) {
        val mappedVideos = videos.mapIndexed { index, video ->
            VideoInPlaylist(this, video, index + startIndex)
        }
        this.videos.addAll(mappedVideos)
    }
}
