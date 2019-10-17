export default {
    namespaced: true,
    state: {
        query: "",
        sort: {
            property: 'Name',
            direction: 'ASC'
        }
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
        }
    }
}
