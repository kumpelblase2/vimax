import { shouldShowBack, supportsMetadataFilter, supportsOrder, supportsPlaylist, supportsSearch } from "../router/supports";

export default {
    namespaced: true,
    state: {
        lastView: null,
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
        },
        shouldShowBack(state) {
            return shouldShowBack(state.view) && state.lastView != null;
        }
    },
    mutations: {
        switchView(state, to) {
            state.lastView = state.view;
            state.view = to;
        }
    }
}
