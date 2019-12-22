import playlists from "../api/playlists";

export default {
    namespaced: true,
    state: {
        playlists: []
    },
    getters: {
        getPlaylist(state) {
            return (id) => state.playlists.find(playlist => playlist.id === id);
        }
    },
    mutations: {
        addOrUpdatePlaylist(state, playlist) {
            const index = state.playlists.findIndex(existing => existing.id === playlist.id);
            if(index >= 0) {
                Object.assign(state.playlists[index], playlist);
            } else {
                state.playlists.push(playlist);
            }
        },
        removePlaylist(state, playlistId) {
            const index = state.playlists.findIndex(existing => existing.id === playlistId);
            if(index >= 0) {
                state.playlists.splice(index, 1);
            }
        },
        clearPlaylists(state) {
            while(state.playlists.length > 0) {
                state.playlists.pop();
            }
        }
    },
    actions: {
        async createPlaylist({ commit }, { name, videoIds }) {
            const createdPlaylist = await playlists.savePlaylist(name, videoIds);
            commit('addOrUpdatePlaylist', createdPlaylist);
        },
        async removePlaylist({ commit }, playlistId) {
            await playlists.deletePlaylist(playlistId);
            commit('removePlaylist', playlistId);
        },
        async loadPlaylists({ commit, dispatch }) {
            commit('clearPlaylists');
            const loaded = await playlists.getPlaylists();
            loaded.forEach(playlist => commit('addOrUpdatePlaylist', playlist));
            const videoIds = [...new Set(loaded.flatMap(playlist => playlist.videoIds))];
            await dispatch('videos/loadVideos', videoIds, { root: true });
        },
        async addToPlaylist({ commit }, { playlistId, videoIds }) {
            const newPlaylist = await playlists.addToPlaylist(playlistId, videoIds);
            commit('addOrUpdatePlaylist', newPlaylist);
        },
        async removeFromPlaylist({commit}, {playlistId,videoIds}) {
            const newPlaylist = await playlists.deleteFromPlaylist(playlistId, videoIds);
            commit('addOrUpdatePlaylist', newPlaylist);
        },
        async updateOrder({commit}, { playlistId, videoIds }) {
            const newPlaylist = await playlists.updateOrderOf(playlistId, videoIds);
            commit('addOrUpdatePlaylist', newPlaylist);
        }
    }
}
