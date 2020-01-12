<template>
    <v-flex xs12 sm6 md3 xl2 class="pa-1">
            <v-card>
                <v-img :class="{ 'selected-video': selected }" :aspect-ratio="16/10" :src="thumbnailUrl"
                       @mouseenter="startHover" @mouseleave="stopHover">
                    <v-row fill-height v-show="hover" style="margin: 5px;">
                        <v-btn text icon color="orange" @click="toggleSelection">
                            <v-icon v-if="selected">check_box</v-icon>
                            <v-icon v-else>check_box_outline_blank</v-icon>
                        </v-btn>
                        <v-spacer></v-spacer>
                        <v-btn text icon color="orange" :to="watchRoute">
                            <v-icon>play_arrow</v-icon>
                        </v-btn>
                        <v-btn text icon color="orange" @click="edit">
                            <v-icon>edit</v-icon>
                        </v-btn>
                    </v-row>
                </v-img>
                <v-card-title>{{ video.name }}</v-card-title>
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
</style>

<script>
    import { mapActions, mapGetters } from "vuex";
    import VideoMetadataDisplay from "./VideoMetadataDisplay";
    import { getSelectedThumbnailURLForVideo } from "../../video";

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
            },
            watchRoute() {
                return `/watch/${this.videoId}`;
            }
        },
        methods: {
            ...mapActions('videos', ['toggleSelectVideo', 'loadVideos']),
            ...mapActions('videos/editing', ['editVideo']),
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
            }
        }
    }
</script>

