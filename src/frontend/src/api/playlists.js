import axios from 'axios';

export default {
    getPlaylists() {
        return axios.get("/api/playlists").then(response => response.data);
    },

    savePlaylist(name, videoIds = []) {
        return axios.post("/api/playlists", { name, videoIds }).then(response => response.data);
    },

    deletePlaylist(id) {
        return axios.delete("/api/playlists/" + id).then(response => response.data);
    },

    addToPlaylist(id, videoIds) {
        return axios.put(`/api/playlists/${id}/add`, videoIds).then(response => response.data);
    }
};
