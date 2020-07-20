function createHandlers(vuex) {
    return {
        'video-delete': ({videoId}) => {
            vuex.dispatch('videos/videoDeleteUpdate', videoId);
            vuex.dispatch('sorting/videoDeleteUpdate', videoId);
            vuex.dispatch('checkin/videoDeleteUpdate', videoId);
        }
    }
}

export function setup(vuex) {
    const handlers = createHandlers(vuex);
    const source = new EventSource("/notifications");
    source.onopen = () => console.log("Connected to SSE.");
    source.onmessage = (message) => {
        const data = JSON.parse(message.data);
        const type = data.type;
        const handler = handlers[type];
        if(handler) {
            handler(data.content);
        }
    };
}
