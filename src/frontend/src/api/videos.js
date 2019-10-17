import axios from 'axios';

export default {
    getAllVideos() {
        return axios.get('/api/videos').then(response => response.data);
    },

    getVideosByPage(page, query = "", sortProp = 'Name', sortDir = 'ASC') {
        return axios.get(`/api/videos?query=${encodeURIComponent(query)}&page=${page}&sortby=${sortProp}&sortdir=${sortDir}`).then(response => response.data);
    },

    getRecentVideos() {
        return axios.get('/api/home').then(response => response.data);
    },

    getVideo(id) {
        return axios.get(`/api/video/${id}`).then(response => response.data);
    },

    saveVideo(video) {
        return axios.put(`/api/video/${video.id}`, video).then(response => response.data);
    },

    refreshThumbnails(video) {
        return axios.post(`/api/video/${video.id}/refresh`).then(response => response.data);
    },

    getSortableVideosFor(metadata) {
        return axios.get(`/api/sorting/${metadata.id}`).then(response => response.data);
    },

    getVideosById(ids) {
        return axios.get(`/api/videos/byid?ids=${ids.join(",")}`).then(response => response.data);
    }
};
