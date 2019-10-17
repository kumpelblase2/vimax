import videos from '../api/videos';

const APPEND_VIDEO = 'appendSortableVideo';
const CLEAR_VIDEOS = 'clearVideos';
const SKIP_VIDEO = 'skipVideo';
const SET_SORTABLE_VIDEOS = 'setSortableVideos';

export default {
    namespaced: true,
    state: {
        selectedMetadata: null,
        buckets: [],
        sortingVideoQueue: [],
        sortingVideoIds: [],
        remaining: true
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
        nextVideo: (state) => {
            if(state.sortingVideoQueue.length > 0) {
                return state.sortingVideoQueue[0];
            } else {
                return null;
            }
        },
        empty: (state) => !state.remaining && state.sortingVideoQueue.length === 0
    },
    mutations: {
        addBucket(state) {
            state.buckets.push({ value: null });
        },
        deleteBucket(state, index) {
            state.buckets.splice(index, 1);
        },
        clearBuckets(state) {
            while(state.buckets.length > 0) {
                state.buckets.pop();
            }
        },
        updateMetadata(state, metadata) {
            state.selectedMetadata = metadata;
        },
        updateValue(state, { value, index }) {
            state.buckets[index].value = value;
        },
        [APPEND_VIDEO](state, video) {
            state.sortingVideoQueue.push(video);
        },
        [CLEAR_VIDEOS](state) {
            while(state.sortingVideoQueue.length > 0) {
                state.sortingVideoQueue.shift();
            }
        },
        [SKIP_VIDEO](state) {
            state.sortingVideoQueue.shift();
        },
        [SET_SORTABLE_VIDEOS](state,value) {
            state.sortingVideoIds = value;
            state.remaining = value.length > 0;
        },
        updateCurrentVideo(state,video) {
            Object.assign(state.sortingVideoQueue[0], video);
        }
    },
    actions: {
        async reloadVideo({state,commit}) {
            const video = await videos.getVideo(state.sortingVideoQueue[0].id);
            commit('updateCurrentVideo', video);
        },
        async loadSortableVideos({ getters, commit, dispatch }) {
            commit(CLEAR_VIDEOS);
            const availableVideoIds = await videos.getSortableVideosFor(getters.selectedMetadata);
            commit(SET_SORTABLE_VIDEOS, availableVideoIds);
            dispatch('loadNextSortableBatch');
        },
        async loadNextSortableBatch({commit, state}) {
            const videosToLoad = state.sortingVideoIds.slice(0, 25);
            const remainingVideoIds = state.sortingVideoIds.slice(25);
            commit(SET_SORTABLE_VIDEOS, remainingVideoIds);
            const loadedVideos = await videos.getVideosById(videosToLoad);
            loadedVideos.forEach(video => commit(APPEND_VIDEO, video));
        },
        async assignVideoToBucket({ getters, commit, state, dispatch }, index) {
            const bucket = getters.bucket(index);
            if(bucket == null) return;
            const video = state.sortingVideoQueue[0];
            const assigningMetadata = state.selectedMetadata;
            const metadataToUpdate = video.metadata.find(metadata => metadata.definition.id === assigningMetadata.id);
            metadataToUpdate.value.value = bucket.value;
            await videos.saveVideo(video);
            commit(SKIP_VIDEO);
            if(state.sortingVideoQueue.length === 0) {
                dispatch('loadNextSortableBatch');
            }
        },
        async assignVideoToNothing({ commit, dispatch, state }) {
            commit(SKIP_VIDEO);
            if(state.sortingVideoQueue.length === 0) {
                dispatch('loadNextSortableBatch');
            }
        }
    }
}
