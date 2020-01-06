import videoApi from "../api/videos";
import { getSelectedThumbnail } from "../video";

const APPEND_VIDEO = 'addOrUpdateVideo';
const CLEAR_VIDEOS = 'clearVideos';
const SET_EDITING_VIDEO = 'editVideo';

const SELECT_VIDEO = 'selectVideo';
const UNSELECT_VIDEO = 'unselectVideo';

const MAX_VIDEOS = 80;

export const MUTATIONS = { APPEND_VIDEO, CLEAR_VIDEOS, SET_EDITING_VIDEO, SELECT_VIDEO, UNSELECT_VIDEO };

export default {
    namespaced: true,
    state: {
        videos: [],
        editingVideo: null,
        selectedVideoIds: [],
        isLoading: false,
        displayVideoIds: [],
        hasMoreVideos: true,
        currentPage: 0
    },
    getters: {
        selectedVideos: (state) => {
            return state.videos.filter(video => state.selectedVideoIds.includes(video.id));
        },
        getVideo: (state) => {
            return (id) => state.videos.find(video => video.id === id);
        },
        videoThumbnail: (state, getters) => {
            return (videoId) => getters.thumbnailOf(videoId);
        },
        thumbnailOf: (state, getters) => {
            return (id) => {
                const video = getters.getVideo(id);
                return getSelectedThumbnail(video);
            }
        },
        isSelected: (state) => {
            return (id) => state.selectedVideoIds.includes(id);
        },
        hasVideo: (state) => {
            return (id) => state.videos.findIndex(video => video.id === id) >= 0;
        },
        displayedVideos: (state,getters) => {
            return state.displayVideoIds.map(id => getters.getVideo(id));
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
        [SET_EDITING_VIDEO](state, videoId) {
            if(videoId == null) {
                state.editingVideo = null;
            } else {
                state.editingVideo = Object.assign({}, state.videos.find(video => video.id === videoId));
            }
        },
        [SELECT_VIDEO](state, videoId) {
            if(state.selectedVideoIds.indexOf(videoId) < 0) {
                state.selectedVideoIds.push(videoId);
            }
        },
        [UNSELECT_VIDEO](state, videoId) {
            const index = state.selectedVideoIds.indexOf(videoId);
            if(index >= 0) {
                state.selectedVideoIds.splice(index, 1);
            }
        },
        clearSelectedVideos(state) {
            state.selectedVideoIds = [];
        },
        changeThumbnailsInEdit(state, thumbnailIndex) {
            state.editingVideo.selectedThumbnail = thumbnailIndex;
        },
        setEditingMetadataValue(state, { id, value }) {
            state.editingVideo.metadata[id].value = value;
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
            state.displayVideoIds = [];
        },
        setLoading(state, value) {
            state.isLoading = value;
        },
        nextPage(state) {
            state.currentPage += 1;
        },
        resetPage(state) {
            state.currentPage = 0;
            state.hasMoreVideos = true;
        },
        addDisplayVideos(state, videoIds) {
            videoIds.forEach(id => state.displayVideoIds.push(id));
        },
        noMoreVideos(state) {
            state.hasMoreVideos = false;
        }
    },
    actions: {
        toggleSelectVideo({ getters, commit }, videoId) {
            if(getters.isSelected(videoId)) {
                commit('unselectVideo', videoId);
            } else {
                commit('selectVideo', videoId);
            }
        },
        async loadVideosOfCurrentPage({ commit, state, dispatch, rootState }) {
            commit('setLoading', true);
            const videoIds = await videoApi.getVideosByPage(state.currentPage, rootState.search.query, rootState.search.sort.property, rootState.search.sort.direction);
            commit('nextPage');
            await dispatch('loadVideos', videoIds);
            commit('addDisplayVideos', videoIds);
            if(videoIds.length < MAX_VIDEOS) {
                commit('noMoreVideos');
            }
            commit('setLoading', false);
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
        setEditingMetadataValue({ commit }, { id, value }) {
            commit('setEditingMetadataValue', { id, value });
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
        },
        async reloadVideo({ commit }, videoId) {
            const video = await videoApi.getVideo(videoId);
            commit(APPEND_VIDEO, video);
        },
        async loadVideos({ commit, getters }, videoIds = []) {
            const videosToLoad = videoIds.filter(videoId => !getters.hasVideo(videoId));
            if(videosToLoad.length > 0) {
                const videos = await videoApi.getVideosById(videosToLoad);
                videos.forEach(video => {
                    commit(APPEND_VIDEO, video);
                });
            }
        }
    }
};
