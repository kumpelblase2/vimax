import videos from "../api/videos";

export default {
    namespaced: true,
    state: {
        query: "",
        remainingVideoIds: [],
        current: null
    },
    getters: {
        filter(state) {
            return state.query;
        },
        currentVideo(state) {
            return state.current;
        }
    },
    actions: {
        async updateFilter({ commit, state, dispatch }, value) {
            commit('changeFilter', value);
            const ids = await videos.getAllIds(value);
            commit('updateRemaining', ids);
            dispatch('nextVideo');
        },
        async nextVideo({ commit, state, dispatch }) {
            if(state.remainingVideoIds.length > 0) {
                const next = state.remainingVideoIds[0];
                await dispatch('videos/editing/editVideo', next, { root: true });
                commit('updateNext');
            } else {
                commit('updateNext');
            }
        },
        async restartEditingIfPossible({ state, dispatch }) {
            if(state.current) {
                await dispatch('videos/editing/editVideo', state.current, { root: true });
            }
        },
        videoDeleteUpdate({ dispatch, commit, getters }, videoId) {
            if(getters.currentVideo != null && getters.currentVideo.id === videoId) {
                dispatch('nextVideo');
            }
            commit('removeVideoFromQueue', videoId);
        }
    },
    mutations: {
        changeFilter(state, value) {
            state.query = value;
        },
        updateRemaining(state, videos) {
            state.remainingVideoIds = videos;
        },
        updateNext(state) {
            if(state.remainingVideoIds.length > 0) {
                state.current = state.remainingVideoIds.shift();
            } else {
                state.current = null;
            }
        },
        removeVideoFromQueue(state, videoId) {
            const index = state.remainingVideoIds.indexOf(videoId);
            if(index >= 0) {
                state.remainingVideoIds.splice(index, 1);
            }
        }
    }
};
