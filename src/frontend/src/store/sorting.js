import { removeFromArray } from "@/helpers/array-helper";
import videos from '../api/videos';

const APPEND_VIDEO = 'appendSortableVideo';
const CLEAR_VIDEOS = 'clearVideos';
const SKIP_VIDEO = 'skipVideo';
const SET_SORTABLE_VIDEOS = 'setSortableVideos';

const LOAD_THRESHOLD = 3;

export default {
    namespaced: true,
    state: {
        selectedMetadata: null,
        buckets: [],
        sortingVideoIds: []
    },
    getters: {
        bucket: (state) => (index) => {
            return state.buckets[index];
        },
        selectedMetadata: (state) => {
            return state.selectedMetadata;
        },
        bucketAssignedValue: (state, getters) => (index) => {
            return getters.bucket(index).value;
        },
        nextVideoId: (state) => {
            if(state.sortingVideoIds.length > 0) {
                return state.sortingVideoIds[0];
            } else {
                return null;
            }
        },
        nextVideo(state, getters, _rootState, rootGetters) {
            return rootGetters['videos/getVideo'](getters.nextVideoId);
        },
        needsToLoadMore(state, getters, _rootState, rootGetters) {
            if(LOAD_THRESHOLD >= state.sortingVideoIds.length) return false;
            return !rootGetters['videos/hasVideo'](state.sortingVideoIds[LOAD_THRESHOLD]);
        },
        empty: (state) => state.sortingVideoIds.length === 0
    },
    mutations: {
        addBucket(state) {
            state.buckets.push({ value: null });
        },
        deleteBucket(state, index) {
            state.buckets.splice(index, 1);
        },
        clearBuckets(state) {
            state.buckets = [];
        },
        updateMetadata(state, metadata) {
            state.selectedMetadata = metadata;
        },
        updateValue(state, { value, index }) {
            state.buckets[index].value = value;
        },
        [APPEND_VIDEO](state, videoId) {
            state.sortingVideoIds.push(videoId);
        },
        [CLEAR_VIDEOS](state) {
            state.sortingVideoIds = [];
        },
        [SKIP_VIDEO](state) {
            state.sortingVideoIds.shift();
        },
        [SET_SORTABLE_VIDEOS](state, videoIds) {
            videoIds.forEach(id => state.sortingVideoIds.push(id));
        },
        removeVideoFromQueue(state, videoId) {
            removeFromArray(state.sortingVideoIds, videoId);
        }
    },
    actions: {
        async loadSortableVideos({ getters, commit, dispatch }) {
            commit(CLEAR_VIDEOS);
            const availableVideoIds = await videos.getSortableVideosFor(getters.selectedMetadata);
            commit(SET_SORTABLE_VIDEOS, availableVideoIds);
            dispatch('loadNextSortableBatch');
        },
        async loadNextSortableBatch({ state, dispatch }) {
            const videosToLoad = state.sortingVideoIds.slice(0, 25);
            await dispatch('videos/loadVideos', videosToLoad, { root: true });
        },
        async assignVideoToBucket({ getters, commit, state, dispatch }, index) {
            const bucket = getters.bucket(index);
            if(bucket == null) return;
            const video = getters.nextVideo;
            const assigningMetadata = state.selectedMetadata;
            const metadataToUpdate = video.metadata[assigningMetadata.id];
            metadataToUpdate.value = bucket.value;
            await videos.saveVideo(video);
            commit(SKIP_VIDEO);
            if(getters.needsToLoadMore) {
                dispatch('loadNextSortableBatch');
            }
        },
        async assignVideoToNothing({ commit, dispatch, getters }) {
            commit(SKIP_VIDEO);
            if(getters.needsToLoadMore) {
                dispatch('loadNextSortableBatch');
            }
        },
        videoDeleteUpdate({ commit, getters }, videoId) {
            if(getters.nextVideoId === videoId) {
                commit(SKIP_VIDEO);
            }
            commit('removeVideoFromQueue', videoId);
        }
    }
}
