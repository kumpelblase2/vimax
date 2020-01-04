<template>
    <v-flex xs3 class="pa-2">
        <v-sheet class="sorting-card px-3 py-1">
            <v-row>
                <v-flex pt-2 pl-2>
                    <p class="title">{{bucketIndex + 1}}</p>
                </v-flex>
                <v-spacer></v-spacer>
                <v-btn icon xs1 @click="deleteCard">
                    <v-icon>close</v-icon>
                </v-btn>
            </v-row>
            <metadata-value-editor :metadata-definition="selectedMetadata"
                                   :metadata-value="myBucketAssignedValue"
                                   @change="updateValue({value: $event, index: bucketIndex})" solo></metadata-value-editor>

            <v-btn @click="selectBucket($event)">Assign</v-btn>
        </v-sheet>
    </v-flex>
</template>

<script>
    import MetadataValueEditor from "../metadata/MetadataValueEditor";
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
            },
            selectBucket(event) {
                setTimeout(() => {
                    this.$emit('click', event);
                }, 100);
            }
        }
    }
</script>

<style scoped>
    .sorting-card {
        min-height: 200px;
    }

    .row {
        margin: 0 !important;
    }
</style>
