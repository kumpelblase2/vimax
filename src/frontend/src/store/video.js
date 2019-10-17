import videoApi from "../api/videos";

const APPEND_VIDEO = 'addOrUpdateVideo';
const CLEAR_VIDEOS = 'clearVideos';
const SET_ACTIVE_VIDEO = 'updateActiveVideo';
const SET_EDITING_VIDEO = 'editVideo';

const SELECT_VIDEO = 'selectVideo';
const UNSELECT_VIDEO = 'unselectVideo';

export const MUTATIONS = { APPEND_VIDEO, CLEAR_VIDEOS, SET_ACTIVE_VIDEO, SET_EDITING_VIDEO, SELECT_VIDEO, UNSELECT_VIDEO };

export default {
    namespaced: true,
    state: {
        videos: [],
        activeVideoId: null,
        editingVideo: null,
        selectedVideos: [],
        isLoading: false,
        currentPage: 0
    },
    getters: {
        activeVideo: (state, getters) => {
            return getters.getVideo(state.activeVideoId);
        },
        getVideo: (state) => {
            return (id) => state.videos.find(video => video.id === id);
        },
        editingVideo: (state, getters) => {
            return getters.getVideo(state.editingId);
        },
        videoThumbnail: (state, getters) => {
            return (videoId) => getters.thumbnailOf(videoId);
        },
        thumbnailOf: (state, getters) => {
            return (id) => {
                const video = getters.getVideo(id);
                if(video != null) {
                    return video.thumbnails[video.selectedThumbnail];
                } else {
                    return null;
                }
            }
        }
    },
    mutations: {
        [APPEND_VIDEO](state, video) {
            const index = state.videos.findIndex(existing => existing.id === video.id);
            if(index >= 0) {
                Object.assign(state.videos[index], video);
            } else {
                state.videos.push(video);
            }
        },
        [SET_ACTIVE_VIDEO](state, videoId) {
            state.activeVideoId = videoId;
        },
        [SET_EDITING_VIDEO](state, videoId) {
            if(videoId == null) {
                state.editingVideo = null;
            } else {
                state.editingVideo = Object.assign({}, state.videos.find(video => video.id === videoId));
            }
        },
        [SELECT_VIDEO](state, videoId) {
            if(state.selectedVideos.findIndex(videoId) < 0) {
                state.selectedVideos.push(videoId);
            }
        },
        [UNSELECT_VIDEO](state, videoId) {
            const index = state.selectedVideos.findIndex(videoId);
            if(index >= 0) {
                state.selectedVideos.splice(index, 1);
            }
        },
        changeThumbnailsInEdit(state, thumbnailIndex) {
            state.editingVideo.selectedThumbnail = thumbnailIndex;
        },
        setEditingMetadataValue(state, { index, value }) {
            state.editingVideo.metadata[index].value.value = value;
        },
        updateThumbnail(state, { videoId, thumbnails }) {
            const video = state.videos.find(video => video.id === videoId);
            if(video != null) {
                video.thumbnails = thumbnails;
            }
        },
        updateEditingThumbnails(state, thumbnails) {
            state.editingVideo.thumbnails = thumbnails;
        },
        [CLEAR_VIDEOS](state) {
            while(state.videos.length) {
                state.videos.pop();
            }
        },
        setLoading(state, value) {
            state.isLoading = value;
        },
        nextPage(state) {
            state.currentPage += 1;
        },
        resetPage(state) {
            state.currentPage = 0;
        }
    },
    actions: {
        async loadRecentVideos({ commit }) {
            commit('setLoading', true);
            const videos = await videoApi.getRecentVideos();
            videos.forEach(video => {
                commit(APPEND_VIDEO, video);
            });
            commit('setLoading', false);
        },
        async loadAllVideos({ commit }) {
            commit('setLoading', true);
            const videos = await videoApi.getAllVideos();
            videos.forEach(video => {
                commit(APPEND_VIDEO, video);
            });
            commit('setLoading', false);
        },
        async loadVideosOfCurrentPage({ commit, state, rootState }) {
            commit('setLoading', true);
            const videos = await videoApi.getVideosByPage(state.currentPage, rootState.search.query, rootState.search.sort.property, rootState.search.sort.direction);
            commit('nextPage');
            videos.forEach(video => {
                commit(APPEND_VIDEO, video);
            });
            commit('setLoading', false);
        },
        async makeVideoActive({ commit }, videoId) {
            commit(SET_ACTIVE_VIDEO, videoId);
            const video = await videoApi.getVideo(videoId);
            commit(APPEND_VIDEO, video);
        },
        async editVideo({ commit }, videoId) {
            commit(SET_EDITING_VIDEO, videoId);
            if(videoId != null) {
                const video = await videoApi.getVideo(videoId);
                commit(APPEND_VIDEO, video);
            }
        },
        async saveEditingVideo({ commit, state }) {
            const editedVideo = await videoApi.saveVideo(state.editingVideo);
            commit(APPEND_VIDEO, editedVideo);
        },
        changeSelectedThumbnail({ commit }, thumbnailIndex) {
            commit('changeThumbnailsInEdit', thumbnailIndex);
        },
        setEditingMetadataValue({ commit }, { index, value }) {
            commit('setEditingMetadataValue', { index, value });
        },
        async refreshThumbnails({ commit, state }) {
            const newThumbnails = await videoApi.refreshThumbnails(state.editingVideo);
            commit('updateThumbnail', { videoId: state.editingVideo.id, thumbnails: newThumbnails });
            commit('updateEditingThumbnails', newThumbnails);
        },
        async search({ commit, dispatch }) {
            commit(CLEAR_VIDEOS);
            commit('resetPage');
            await dispatch('loadVideosOfCurrentPage');
        }
    }
};
