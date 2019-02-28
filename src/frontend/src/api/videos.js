import axios from 'axios';

export default {
  getRecentVideos() {
    return axios.get('/api/home');
  }
};
