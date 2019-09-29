<template>
    <v-flex xs12 sm6 md3 xl2 class="px-1">
        <v-card>
            <v-img :aspect-ratio="16/9" v-bind:src="thumbnailUrl" @mouseenter="startHover" @mouseleave="stopHover">
                <v-layout row fill-height v-show="hover">
                    <v-spacer></v-spacer>
                    <v-btn flat icon color="orange" :to="watchRoute"><v-icon>play_arrow</v-icon></v-btn>
                    <v-btn flat icon color="orange" @click="edit"><v-icon>edit</v-icon></v-btn>
                </v-layout>
            </v-img>
            <v-card-title>{{ video.name }}</v-card-title>
            <v-card-text>
                <VideoMetadataDisplay :video-metadata="video.metadata"/>
            </v-card-text>
        </v-card>
    </v-flex>
</template>

<script>
    import { mapActions, mapGetters } from "vuex";
    import VideoMetadataDisplay from "./VideoMetadataDisplay";
    import { getThumbnailURLForVideo } from "../video";

    export default {
        name: "VideoCard",
        components: { VideoMetadataDisplay },
        props: ['video-id'],
        data() {
            return {
                hover: false
            }
        },
        computed: {
            ...mapGetters('videos', [
                'thumbnailOf',
                'getVideo'
            ]),
            video() {
                return this.getVideo(this.videoId);
            },
            thumbnailUrl() {
                let thumbnail = this.thumbnailOf(this.videoId);
                if(thumbnail == null) {
                    return "";
                } else {
                    return getThumbnailURLForVideo(this.videoId, thumbnail);
                }
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
            },
            startHover() {
                this.hover = true;
            },
            stopHover() {
                this.hover = false;
            }
        }
    }
</script>

