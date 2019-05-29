import metadataApi from "../api/metadata";

export default {
    namespaced: true,
    state: {
        metadata: [],
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
            return [...state.metadata].sort((first,second) => first.displayOrder - second.displayOrder);
        }
    },
    actions: {
        async loadMetadata({ commit }) {
            const metadatas = await metadataApi.getMetadata();
            metadatas.forEach(metadata => {
                commit('addOrUpdateLibrary', metadata);
            });
        },
        async saveMetadata({ commit }, metadata) {
            metadata.options.type = metadata.type;
            const saved = await metadataApi.saveMetadata(metadata);
            commit('addOrUpdateLibrary', saved);
        },
        deleteMetadata({ commit }, metadata) {
            return metadataApi.deleteMetadata(metadata.id).then(() => {
                commit('removeLibrary', metadata);
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
        }
    },
    mutations: {
        addOrUpdateLibrary(state, metadata) {
            const existingIndex = state.metadata.findIndex(existing => existing.id === metadata.id);
            if(existingIndex >= 0) {
                Object.assign(state.metadata[existingIndex], metadata);
            } else {
                state.metadata.push(metadata);
            }
        },
        removeLibrary(state, metadata) {
            const existingIndex = state.metadata.findIndex(existing => existing.id === metadata.id);
            if(existingIndex >= 0) {
                state.metadata.splice(existingIndex, 1);
            }
        },
        setEditItem(state, item) {
            state.editingItem = Object.assign({}, item);
        },
        setOrder(state, {metadataId,order}) {
            const found = state.metadata.find(existing => existing.id === metadataId);
            found.displayOrder = order;
        }
    }
};
