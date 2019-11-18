import axios from 'axios';

export default {
    watchStartEvent(id) {
        return this.publishEvent(id, 'START_WATCHING');
    },
    watchFinishEvent(id) {
        return this.publishEvent(id, 'FINISH_WATCHING');
    },
    publishEvent(videoId, event) {
        return axios.post(`/api/event?type=${event}&video=${videoId}`).then(response => response.data);
    }
};
