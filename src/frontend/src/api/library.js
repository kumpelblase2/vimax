import axios from 'axios';

export default {
    getLibraries() {
        return axios.get("/api/library").then(response => response.data);
    },

    saveLibrary(library) {
        return axios.post("/api/library", library).then(response => response.data);
    },

    deleteLibrary(id, deleteThumbnails = false) {
        return axios.delete(`/api/library/${id}?delete_thumbnails=${deleteThumbnails}`).then(response => response.data);
    }
};
