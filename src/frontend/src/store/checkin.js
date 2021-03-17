import videoApi from "@/api/videos";
import { removeFromArray } from "@/helpers/array-helper";

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
            if(state.query === value) {
                return;
            }

            commit('changeFilter', value);
            await dispatch('refreshQueue');
        },
        async refreshQueue({ state, commit, dispatch }) {
            const ids = await videoApi.getVideosMatchingQuery(state.query);
            commit('updateRemaining', ids);
            await dispatch('nextVideo');
        },
        async nextVideo({ commit, state, dispatch, rootGetters }) {
            if(state.remainingVideoIds.length > 0) {
                const next = state.remainingVideoIds[0];
                await dispatch('videos/reloadVideo', next, { root: true });
                commit('changeCurrent', rootGetters["videos/getVideo"](next));
                commit('removeVideoFromQueue', next);
            } else {
                commit('changeCurrent', null);
            }
        },
        async restartEditingIfPossible({ getters, dispatch, commit, rootGetters }) {
            if(getters.currentVideo) {
                const id = getters.currentVideo.id;
                await dispatch('videos/reloadVideo', id, { root: true });
                commit('changeCurrent', rootGetters["videos/getVideo"](id));
            }
        },
        async saveAndContinue({ getters, dispatch, commit }) {
            if(getters.currentVideo) {
                const editedVideo = await videoApi.saveVideo(getters.currentVideo);
                commit('videos/addOrUpdateVideo', editedVideo, { root: true });
                dispatch('nextVideo');
            }
        },
        videoDeleteUpdate({ dispatch, commit, getters }, videoId) {
            commit('removeVideoFromQueue', videoId);
            if(getters.currentVideo != null && getters.currentVideo.id === videoId) {
                dispatch('nextVideo');
            }
        }
    },
    mutations: {
        changeFilter(state, value) {
            state.query = value;
        },
        updateRemaining(state, videos) {
            state.remainingVideoIds = videos;
        },
        changeCurrent(state, video) {
            state.current = video;
        },
        removeVideoFromQueue(state, videoId) {
            removeFromArray(state.remainingVideoIds, videoId);
        },
        setEditingMetadataValue(state, { id, value }) {
            state.current.metadata[id].value = value;
        },
        changeThumbnail(state, thumbnailIndex) {
            state.current.selectedThumbnail = thumbnailIndex;
        },
    }
};
