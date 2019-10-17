import metadataApi from "../api/metadata";
import { isSortable } from "../helpers/metadata-display-helper";

export default {
    namespaced: true,
    state: {
        metadata: [],
        visibleMetadatas: [],
        possibleMetadataTypes: [
            { text: 'Text', value: 'TEXT' },
            { text: 'Number', value: 'NUMBER' },
            { text: 'Range', value: 'RANGE' },
            { text: 'Taglist', value: 'TAGLIST' },
            { text: 'Selection', value: 'SELECTION' },
            { text: 'Duration', value: 'DURATION' },
            { text: 'Switch', value: 'BOOLEAN' }
        ],
        headers: [
            { text: 'Order', value: 'displayOrder' },
            { text: 'Name', value: 'name' },
            { text: 'Type', value: 'type' },
            { text: 'Default Value', values: 'defaultValue' },
            { text: 'Actions', value: 'actions', sortable: false, width: 150 }
        ],
        editingItem: {
            id: null,
            name: '',
            type: 'TEXT',
            ordering: 'ASC',
            options: {
                values: []
            }
        },
        defaultItem: {
            id: null,
            name: '',
            type: 'TEXT',
            ordering: 'ASC',
            options: {
                values: []
            }
        }
    },
    getters: {
        orderedMetadata(state) {
            return [...state.metadata].sort((first, second) => first.displayOrder - second.displayOrder);
        },
        visibleMetadata(state) {
            return state.visibleMetadatas;
        },
        hasVisibleMetadata(state) {
            return state.visibleMetadatas.length > 0;
        },
        sortableMetadata(state) {
            return state.visibleMetadatas.filter(metadata => isSortable(metadata.type));
        }
    },
    actions: {
        async loadMetadata({ commit }) {
            const metadatas = await metadataApi.getMetadata();
            metadatas.forEach(metadata => {
                commit('addOrUpdateMetadata', metadata);
                commit('showMetadata', metadata);
            });
        },
        async saveMetadata({ commit }, metadata) {
            metadata.options.type = metadata.type;
            const saved = await metadataApi.saveMetadata(metadata);
            commit('addOrUpdateMetadata', saved);
        },
        deleteMetadata({ commit }, metadata) {
            return metadataApi.deleteMetadata(metadata.id).then(() => {
                commit('removeMetadata', metadata);
            }).catch((ex) => console.error(ex));
        },
        resetEditItem({ commit, state }) {
            commit('setEditItem', state.defaultItem);
        },
        startEditItem({ commit }, item) {
            commit('setEditItem', item);
        },
        async moveMetadataUp({ commit, state }, metadata) {
            const currentOrder = metadata.displayOrder;
            const nextOrder = metadata.displayOrder - 1;
            const metadataToMove = state.metadata.find(existing => existing.displayOrder === nextOrder);
            commit('setOrder', { metadataId: metadata.id, order: nextOrder });
            commit('setOrder', { metadataId: metadataToMove.id, order: currentOrder });

            await metadataApi.saveMetadata(metadata);
            await metadataApi.saveMetadata(metadataToMove);
        },
        async moveMetadataDown({ commit, state }, metadata) {
            const currentOrder = metadata.displayOrder;
            const nextOrder = metadata.displayOrder + 1;
            const metadataToMove = state.metadata.find(existing => existing.displayOrder === nextOrder);
            commit('setOrder', { metadataId: metadata.id, order: nextOrder });
            commit('setOrder', { metadataId: metadataToMove.id, order: currentOrder });

            await metadataApi.saveMetadata(metadata);
            await metadataApi.saveMetadata(metadataToMove);
        },
        onlyShowMetadata({ commit, state }, newVisibleMetadata) {
            state.metadata.forEach(metadata => {
                if(newVisibleMetadata.findIndex(visibleMetadata => visibleMetadata.id === metadata.id) >= 0) {
                    commit('showMetadata', metadata);
                } else {
                    commit('hideMetadata', metadata);
                }
            })
        },
        hideAllMetadata({ commit, state }) {
            state.metadata.forEach(metadata => {
                commit('hideMetadata', metadata);
            });
        },
        showAllMetadata({ commit, state }) {
            state.metadata.forEach(metadata => {
                commit('showMetadata', metadata);
            });
        }
    },
    mutations: {
        showMetadata(state, metadata) {
            const existingIndex = state.visibleMetadatas.findIndex(existing => metadata.id === existing.id);
            if(existingIndex < 0) {
                state.visibleMetadatas.push(metadata);
                state.visibleMetadatas.sort((first, second) => first.displayOrder - second.displayOrder)
            }
        },
        hideMetadata(state, metadata) {
            const existingIndex = state.visibleMetadatas.findIndex(existing => metadata.id === existing.id);
            if(existingIndex >= 0) {
                state.visibleMetadatas.splice(existingIndex, 1);
            }
        },
        addOrUpdateMetadata(state, metadata) {
            const existingIndex = state.metadata.findIndex(existing => existing.id === metadata.id);
            if(existingIndex >= 0) {
                Object.assign(state.metadata[existingIndex], metadata);
            } else {
                state.metadata.push(metadata);
            }
        },
        removeMetadata(state, metadata) {
            const existingIndex = state.metadata.findIndex(existing => existing.id === metadata.id);
            if(existingIndex >= 0) {
                state.metadata.splice(existingIndex, 1);
            }
        },
        setEditItem(state, item) {
            state.editingItem = Object.assign({}, item);
        },
        setOrder(state, { metadataId, order }) {
            const found = state.metadata.find(existing => existing.id === metadataId);
            found.displayOrder = order;
        }
    }
};
