export default {
    namespaced: true,
    state: {
        currentlyPlayingVideoId: -1,
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
            if(currentIndex > -1 && nextIndex < state.playQueue.length) {
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
        addToQueue(state, videoId) {
            state.playQueue.push(videoId);
        },
        clearPlaylist(state) {
            state.playQueue = [];
        }
    },
    actions: {
        nextVideo({ commit, getters }) {
            const next = getters.nextVideoId;
            if(next != null) {
                commit('setCurrentVideo', next);
            }
        },
        playPlaylist({ commit, getters, dispatch }, playlist) {
            commit('clearPlaylist');
            playlist.videoIds.forEach(video => {
                commit('addToQueue', video);
            });
            dispatch('nextVideo');
        },
        skipToVideo({ commit, getters }, videoId) {
            if(getters.containsVideo(videoId)) {
                commit('setCurrentVideo', videoId);
            }
        }
    }
}
