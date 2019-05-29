<template>
    <v-flex xs4 md3>
        <v-card>
            <v-img height="250px" v-bind:src="thumbnailUrl"></v-img>
            <v-card-title>{{ video.name }}</v-card-title>
            <v-card-text>
                <VideoMetadataDisplay :video-metadata="video.metadata"/>
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
    import VideoMetadataDisplay from "./VideoMetadataDisplay";
    import { getThumbnailURLForVideo } from "../video";

    export default {
        name: "VideoCard",
        components: { VideoMetadataDisplay},
        props: ['video-id'],
        computed: {
            ...mapGetters('videos', [
                'thumbnailOf',
                'getVideo'
            ]),
            video() {
                return this.getVideo(this.videoId);
            },
            thumbnailUrl() {
                return getThumbnailURLForVideo(this.videoId, this.thumbnailOf(this.videoId));
            },
            watchRoute() {
                return `/watch/${this.videoId}`;
            }
        },
        methods: {
            ...mapActions('videos', [
                'editVideo'
            ]),
            edit() {
                this.editVideo(this.videoId);
            }
        }
    }
</script>

