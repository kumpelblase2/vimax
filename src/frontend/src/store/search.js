function createFilterTermFor(metadata, value) {
    switch(metadata.type) {
        case 'BOOLEAN':
            return (value ? '+' : '-') + metadata.name;
        case 'SELECTION':
            value = metadata.options.values.find(v => v.id === value).name;
            // Intentional fall through
        default:
            return metadata.name + ":" + (value.includes(" ") ? `"${value}"` :value);
    }
}

export default {
    namespaced: true,
    state: {
        query: "",
        sort: {
            property: 'name',
            direction: 'ASC'
        },
        queryError: ""
    },
    getters: {
        sortingProperty(state) {
            return state.sort.property;
        },
        sortingDirection(state) {
            return state.sort.direction;
        },
        searchQuery(state) {
            return state.query;
        },
        error(state) {
            return state.queryError;
        }
    },
    mutations: {
        updateQuery(state, query) {
            state.query = query;
        },
        updateSortingProperty(state, property) {
            state.sort.property = property;
        },
        updateSortingDirection(state, direction) {
            state.sort.direction = direction;
        },
        updateQueryError(state, error) {
            state.queryError = error;
        },
        addFilterTerm(state, value) {
            if(state.query === "") {
                state.query = value;
            } else {
                state.query += " " + value;
            }
        }
    },
    actions: {
        addFilterFor({commit}, {metadata, value}) {
            const term = createFilterTermFor(metadata, value);
            commit('addFilterTerm', term);
        }
    }
}
