import videos from '../api/videos';

const APPEND_VIDEO = 'appendSortableVideo';
const CLEAR_VIDEOS = 'clearVideos';
const SKIP_VIDEO = 'skipVideo';
const MARK_REMAINING = 'markRemaining';

export default {
    namespaced: true,
    state: {
        selectedMetadata: null,
        buckets: [],
        sortingVideoQueue: [],
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
        [MARK_REMAINING](state, value) {
            state.remaining = value;
        }
    },
    actions: {
        async loadSortableVideos({ getters, commit }) {
            commit(CLEAR_VIDEOS);
            const availableVideos = await videos.getSortableVideosFor(getters.selectedMetadata);
            availableVideos.forEach(video => {
                commit(APPEND_VIDEO, video);
            });

            commit(MARK_REMAINING, availableVideos.length > 0);
        },
        async assignVideoToBucket({ getters, commit, state, dispatch }, index) {
            const bucket = getters.bucket(index);
            const video = state.sortingVideoQueue[0];
            const assigningMetadata = state.selectedMetadata;
            const metadataToUpdate = video.metadata.find(metadata => metadata.metadata.id === assigningMetadata.id);
            metadataToUpdate.value = bucket.value;
            await videos.saveVideo(video);
            commit(SKIP_VIDEO);
            if(state.sortingVideoQueue.length === 0) {
                dispatch('loadSortableVideos');
            }
        },
        async assignVideoToNothing({ commit, dispatch }) {
            commit(SKIP_VIDEO);
            if(state.sortingVideoQueue.length === 0) {
                dispatch('loadSortableVideos');
            }
        }
    }
}
