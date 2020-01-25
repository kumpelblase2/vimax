import pluginApi from "../api/plugin";

export default {
    namespaced: true,
    state: {
        headers: [
            { text: "Name", value: "name" },
            { text: "Description", value: "description", width: 500 },
            { text: "Author", value: "author", width: 200 },
            { text: "Version", value: "version", width: 150 },
            { text: "Enabled", value: "enabled", width: 150 },
            { text: "Added On", value: "createdAt", width: 150 },
            { text: "Enabled On", value: "enabledAt", width: 150 },
            { text: "Disabled On", value: "disabledAt", width: 150 }
        ],
        plugins: []
    },
    actions: {
        async loadPlugins({ commit }) {
            const plugins = await pluginApi.getPlugins();
            plugins.forEach(plugin => commit('addOrUpdatePlugin', plugin));
        },
        async togglePlugin({commit,state}, name) {
            const currentState = state.plugins.find(plugin => plugin.name === name).enabled;
            let newPlugin;
            if(currentState) {
                newPlugin = await pluginApi.disable(name);
            } else {
                newPlugin = await pluginApi.enable(name);
            }

            commit('addOrUpdatePlugin', newPlugin);
        }
    },
    mutations: {
        addOrUpdatePlugin(state, plugin) {
            const existingIndex = state.plugins.findIndex(existing => existing.name === plugin.name);
            if(existingIndex >= 0) {
                Object.assign(state.plugins[existingIndex], plugin);
            } else {
                state.plugins.push(plugin);
            }
        }
    }
};
