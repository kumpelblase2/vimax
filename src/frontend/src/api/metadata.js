import axios from 'axios';

export default {
    getMetadata() {
        return axios.get("/api/metadata").then(response => response.data);
    },

    saveMetadata(metadata) {
        return axios.post("/api/metadata", metadata).then(response => response.data);
    },

    deleteMetadata(id) {
        return axios.delete("/api/metadata/" + id).then(response => response.data);
    },

    getMetadataValues(id) {
        return axios.get(`/api/metadata/${id}/values`).then(response => response.data);
    }
};
