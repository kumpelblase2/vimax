import axios from 'axios';

export default {
    watchStartEvent(id) {
        return this.publishEvent(id, 'START_WATCHING');
    },
    watchFinishEvent(id) {
        return this.publishEvent(id, 'FINISH_WATCHING');
    },
    publishEvent(videoId, event) {
        return axios.post(`/api/event?type=${event}&video=${videoId}`, {}, {
            validateStatus(value) {
                return value === 200 || value === 304;
            }
        }).then(res => {
            if(res.status === 200) {
                return res.data;
            } else {
                return null;
            }
        });
    }
};
