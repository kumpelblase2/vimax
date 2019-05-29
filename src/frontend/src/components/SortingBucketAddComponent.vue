<template>
    <v-flex>
        <v-sheet>
            <v-layout row justify-center align-center class="sorting-card">
                <v-btn @click="addBucket">
                    <v-icon>add</v-icon>
                </v-btn>
            </v-layout>
        </v-sheet>
    </v-flex>
</template>

<script>
    import { mapMutations } from 'vuex';

    export default {
        name: "SortingBucketAddComponent",
        methods: {
            ...mapMutations('sorting', [
                'addBucket'
            ])
        }
    }
</script>

<style scoped>
    .sorting-card {
        min-height: 200px;
    }
</style>
