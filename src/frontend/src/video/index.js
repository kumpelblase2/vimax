export function getThumbnailURLForVideo(videoId, thumbnail) {
    return `/api/video/${videoId}/thumbnail/${thumbnail.id}`;
}

export function getStreamURLForVideo(videoId) {
    return `/api/video/${videoId}/stream`;
}
