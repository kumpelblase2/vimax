<template>
    <v-select class="nav-select" :items="sortableProperties" item-text="name" hide-details @change="updateSortingAndReload"
              :value="sortingProperty"></v-select>
</template>

<script>
    import { mapActions, mapGetters, mapMutations } from "vuex";

    const applicationMetadata = [
        { name: "Name", value: "name" },
        { name: "Updated", value: "updateTime" },
        { name: "Created", value: "creationTime" }
    ];

    export default {
        name: "SortSelect",
        computed: {
            ...mapGetters('settings/metadata', ['sortableMetadata']),
            ...mapGetters('search', ['sortingProperty']),
            sortableProperties() {
                return applicationMetadata.concat(this.sortableMetadata);
            }
        },
        methods: {
            ...mapMutations('search', ['updateSortingProperty']),
            ...mapActions('videos', ['search']),
            updateSortingAndReload(property) {
                this.updateSortingProperty(property);
                this.search();
            }
        }
    }
</script>

<style scoped>
    .nav-select {
        padding-top: 10px;
        max-width: 500px;
    }
</style>
