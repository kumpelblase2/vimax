<template>
    <v-select class="nav-select" :items="sortableProperties" item-text="name" solo @change="updateSortingProperty"
              v-model="sortingProperty"></v-select>
</template>

<script>
    import { mapGetters, mapMutations } from "vuex";

    export default {
        name: "SortSelect",
        computed: {
            ...mapGetters('settings/metadata', ['sortableMetadata']),
            ...mapGetters('search', ['sortingProperty']),
            sortableProperties() {
                return [{name: "Name"}].concat(this.sortableMetadata);
            }
        },
        methods: {
            ...mapMutations('search', ['updateSortingProperty'])
        }
    }
</script>

<style scoped>
    .nav-select {
        padding-top: 10px;
        max-width: 500px;
    }
</style>
