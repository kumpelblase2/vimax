export default {
    namespaced: true,
    state: {
        currentlyPlayingVideo: -1,
        playQueue: []
    },
    getters: {
        currentVideo(state, _getters, _rootState, rootGetters) {
            return rootGetters['videos/getVideo'](state.currentlyPlayingVideo);
        },
        nextUp(state, _getters, _rootState, rootGetters) {
            return rootGetters['videos/getVideo'](state.playQueue[0]);
        },
        hasQueue(state) {
            return state.playQueue.length > 0;
        },
        hasVideoPlaying(state) {
            return state.currentlyPlayingVideo >= 0;
        },
        containsVideo(state) {
            return (id) => state.currentlyPlayingVideo === id || state.playQueue.includes(id);
        },
        videosInQueue(state, _getters, _rootState, rootGetters) {
            return state.playQueue.map(videoId => rootGetters['videos/getVideo'](videoId));
        }
    },
    mutations: {
        setCurrentVideo(state, video) {
            state.currentlyPlayingVideo = video;
        },
        dropHeadVideo(state) {
            state.playQueue.splice(0, 1);
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
            while(state.playQueue.length > 0) {
                state.playQueue.pop();
            }
        }
    },
    actions: {
        nextVideo({ commit, getters }) {
            const next = getters.nextUp;
            if(next != null) {
                commit('dropHeadVideo');
                commit('setCurrentVideo', next.id);
            }
        },
        playPlaylist({ commit, getters, dispatch }, playlist) {
            commit('clearPlaylist');
            playlist.videoIds.forEach(video => {
                commit('addToQueue', video);
            });
            dispatch('nextVideo');
        },
        skipToVideo({dispatch,state,getters}, videoId) {
            if(getters.containsVideo(videoId)){
                while(state.currentlyPlayingVideo !== videoId) {
                    dispatch('nextVideo');
                }
            }
        }
    }
}
