const BEFORE_FIRST_ELEMENT = -1;

export default {
    namespaced: true,
    state: {
        currentlyPlayingVideoId: BEFORE_FIRST_ELEMENT,
        playQueue: [],
        volume: 1.0,
        muted: false
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
        previousVideoId(state) {
            const currentIndex = state.playQueue.indexOf(state.currentlyPlayingVideoId);
            const nextIndex = currentIndex - 1;
            if(currentIndex > BEFORE_FIRST_ELEMENT && nextIndex > BEFORE_FIRST_ELEMENT) {
                return state.playQueue[nextIndex];
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
        hasPrevious(state) {
            if(state.currentlyPlayingVideoId == null) {
                return false;
            }

            return state.playQueue.length > 1 && state.playQueue.findIndex(video => video === state.currentlyPlayingVideoId) > 0;
        },
        hasNext(state) {
            if(state.currentlyPlayingVideoId == null) {
                return false;
            }

            return state.playQueue.length > 1 && state.playQueue.findIndex(video => video === state.currentlyPlayingVideoId) < state.playQueue.length - 1;
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
        },
        currentVolume(state) {
            return state.volume;
        },
        isMuted(state) {
            return state.muted;
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
        insertAfterCurrent(state, videoId) {
            const currentIndex = state.playQueue.findIndex(video => video === state.currentlyPlayingVideoId);
            const insertIndex = currentIndex + 1;
            if(insertIndex >= state.playQueue.length) {
                state.playQueue.push(videoId);
            } else {
                state.playQueue.splice(insertIndex, 0, videoId);
            }
        },
        updateOrder(state, videoIds) {
            state.playQueue.splice(0, state.playQueue.length, ...videoIds);
        },
        toggleMuted(state) {
            state.muted = !state.muted;
        },
        updateVolume(state, volume) {
            state.volume = Math.min(1.0, Math.max(0.0, volume));
        }
    },
    actions: {
        async changeToVideo({ commit, state }, next) {
            if(next == null && state.currentlyPlayingVideoId === BEFORE_FIRST_ELEMENT) {
                next = state.playQueue[0];
            }
            if(next != null) {
                commit('setCurrentVideo', next);
            }
        },
        async playVideo({ commit }, videoId) {
            commit('addToQueue', [videoId]);
            commit('setCurrentVideo', videoId);
        },
        async previousVideo({ getters, dispatch }) {
            return dispatch('changeToVideo', getters.previousVideoId);
        },
        async nextVideo({ getters, dispatch }) {
            return dispatch('changeToVideo', getters.nextVideoId);
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
        },
        clear({ commit }) {
            commit('clearPlaylist');
        },
        removeVideo({ state, commit }, videoId) {
            if(state.currentlyPlayingVideoId === videoId) {
                const currentIndex = state.playQueue.findIndex(video => video === videoId);
                if(currentIndex + 1 < state.playQueue.length) {
                    commit('setCurrentVideo', state.playQueue[currentIndex + 1]);
                } else if(currentIndex - 1 > 0) {
                    commit('setCurrentVideo', state.playQueue[currentIndex - 1]);
                } else {
                    commit('setCurrentVideo', BEFORE_FIRST_ELEMENT);
                }
            }
            commit('removeVideoFromQueue', videoId);
        },
        playVideoNext({ state, commit, dispatch }, videoId) {
            if(state.currentlyPlayingVideoId === BEFORE_FIRST_ELEMENT) {
                dispatch('playVideo', videoId);
            } else {
                commit('insertAfterCurrent', videoId);
            }
        },
        queueVideoIn({ state, commit, dispatch }, videoId) {
            if(state.currentlyPlayingVideoId === BEFORE_FIRST_ELEMENT) {
                dispatch('playVideo', videoId);
            } else {
                commit('addToQueue', [videoId]);
            }
        }
    }
}
