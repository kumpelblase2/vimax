import playlists from "../api/playlists";

export default {
    namespaced: true,
    state: {
        playlists: []
    },
    getters: {},
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
        async createPlaylist({ commit }, { name, videos }) {
            const createdPlaylist = await playlists.savePlaylist(name, videos);
            commit('addOrUpdatePlaylist', createdPlaylist);
        },
        async removePlaylist({ commit }, playlistId) {
            await playlists.deletePlaylist(playlistId);
            commit('removePlaylist', playlistId);
        },
        async loadPlaylists({ commit }) {
            commit('createPlaylists');
            const playlists = await playlists.getPlaylists();
            playlists.forEach(playlist => commit('addOrUpdatePlaylist', playlist));
        },
        async addSelectedToPlaylist({ commit, state }, playlistId) {
            await playlists.addToPlaylist(playlistId, state.selectedVideoIds);
        },
        async addToPlaylist({ commit }, { playlistId, videoIds }) {
            await playlists.addToPlaylist(playlistId, videoIds);
        }
    }
}
