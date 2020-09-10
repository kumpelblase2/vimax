import axios from 'axios';

export default {
    getPlaylists() {
        return axios.get("/api/smart-playlists").then(response => response.data);
    },

    getVideosOf(id) {
        return axios.get(`/api/smart-playlists/${id}`).then(response => response.data);
    },

    savePlaylist(playlist) {
        return axios.post("/api/smart-playlists", playlist).then(response => response.data);
    },

    updatePlaylist(playlist) {
        return axios.put("/api/smart-playlists/" + playlist.id, playlist).then(response => response.data);
    },

    deletePlaylist(id) {
        return axios.delete("/api/smart-playlists/" + id).then(response => response.data);
    }
};
