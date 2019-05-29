import videoApi from "../api/videos";

export default {
    namespaced: true,
    state: {
        videos: [],
        activeVideoId: null,
        editingVideo: null,
        selectedVideos: []
    },
    getters: {
        activeVideo(state, getters) {
            return getters.getVideo(state.activeVideoId);
        },
        getVideo(state) {
            return (id) => state.videos.find(video => video.id === id);
        },
        editingVideo(state, getters) {
            return getters.getVideo(state.editingId);
        },
        videoThumbnail: (state, getters) => {
            return (videoId) => getters.thumbnailOf(videoId);
        },
        thumbnailOf(state, getters) {
            return (id) => {
                const video = getters.getVideo(id);
                return video.thumbnails[video.selectedThumbnail];
            }
        }
    },
    mutations: {
        addOrUpdateVideo(state, video) {
            const index = state.videos.findIndex(existing => existing.id === video.id);
            if(index >= 0) {
                Object.assign(state.videos[index], video);
            } else {
                state.videos.push(video);
            }
        },
        updateActiveVideo(state, videoId) {
            state.activeVideoId = videoId;
        },
        editVideo(state, videoId) {
            if(videoId == null) {
                state.editingVideo = null;
            } else {
                state.editingVideo = Object.assign({}, state.videos.find(video => video.id === videoId));
            }
        },
        selectVideo(state, videoId) {
            if(state.selectedVideos.findIndex(videoId) < 0) {
                state.selectedVideos.push(videoId);
            }
        },
        unselectVideo(state, videoId) {
            const index = state.selectedVideos.findIndex(videoId);
            if(index >= 0) {
                state.selectedVideos.splice(index, 1);
            }
        },
        changeThumbnailsInEdit(state, thumbnailIndex) {
            state.editingVideo.selectedThumbnail = thumbnailIndex;
        },
        setEditingMetadataValue(state, { index, value }) {
            state.editingVideo.metadata[index].value = value;
        },
        updateThumbnail(state, {videoId, thumbnails}){
            const video = state.videos.find(video => video.id === videoId);
            if(video != null) {
                video.thumbnails = thumbnails;
            }
        },
        updateEditingThumbnails(state, thumbnails) {
            state.editingVideo.thumbnails = thumbnails;
        },
        clearVideos(state) {
            while(state.videos.length) {
                state.videos.pop();
            }
        }
    },
    actions: {
        async loadRecentVideos({ commit }) {
            const videos = await videoApi.getRecentVideos();
            videos.forEach(video => {
                commit('addOrUpdateVideo', video);
            });
        },
        async loadAllVideos({ commit }) {
            const videos = await videoApi.getAllVideos();
            videos.forEach(video => {
                commit('addOrUpdateVideo', video);
            });
        },
        async makeVideoActive({ commit }, videoId) {
            commit('updateActiveVideo', videoId);
            const video = await videoApi.getVideo(videoId);
            commit('addOrUpdateVideo', video);
        },
        async editVideo({ commit }, videoId) {
            commit('editVideo', videoId);
            if(videoId != null) {
                const video = await videoApi.getVideo(videoId);
                commit('addOrUpdateVideo', video);
            }
        },
        async saveEditingVideo({ commit, state }) {
            const editedVideo = await videoApi.saveVideo(state.editingVideo);
            commit('addOrUpdateVideo', editedVideo);
        },
        selectVideo({ commit }, videoId) {
            commit('selectVideo', videoId);
        },
        unselectVideo({ commit }, videoId) {
            commit('unselectVideo', videoId);
        },
        changeSelectedThumbnail({ commit }, thumbnailIndex) {
            commit('changeThumbnailsInEdit', thumbnailIndex);
        },
        setEditingMetadataValue({ commit }, { index, value }) {
            commit('setEditingMetadataValue', { index, value });
        },
        async refreshThumbnails({ commit, state }) {
            const newThumbnails = await videoApi.refreshThumbnails(state.editingVideo);
            commit('updateThumbnail', {videoId: state.editingVideo.id, thumbnails: newThumbnails});
            commit('updateEditingThumbnails', newThumbnails);
        },
        async search({commit}, query) {
            const videos = await videoApi.search(query);
            commit('clearVideos');
            videos.forEach(video => {
                commit('addOrUpdateVideo', video);
            });
        }
    }
};
