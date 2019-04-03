<template>
    <form action="#" @submit="doSearch">
        <v-text-field
            v-model="searchQuery"
            :append-icon-cb="doSearch"
            placeholder="Search..."
            single-line
            append-icon="search"
            color="white"
            hide-details
        ></v-text-field>
    </form>
</template>

<script>
    import {mapActions} from 'vuex';

    export default {
        name: "SearchComponent",
        data() {
            return {
                searchQuery: ""
            };
        },
        methods: {
            ...mapActions('videos', [
                'search'
            ]),
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
