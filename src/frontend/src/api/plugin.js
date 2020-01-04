import axios from 'axios';

export default {
    getPlugins() {
        return axios.get("/api/plugin").then(response => response.data);
    },

    disable(name) {
        return axios.post(`/api/plugin/${name}/disable`).then(response => response.data);
    },

    enable(name) {
        return axios.post(`/api/plugin/${name}/enable`).then(response => response.data);
    },

    refresh(name) {
        return axios.post(`/api/plugin/${name}/refresh`).then(response => response.data);
    }
};
