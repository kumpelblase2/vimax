<template>
    <form action="#" @submit="doSearch">
        <v-text-field
            v-model="query"
            @click:append="doSearch"
            placeholder="Search..."
            append-icon="search"
            color="white"
            hide-details
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
            ...mapGetters('search', ['searchQuery'])
        },
        methods: {
            ...mapActions('videos', ['search']),
            ...mapMutations('search', ['updateQuery']),
            doSearch() {
                this.updateQuery(this.query);
                this.search();
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
