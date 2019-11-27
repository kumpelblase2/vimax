export function getSelectedThumbnailURLForVideo(video) {
    return getThumbnailURLForVideo(video, getSelectedThumbnail(video));
}

export function getThumbnailURLForVideo(video, thumbnail) {
    if(thumbnail == null || video == null) return "";
    return `/api/video/${video.id}/thumbnail/${thumbnail.id}`;
}

export function getStreamURLForVideo(video) {
    if(video == null) return "";
    return `/api/video/${video.id}/stream`;
}

export function getSelectedThumbnail(video) {
    if(video != null) {
        return video.thumbnails[video.selectedThumbnail];
    } else {
        return null;
    }
}
