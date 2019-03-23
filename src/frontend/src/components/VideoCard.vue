<template>
    <v-flex xs4 md3>
        <v-card>
            <v-img height="250px" v-bind:src="thumbnailUrl"></v-img>
            <v-card-title>{{ video.name }}</v-card-title>
            <v-card-text>
                <v-layout column>
                    <v-layout v-for="(metadata, index) in video.metadata" xs12 column :key="index">
                        <v-layout justift-space-between row my-1>
                            <v-flex>{{ metadata.metadata.name }}</v-flex>
                            <v-flex class="text-xs-right">
                                <metadata-value-display :metadata-value="metadata"></metadata-value-display>
                            </v-flex>
                        </v-layout>
                        <v-divider xs12 v-if="index < video.metadata.length - 1"></v-divider>
                    </v-layout>
                </v-layout>
            </v-card-text>
            <v-card-actions>
                <v-btn flat color="orange" :to="watchRoute">Play</v-btn>
                <v-btn flat color="orange" @click="edit">Edit</v-btn>
            </v-card-actions>
        </v-card>
    </v-flex>
</template>

<script>
    import { mapActions, mapGetters } from "vuex";
    import MetadataValueDisplay from "./MetadataValueDisplay";

    export default {
        name: "VideoCard",
        components: { MetadataValueDisplay },
        props: ['video_id'],
        computed: {
            ...mapGetters('videos', [
                'thumbnailOf',
                'getVideo'
            ]),
            video() {
                return this.getVideo(this.video_id);
            },
            thumbnailUrl() {
                return `/api/video/${this.video_id}/thumbnail/${this.thumbnailOf(this.video_id).id}`;
            },
            watchRoute() {
                return `/watch/${this.video_id}`;
            }
        },
        methods: {
            edit() {
                this.editVideo(this.video_id);
            },
            ...mapActions('videos', [
                'editVideo'
            ])
        }
    }
</script>

<style scoped>
</style>
