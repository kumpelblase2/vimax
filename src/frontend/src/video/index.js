export function getSelectedThumbnailURLForVideo(video) {
    return getThumbnailURL(getSelectedThumbnail(video));
}

export function getThumbnailURL(thumbnail) {
    if(thumbnail == null) return "";
    return `/api/video/thumbnail/${thumbnail}`;
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
