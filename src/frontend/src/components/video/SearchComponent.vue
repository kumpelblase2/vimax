<template>
    <form action="#" @submit="doSearch">
        <v-text-field
            :value="searchQuery"
            @change="updateQuery"
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
        computed: {
            ...mapGetters('search', ['searchQuery'])
        },
        methods: {
            ...mapActions('videos', [
                'search'
            ]),
            ...mapMutations('search', ['updateQuery']),
            doSearch() {
                this.search(this.searchQuery);
            }
        }
    }
</script>

<style scoped>
    form {
        width: 100%;
    }
</style>
