import { removeFromArrayWhere } from "@/helpers/array-helper";
import libraryApi from '../api/library';

export default {
    namespaced: true,
    state: {
        libraries: [],
        editingItem: {
            id: null,
            path: ''
        },
        defaultItem: {
            id: null,
            path: ''
        },
        headers: [
            { text: 'Location', value: 'path' },
            { text: 'Actions', value: 'actions', sortable: false, width: 50 }
        ]
    },
    actions: {
        async loadLibraries({ commit }) {
            const libraries = await libraryApi.getLibraries();
            libraries.forEach(library => {
                commit('addOrUpdateLibrary', library);
            });
        },
        async saveLibrary({ commit }, library) {
            const saved = await libraryApi.saveLibrary(library);
            commit('addOrUpdateLibrary', saved);
        },
        async deleteLibrary({ commit }, {library, deleteThumbnails}) {
            await libraryApi.deleteLibrary(library.id, deleteThumbnails);
            commit('removeLibrary', library);
        },
        resetEditItem({ commit }) {
            commit('resetEditItem');
        }
    },
    mutations: {
        addOrUpdateLibrary(state, library) {
            const existingIndex = state.libraries.findIndex(existing => existing.id === library.id);
            if(existingIndex >= 0) {
                Object.assign(state.libraries[existingIndex], library);
            } else {
                state.libraries.push(library);
            }
        },
        removeLibrary(state, library) {
            removeFromArrayWhere(state.libraries, existing => existing.id === library.id);
        },
        resetEditItem(state) {
            state.editingItem = Object.assign({}, state.defaultItem);
        }
    }
};
