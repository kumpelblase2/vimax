function createHandlers(vuex) {
    return {
        'video-delete': (id) => {
            vuex.dispatch('videos/videoDeleteUpdate', id);
            vuex.dispatch('sorting/videoDeleteUpdate', id);
            vuex.dispatch('checkin/videoDeleteUpdate', id);
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
