import videoApi from "../api/videos";
import videoEditing from "./videoEditing";
import { getSelectedThumbnail } from "../video";

const UPDATE_VIDEO = 'addOrUpdateVideo';
const CLEAR_VIDEOS = 'clearVideos';

const SELECT_VIDEO = 'selectVideo';
const UNSELECT_VIDEO = 'unselectVideo';

const MAX_VIDEOS = 50;

export const MUTATIONS = { UPDATE_VIDEO, CLEAR_VIDEOS, SELECT_VIDEO, UNSELECT_VIDEO };

export default {
    namespaced: true,
    modules: {
        editing: videoEditing
    },
    state: {
        videos: [],
        selectedVideoIds: [],
        isLoading: false,
        displayVideoIds: [],
        searchResultVideoIds: [],
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
        hasVideosToDisplay(state) {
            return state.displayVideoIds.length > 0;
        },
        hasVideosSelected(state) {
            return state.selectedVideoIds.length > 0;
        },
        searchedVideosAtPage: (state) => {
            return (page, size) => state.searchResultVideoIds.slice(page * size, page * size + size);
        }
    },
    mutations: {
        [UPDATE_VIDEO](state, video) {
            const index = state.videos.findIndex(existing => existing.id === video.id);
            if(index >= 0) {
                Object.assign(state.videos[index], video);
            } else {
                state.videos.push(video);
            }
        },
        addVideos(state, videos) {
            state.videos.push(...videos);
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
        updateThumbnail(state, { videoId, thumbnails }) {
            const video = state.videos.find(video => video.id === videoId);
            if(video != null) {
                video.thumbnails = thumbnails;
            }
        },
        [CLEAR_VIDEOS](state) {
            state.displayVideoIds = [];
            state.selectedVideoIds = [];
            state.searchResultVideoIds = [];
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
            state.displayVideoIds = [];
        },
        addDisplayVideos(state, videoIds) {
            state.displayVideoIds.push(...videoIds);
        },
        noMoreVideos(state) {
            state.hasMoreVideos = false;
        },
        selectAllVideos(state) {
            state.selectedVideoIds = [...state.displayVideoIds];
        },
        addSearchResultIds(state, ids) {
            state.searchResultVideoIds.push(...ids);
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
        async loadVideosOfCurrentPage({ commit, state, dispatch, getters }) {
            commit('setLoading', true);
            const videoIds = getters.searchedVideosAtPage(state.currentPage, 50);
            commit('nextPage');
            await dispatch('loadVideos', videoIds);
            commit('addDisplayVideos', videoIds);
            if(videoIds.length < MAX_VIDEOS) {
                commit('noMoreVideos');
            }
            commit('setLoading', false);
        },
        async search({ commit, dispatch, rootState }) {
            let videoIds;
            try {
                videoIds = await videoApi.getVideosByPage(rootState.search.query, rootState.search.sort.property, rootState.search.sort.direction);
                commit('search/updateQueryError', "", { root: true });
            } catch(exception) {
                // Save error
                commit('search/updateQueryError', "Invalid query.", { root: true });
                return;
            }
            commit(CLEAR_VIDEOS);
            commit('resetPage');
            commit('addSearchResultIds', videoIds);
            await dispatch('loadVideosOfCurrentPage');
        },
        async reloadVideo({ commit }, videoId) {
            const video = await videoApi.getVideo(videoId);
            commit(UPDATE_VIDEO, video);
        },
        async loadVideos({ commit, getters }, videoIds = []) {
            const videosToLoad = videoIds.filter(videoId => !getters.hasVideo(videoId));
            if(videosToLoad.length > 0) {
                const videos = await videoApi.getVideosById(videosToLoad);
                commit('addVideos', videos);
            }
        }
    }
};
