<template>
    <form action="#" @submit="doSearch">
        <v-text-field
            v-model="query"
            @click:append.prevent="doSearch"
            placeholder="Search..."
            append-icon="search"
            color="white"
            :hide-details="!hasError"
            clearable
            :error-messages="queryErrors"
            @click:clear="reset"
        />
    </form>
</template>

<script>
    import { mapActions, mapGetters, mapMutations } from 'vuex';

    export default {
        name: "SearchComponent",
        data() {
            return {
                query: ""
            }
        },
        computed: {
            ...mapGetters('search', ['searchQuery', 'error']),
            hasError() {
                return this.error != null && this.error.length > 0;
            },
            queryErrors() {
                if(this.hasError) {
                    return [this.error];
                } else {
                    return [];
                }
            }
        },
        methods: {
            ...mapActions('videos', ['search']),
            ...mapMutations('search', ['updateQuery']),
            doSearch() {
                this.updateQuery(this.query);
                this.search();
            },
            reset() {
                this.query = "";
                this.doSearch();
            }
        },
        mounted() {
            this.query = this.searchQuery;
        }
    }
</script>

<style scoped>
    form {
        width: 100%;
    }
</style>
