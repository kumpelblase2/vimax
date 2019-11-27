<template>
    <form action="#" @submit="doSearch">
        <v-text-field
            v-model="searchQuery"
            @change="updateQuery"
            :append-icon-cb="doSearch"
            placeholder="Search..."
            append-icon="search"
            color="white"
            hide-details
        ></v-text-field>
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
