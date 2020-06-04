<template>
    <v-select class="nav-select" :items="sortableProperties" item-text="name" hide-details @change="updateSortingAndReload"
              :value="sortingProperty"></v-select>
</template>

<script>
    import { mapActions, mapGetters, mapMutations } from "vuex";

    const applicationMetadata = [
        { name: "Name", value: "name", ordering: "ASC" },
        { name: "Updated", value: "updateTime", ordering: "DESC" },
        { name: "Created", value: "creationTime", ordering: "DESC" }
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
            ...mapMutations('search', ['updateSortingProperty', 'updateSortingDirection']),
            ...mapActions('videos', ['search']),
            updateSortingAndReload(property) {
                this.updateSortingProperty(property);
                const metadata = this.sortableProperties.find(metadata => metadata.name === property || metadata.value === property);
                if(metadata != null) {
                    this.updateSortingDirection(metadata.ordering);
                }
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
