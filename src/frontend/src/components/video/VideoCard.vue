<template>
    <v-flex xs12 sm6 md3 xl2 class="pa-1">
        <v-card>
            <v-img :class="{ 'selected-video': selected }" :aspect-ratio="16/10" :src="thumbnailUrl"
                   @mouseenter="startHover" @mouseleave="stopHover">
                <v-row fill-height v-show="hover" class="ma-1">
                    <v-btn text icon color="primary" @click="toggleSelection">
                        <v-icon v-if="selected">check_box</v-icon>
                        <v-icon v-else>check_box_outline_blank</v-icon>
                    </v-btn>
                    <v-spacer></v-spacer>
                    <v-menu offset-y>
                        <template v-slot:activator="{ on }">
                            <v-btn icon color="primary" v-on="on">
                                <v-icon>more_vert</v-icon>
                            </v-btn>
                        </template>
                        <v-list>
                            <v-list-item @click="playNext"><v-list-item-title>Play Next</v-list-item-title></v-list-item>
                            <v-list-item @click="appendQueue"><v-list-item-title>Add to Queue</v-list-item-title></v-list-item>
                            <v-list-item @click="showVideo"><v-list-item-title>Info</v-list-item-title></v-list-item>
                        </v-list>
                    </v-menu>
                    <v-btn text icon color="primary" @click="watchVideo">
                        <v-icon>play_arrow</v-icon>
                    </v-btn>
                    <v-btn text icon color="primary" @click="edit">
                        <v-icon>edit</v-icon>
                    </v-btn>
                </v-row>
            </v-img>
            <v-card-title><span class="ellipsis-text">{{ video.name }}</span></v-card-title>
            <v-card-text v-show="hasVisibleMetadata">
                <VideoMetadataDisplay :video-metadata="video.metadata"/>
            </v-card-text>
        </v-card>
    </v-flex>
</template>

<style scoped>
    .selected-video {
        outline: 5px solid orange;
        outline-offset: -5px;
    }

    .ellipsis-text {
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
    }
</style>

<script>
import { mapActions, mapGetters, mapMutations } from "vuex";
    import VideoMetadataDisplay from "./VideoMetadataDisplay";
    import { getSelectedThumbnailURLForVideo } from "@/video";

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
            ...mapGetters('videos', ['getVideo', 'isSelected']),
            ...mapGetters('settings/metadata', ['hasVisibleMetadata']),
            selected() {
                return this.isSelected(this.videoId);
            },
            video() {
                return this.getVideo(this.videoId);
            },
            thumbnailUrl() {
                return getSelectedThumbnailURLForVideo(this.video);
            }
        },
        methods: {
            ...mapActions('videos', ['toggleSelectVideo', 'loadVideos']),
            ...mapActions('player', ['playVideo', 'playVideoNext','queueVideoIn']),
            ...mapActions('videos/editing', ['editVideo']),
            ...mapMutations('videos', ["displayVideo"]),
            edit() {
                this.editVideo(this.videoId);
            },
            startHover() {
                this.hover = true;
            },
            stopHover() {
                this.hover = false;
            },
            toggleSelection() {
                this.toggleSelectVideo(this.videoId);
            },
            watchVideo() {
                this.playVideo(this.videoId);
            },
            playNext() {
                this.playVideoNext(this.videoId);
            },
            appendQueue() {
                this.queueVideoIn(this.videoId);
            },
            showVideo() {
                this.displayVideo(this.videoId);
            }
        }
    }
</script>

