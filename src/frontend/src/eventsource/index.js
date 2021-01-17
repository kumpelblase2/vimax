function createHandlers(vuex) {
    return {
        'video-delete': (id) => {
            vuex.dispatch('videos/videoDeleteUpdate', id);
            vuex.dispatch('sorting/videoDeleteUpdate', id);
            vuex.dispatch('checkin/videoDeleteUpdate', id);
        },
        'video-update': (id) => {
            vuex.dispatch('videos/videoUpdateUpdate', id);
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
            console.log('Handling SSE. Data: ' + JSON.stringify(data));
            handler(data.content);
        }
    };
}
