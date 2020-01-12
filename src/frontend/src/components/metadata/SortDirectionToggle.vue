<template>
    <v-btn @click="toggleSortingDirection" text icon :title="'Change sorting to ' + oppositeDirectionText">
        <v-icon>{{sortingIcon}}</v-icon>
    </v-btn>
</template>

<script>
    import { mapActions, mapGetters, mapMutations } from "vuex";

    export default {
        name: "SortDirectionToggle",
        computed: {
            ...mapGetters('search', ['sortingDirection']),
            oppositeDirectionText() {
                return (this.sortingDirection === 'ASC' ? 'descending' : 'ascending');
            },
            sortingIcon() {
                return 'arrow_' + (this.sortingDirection === 'ASC' ? 'upward' : 'downward');
            }
        },
        methods: {
            ...mapMutations('search', ['updateSortingDirection']),
            ...mapActions('videos', ['search']),
            toggleSortingDirection() {
                let targetDirection = (this.sortingDirection === 'ASC' ? 'DESC' : 'ASC');
                this.updateSortingDirection(targetDirection);
                this.search();
            }
        }
    }
</script>

<style scoped>

</style>
