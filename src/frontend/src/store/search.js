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
        }
    }
}
