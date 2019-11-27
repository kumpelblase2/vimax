import { supportsMetadataFilter, supportsOrder, supportsPlaylist, supportsSearch } from "../router/supports";

export default {
    namespaced: true,
    state: {
        view: 'home'
    },
    getters: {
        currentView(state) {
            return state.view;
        },
        shouldShowSearch(state) {
            return supportsSearch(state.view);
        },
        shouldShowMetadata(state) {
            return supportsMetadataFilter(state.view);
        },
        shouldShowSort(state) {
            return supportsOrder(state.view);
        },
        shouldShowPlaylistAdd(state) {
            return supportsPlaylist(state.view);
        }
    },
    mutations: {
        switchView(state, to) {
            state.view = to;
        }
    }
}
