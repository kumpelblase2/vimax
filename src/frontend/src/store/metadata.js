import { removeFromArrayWhere } from "@/helpers/array-helper";
import metadataApi from "../api/metadata";
import { isSortable } from "@/helpers/metadata-display-helper";

export const applicationSortableMetadata = [
    { name: "Name", value: "name", ordering: "ASC" },
    { name: "Updated", value: "updateTime", ordering: "DESC" },
    { name: "Created", value: "creationTime", ordering: "DESC" }
];

export default {
    namespaced: true,
    state: {
        metadata: [],
        visibleMetadatas: [],
        possibleMetadataTypes: [
            { text: 'Text', value: 'TEXT' },
            { text: 'Number', value: 'NUMBER' },
            { text: 'Float', value: 'FLOAT' },
            { text: 'Range', value: 'RANGE' },
            { text: 'Taglist', value: 'TAGLIST' },
            { text: 'Selection', value: 'SELECTION' },
            { text: 'Duration', value: 'DURATION' },
            { text: 'Switch', value: 'BOOLEAN' }
        ],
        headers: [
            { text: 'Order', value: 'displayOrder', sortable: false },
            { text: 'Name', value: 'name', sortable: false },
            { text: 'Type', value: 'type', sortable: false },
            { text: 'Default Value', values: 'defaultValue', sortable: false },
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
        editableMetadata(state) {
            return state.metadata.filter(metadata => metadata.owner == null).sort((first, second) => first.displayOrder - second.displayOrder)
        },
        visibleMetadata(state) {
            return state.visibleMetadatas;
        },
        hasVisibleMetadata(state) {
            return state.visibleMetadatas.length > 0;
        },
        sortableMetadata(state) {
            return state.metadata.filter(metadata => isSortable(metadata.type));
        }
    },
    actions: {
        async loadMetadata({ commit }) {
            const metadatas = await metadataApi.getMetadata();
            commit('addAll', metadatas);
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
        async moveMetadataUp({ commit, getters }, metadata) {
            const currentPos = getters.orderedMetadata.indexOf(metadata);
            const changedMetadatas = await metadataApi.insertAt(metadata.id, currentPos - 1);

            changedMetadatas.forEach(changed => commit('addOrUpdateMetadata', changed));
        },
        async moveMetadataDown({ commit, getters }, metadata) {
            const currentPos = getters.orderedMetadata.indexOf(metadata);
            const changedMetadatas = await metadataApi.insertAt(metadata.id, currentPos + 1);

            changedMetadatas.forEach(changed => commit('addOrUpdateMetadata', changed));
        },
        onlyShowMetadata({ commit, state }, newVisibleMetadata) {
            if(newVisibleMetadata.length === 0) {
                commit('hideAllMetadata');
                return;
            } else if(newVisibleMetadata.length === state.metadata.length) {
                commit('showAllMetadata');
                return;
            }

            state.metadata.forEach(metadata => {
                if(newVisibleMetadata.findIndex(visibleMetadata => visibleMetadata.id === metadata.id) >= 0) {
                    commit('showMetadata', metadata);
                } else {
                    commit('hideMetadata', metadata);
                }
            })
        }
    },
    mutations: {
        showMetadata(state, metadata) {
            const existingIndex = state.visibleMetadatas.findIndex(existing => metadata.id === existing.id);
            if(existingIndex < 0) {
                state.visibleMetadatas.push(metadata);
                state.visibleMetadatas.sort((first, second) => first.displayOrder - second.displayOrder);
            }
        },
        hideMetadata(state, metadata) {
            removeFromArrayWhere(state.visibleMetadatas, existing => existing.id === metadata.id);
        },
        addOrUpdateMetadata(state, metadata) {
            const existingIndex = state.metadata.findIndex(existing => existing.id === metadata.id);
            if(existingIndex >= 0) {
                Object.assign(state.metadata[existingIndex], metadata);
            } else {
                state.metadata.push(metadata);
            }
        },
        addAll(state, metadatas) {
            metadatas.forEach(metadata => {
                const existingIndex = state.metadata.findIndex(existing => existing.id === metadata.id);
                if(existingIndex >= 0) {
                    Object.assign(state.metadata[existingIndex], metadata);
                } else {
                    state.metadata.push(metadata);
                }
            });
        },
        removeMetadata(state, metadata) {
            removeFromArrayWhere(state.metadata, existing => existing.id === metadata.id);
        },
        setEditItem(state, item) {
            state.editingItem = Object.assign({}, item);
        },
        hideAllMetadata(state) {
            state.visibleMetadatas = [];
        },
        showAllMetadata(state) {
            const metadataCopy = state.metadata.slice();
            metadataCopy.sort((first, second) => first.displayOrder - second.displayOrder);
            state.visibleMetadatas = metadataCopy;
        }
    }
};
