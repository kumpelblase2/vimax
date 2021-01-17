import { removeFromArray, removeFromArrayWhere } from "@/helpers/array-helper";
import videoApi from "../api/videos";
import videoEditing from "./videoEditing";
import { getSelectedThumbnail } from "@/video";

const UPDATE_VIDEO = 'addOrUpdateVideo';
const CLEAR_VIDEOS = 'clearVideos';

const SELECT_VIDEO = 'selectVideo';
const UNSELECT_VIDEO = 'unselectVideo';

const shouldLoad = (state, videoId) => {
    return state.videos.find(video => video.id === videoId) == null && !state.loadingVideoIds.includes(videoId);
}

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
        displayedVideoId: null,
        loadingVideoIds: [],
        loaderTimeout: null
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
        shouldShowVideoInfo: (state) => {
            return !!state.displayedVideoId;
        },
        videoInfoId: (state) => {
            return state.displayedVideoId;
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
            const loadedIds = videos.map(video => video.id);
            loadedIds.forEach(id => {
                removeFromArray(state.loadingVideoIds, id);
            });
            state.videos.push(...videos);
        },
        [SELECT_VIDEO](state, videoId) {
            if(state.selectedVideoIds.indexOf(videoId) < 0) {
                state.selectedVideoIds.push(videoId);
            }
        },
        [UNSELECT_VIDEO](state, videoId) {
            removeFromArray(state.selectedVideoIds, videoId);
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
        },
        setLoading(state, value) {
            state.isLoading = value;
        },
        resetPage(state) {
            state.displayVideoIds = [];
        },
        setDisplayVideos(state, videoIds) {
            state.displayVideoIds = videoIds;
        },
        selectAllVideos(state) {
            state.selectedVideoIds = [...state.displayVideoIds];
        },
        removeVideo(state, id) {
            removeFromArrayWhere(state.videos, video => video.id === id);
        },
        displayVideo(state, id) {
            state.displayedVideoId = id;
        },
        startLoadingVideos(state, ids) {
            state.loadingVideoIds.push(...ids);
        },
        removeVideoFromDisplay(state, id) {
            removeFromArray(state.displayVideoIds, id);
            removeFromArray(state.loadingVideoIds, id);
            removeFromArray(state.selectedVideoIds, id);
        },
        setupLoader(state, timer) {
            state.loaderTimeout = timer;
        },
        markVideosLoaded(state, videoIds) {
            videoIds.forEach(id => removeFromArray(state.loadingVideoIds, id));
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
        async search({ commit, rootState }) {
            let videoIds;
            try {
                videoIds = await videoApi.getVideosMatchingQuery(rootState.search.query, rootState.search.sort.property, rootState.search.sort.direction);
                commit('search/updateQueryError', "", { root: true });
            } catch(exception) {
                // Save error
                commit('search/updateQueryError', "Invalid query.", { root: true });
                return;
            }
            commit(CLEAR_VIDEOS);
            commit('setDisplayVideos', videoIds);
        },
        async reloadVideo({ commit }, videoId) {
            const video = await videoApi.getVideo(videoId);
            commit(UPDATE_VIDEO, video);
        },
        async loadVideos({ commit, state, getters }, videoIds = []) {
            const videosToLoad = videoIds.filter(videoId => shouldLoad(state, videoId));
            if(videosToLoad.length > 0) {
                commit('startLoadingVideos', videosToLoad);
                if(state.loaderTimeout == null) {
                    commit('setupLoader', setTimeout(() => {
                        videoApi.getVideosById(state.loadingVideoIds).then(videos => {
                            commit('markVideosLoaded', videos.map(video => video.id));
                            commit('setupLoader', null);
                            commit('addVideos', videos);
                        });
                    }, 500));
                }
            }
        },
        videoDeleteUpdate({commit}, videoId) {
            commit('removeVideoFromDisplay', videoId);
            commit('removeVideo', videoId);
        },
        async videoUpdateUpdate({dispatch}, videoId) {
            dispatch('reloadVideo', videoId);
        }
    }
};
