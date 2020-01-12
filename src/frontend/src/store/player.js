const BEFORE_FIRST_ELEMENT = -1;

export default {
    namespaced: true,
    state: {
        currentlyPlayingVideoId: BEFORE_FIRST_ELEMENT,
        playQueue: []
    },
    getters: {
        currentVideo(state, _getters, _rootState, rootGetters) {
            return rootGetters['videos/getVideo'](state.currentlyPlayingVideoId);
        },
        nextUp(state, getters, _rootState, rootGetters) {
            const nextId = getters.nextVideoId;
            if(nextId != null) {
                return rootGetters['videos/getVideo'](nextId);
            } else {
                return null;
            }
        },
        nextVideoId(state) {
            const currentIndex = state.playQueue.indexOf(state.currentlyPlayingVideoId);
            const nextIndex = currentIndex + 1;
            if(currentIndex > BEFORE_FIRST_ELEMENT && nextIndex < state.playQueue.length) {
                return state.playQueue[nextIndex];
            } else {
                return null;
            }
        },
        hasQueue(state) {
            return state.playQueue.length > 0;
        },
        hasVideoPlaying(state) {
            return state.currentlyPlayingVideoId >= 0;
        },
        containsVideo(state) {
            return (id) => state.playQueue.includes(id);
        },
        videosInQueue(state, _getters, _rootState, rootGetters) {
            return state.playQueue.map(videoId => rootGetters['videos/getVideo'](videoId));
        }
    },
    mutations: {
        setCurrentVideo(state, videoId) {
            state.currentlyPlayingVideoId = videoId;
        },
        removeVideoFromQueue(state, videoIdToRemove) {
            const index = state.playQueue.findIndex(video => video === videoIdToRemove);
            if(index >= 0) {
                state.playQueue.splice(index, 1);
            }
        },
        addToQueue(state, videoIds) {
            state.playQueue = [...state.playQueue, ...videoIds];
        },
        clearPlaylist(state) {
            state.playQueue = [];
            state.currentlyPlayingVideoId = BEFORE_FIRST_ELEMENT;
        },
    },
    actions: {
        async nextVideo({ commit, state, getters, dispatch, rootGetters }) {
            let next = getters.nextVideoId;
            if(next == null && state.currentlyPlayingVideoId === BEFORE_FIRST_ELEMENT) {
                next = state.playQueue[0];
            }

            if(next != null) {
                if(rootGetters['videos/getVideo'](next) == null) {
                    await dispatch('videos/loadVideos', [next], { root: true });
                }
                commit('setCurrentVideo', next);
            }
        },
        async playVideos({ commit, dispatch }, videoIds) {
            commit('clearPlaylist');
            commit('addToQueue', videoIds);
            await dispatch('nextVideo');
        },
        async playPlaylist({ dispatch }, playlist) {
            await dispatch('playVideos', playlist.videoIds);
        },
        skipToVideo({ commit, getters }, videoId) {
            if(getters.containsVideo(videoId)) {
                commit('setCurrentVideo', videoId);
            }
        }
    }
}
