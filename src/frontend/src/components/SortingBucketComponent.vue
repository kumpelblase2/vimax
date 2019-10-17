<template>
    <v-flex xs3>
        <v-sheet class="sorting-card">
            <v-layout row>
                <v-flex pt-2 pl-2>
                    <p class="title">{{bucketIndex + 1}}</p>
                </v-flex>
                <v-spacer></v-spacer>
                <v-btn icon xs1 @click="deleteCard">
                    <v-icon>close</v-icon>
                </v-btn>
            </v-layout>
            <metadata-value-editor :metadata-definition="selectedMetadata"
                                   :metadata-value="myBucketAssignedValue"
                                   @change="updateValue({value: $event, index: bucketIndex})" solo></metadata-value-editor>

            <v-btn @click="$emit('click', $event)">Assign</v-btn>
        </v-sheet>
    </v-flex>
</template>

<script>
    import MetadataValueEditor from "./MetadataValueEditor";
    import { mapGetters, mapMutations, mapState } from 'vuex';

    export default {
        name: "SortingBucketComponent",
        components: { MetadataValueEditor },
        props: {
            bucketIndex: {
                type: Number
            }
        },
        computed: {
            ...mapState('settings/metadata', ['possibleMetadataTypes','metadata']),
            ...mapGetters('sorting', [
                "selectedMetadata",
                "bucketAssignedValue"
            ]),
            myBucketAssignedValue() {
                return this.bucketAssignedValue(this.bucketIndex);
            }
        },
        methods: {

            ...mapMutations('sorting', [
                'deleteBucket',
                'updateValue'
            ]),
            deleteCard() {
                this.deleteBucket(this.bucketIndex);
            }
        }
    }
</script>

<style scoped>
    .sorting-card {
        min-height: 200px;
    }
</style>
