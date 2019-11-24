import axios from 'axios';

export default {
    getPlaylists() {
        return axios.get("/api/playlists").then(response => response.data);
    },

    savePlaylist(name, videoIds) {
        return axios.post("/api/playlist", { name, videoIds }).then(response => response.data);
    },

    deletePlaylist(id) {
        return axios.delete("/api/playlist/" + id).then(response => response.data);
    },

    addToPlaylist(id, videoIds) {
        return axios.put(`/api/playlist/${id}/add`, videoIds).then(response => response.data);
    }
};
