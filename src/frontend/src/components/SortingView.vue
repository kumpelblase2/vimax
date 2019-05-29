<template>
    <v-layout fill-height column>
        <v-dialog v-model="showDialog">
            <v-card>
                <v-card-text>
                    <v-select :items="orderedMetadata" label="Metadata" @change="updateMetadata"
                              item-text="name" return-object></v-select>
                </v-card-text>
            </v-card>
        </v-dialog>
        <div v-if="selectedMetadata != null">
            <v-layout row wrap>
                <v-flex xs12>
                    <v-btn block @click="reset">Reset</v-btn>
                </v-flex>
                <sorting-bucket-component v-bind:key="index" v-for="(bucket,index) in buckets"
                                          :bucket-index="index" @click="assignToBucket(index)"></sorting-bucket-component>
                <v-flex v-if="buckets.length < 8" xs3>
                    <sorting-bucket-add-component></sorting-bucket-add-component>
                </v-flex>
            </v-layout>
            <v-layout row v-if="nextVideo">
                <v-flex xs8>
                    <video-player :video-id="videoId" :autoplay="false"></video-player>
                </v-flex>
                <v-flex xs4 style="height: 100%">
                    <v-card>
                        <v-card-title>
                            {{nextVideo.name}}
                        </v-card-title>
                        <v-card-text>
                            <video-metadata-display :video-metadata="nextVideo.metadata"></video-metadata-display>
                        </v-card-text>
                    </v-card>
                </v-flex>
            </v-layout>
        </div>
        <v-layout align-center justify-center v-if="selectedMetadata != null && empty">
            <v-flex class="elevation-2" pt-3 xs-4>
                <p class="font-italic text-xs-center">No more videos to sort.</p>
            </v-flex>
        </v-layout>
    </v-layout>
</template>

<script>
    import { mapActions, mapGetters, mapMutations, mapState } from 'vuex';
    import SortingBucketComponent from "./SortingBucketComponent";
    import SortingBucketAddComponent from "./SortingBucketAddComponent";
    import VideoPlayer from "./VideoPlayer";
    import VideoMetadataDisplay from "./VideoMetadataDisplay";

    export default {
        name: "SortingView",
        data() {
            return {
                listener: null
            };
        },
        components: { VideoMetadataDisplay, VideoPlayer, SortingBucketAddComponent, SortingBucketComponent },
        computed: {
            ...mapState('sorting', [
                'selectedMetadata',
                'buckets'
            ]),
            ...mapGetters('sorting', ['nextVideo', "empty"]),
            ...mapGetters('settings/metadata', ['orderedMetadata']),
            videoId() {
                return this.nextVideo.id;
            },
            showDialog() {
                return this.selectedMetadata == null;
            },
        },
        methods: {
            ...mapActions('settings/metadata', ["loadMetadata"]),
            ...mapActions('sorting', ["loadSortableVideos", "assignVideoToBucket","assignVideoToNothing"]),
            ...mapMutations('sorting', ['updateMetadata', 'deleteBucket', 'clearBuckets']),
            reset() {
                this.updateMetadata(null);
                this.clearBuckets();
            },
            assignToBucket(bucketNumber) {
                if(bucketNumber < 0) {
                    this.assignVideoToNothing();
                } else {
                    this.assignVideoToBucket(bucketNumber);
                }
            }
        },
        watch: {
            selectedMetadata(newValue) {
                if(newValue != null) {
                    this.loadSortableVideos();
                }
            }
        },
        mounted() {
            this.loadMetadata();
            this.listener = (event) => {
                if(event.code.startsWith("Digit")) {
                    const digit = event.code.replace("Digit", "");
                    const number = parseInt(digit);
                    this.assignToBucket(number - 1);
                }
            };
            window.addEventListener('keyup', this.listener);
        },
        destroyed() {
            window.removeEventListener('keyup', this.listener);
            this.listener = null;
        }
    }
</script>

<style scoped>

</style>
