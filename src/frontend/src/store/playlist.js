import { removeFromArrayWhere } from "@/helpers/array-helper";
import playlists from "../api/playlists";
import smartPlaylists from "../api/smart-playlists";

export default {
    namespaced: true,
    state: {
        playlists: [],
        smartPlaylists: []
    },
    getters: {
        getPlaylist(state) {
            return (id) => state.playlists.find(playlist => playlist.id === id);
        },
        getSmartPlaylist(state) {
            return (id) => state.smartPlaylists.find(playlist => playlist.id === id);
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
        addOrUpdateSmartPlaylist(state, playlist) {
            const index = state.smartPlaylists.findIndex(existing => existing.id === playlist.id);
            if(index >= 0) {
                Object.assign(state.smartPlaylists[index], playlist);
            } else {
                state.smartPlaylists.push(playlist);
            }
        },
        removePlaylist(state, playlistId) {
            removeFromArrayWhere(state.playlists, playlist => playlist.id === playlistId);
        },
        removeSmartPlaylist(state, playlistId) {
            removeFromArrayWhere(state.smartPlaylists, playlist => playlist.id === playlistId);
        },
        clearPlaylists(state) {
            state.playlists = [];
        },
        clearSmartPlaylists(state) {
            state.smartPlaylists = [];
        }
    },
    actions: {
        async createPlaylist({ commit }, { name, videoIds }) {
            const createdPlaylist = await playlists.savePlaylist(name, videoIds);
            commit('addOrUpdatePlaylist', createdPlaylist);
        },
        async createSmartPlaylist({ commit }, playlist) {
            const createdPlaylist = await smartPlaylists.savePlaylist(playlist);
            commit('addOrUpdateSmartPlaylist', createdPlaylist);
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
        async loadSmartPlaylists({ commit }) {
            commit('clearSmartPlaylists');
            const playlists = await smartPlaylists.getPlaylists();
            playlists.forEach(playlist => commit('addOrUpdateSmartPlaylist', playlist));
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
        },
        async updateSmartPlaylist({commit}, playlist) {
            const updated = await smartPlaylists.updatePlaylist(playlist);
            commit('addOrUpdateSmartPlaylist', updated);
        },
        async removeSmartPlaylist({commit}, id) {
            await smartPlaylists.deletePlaylist(id);
            commit('removeSmartPlaylist', id);
        },
        async updatePlaylistName({commit}, {id, name}) {
            const updated = await playlists.renamePlaylist(id, name);
            commit('addOrUpdatePlaylist', updated);
        }
    }
}
