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

    deleteFromPlaylist(id, videoIds) {
        return axios.put(`/api/playlists/${id}/remove`, videoIds).then(response => response.data);
    },

    addToPlaylist(id, videoIds) {
        return axios.put(`/api/playlists/${id}/add`, videoIds).then(response => response.data);
    },

    updateOrderOf(id, videoIds) {
        return axios.put(`/api/playlists/${id}/order`, videoIds).then(response => response.data);
    },

    renamePlaylist(id, newName) {
        return axios.put(`/api/playlists/${id}`, newName, {
            headers: {
                'Content-Type': 'text/plain'
            }
        }).then(response => response.data);
    }
};
