<template>
    <v-flex row xs12 fill-height>
        <v-flex column fill-height>
            <video ref="videoPlayer" class="video-js vjs-big-play-centered" @ended="onVideoFinished" @playing="onVideoStarted"></video>
        </v-flex>
        <v-flex column xs2 fill-height v-if="hasQueue" style="max-height: 90vh; overflow-y: scroll;">
            <v-list>
                <v-list-item v-for="video in videosInQueue" :key="video.id">
                    <v-list-item-avatar><v-img :src="_thumbnailForVideo(video)"></v-img></v-list-item-avatar>
                    <v-list-item-content>
                        <v-list-item-title>
                            <v-icon v-if="currentVideo.id === video.id">play_arrow</v-icon>
                            {{video.name}}
                        </v-list-item-title>
                    </v-list-item-content>
                    <v-list-item-action>
                        <v-flex column>
                            <v-btn @click="skipToVideo(video.id)" icon><v-icon>play_arrow</v-icon></v-btn>
                            <v-btn @click="removeVideoFromQueue(video.id)" icon><v-icon>delete</v-icon></v-btn>
                        </v-flex>
                    </v-list-item-action>
                </v-list-item>
            </v-list>
        </v-flex>
    </v-flex>
</template>

<script>
    import { mapActions, mapGetters } from "vuex";
    import { getSelectedThumbnailURLForVideo, getStreamURLForVideo } from "../video";
    import videojs from 'video.js';
    import events from '../api/event';

    export default {
        name: "VideoPlayer",
        props: {
            autoplay: {
                type: Boolean
            },
            disableEvents: false
        },
        data() {
            return {
                player: null,
                videoWatcher: null
            };
        },
        watch: {
            currentVideoThumbnail(newValue) {
                if(newValue != null && newValue.length > 0) {
                    this.player.poster(newValue);
                }
            },
            currentVideoStream(newValue) {
                if(newValue != null && newValue.length > 0) {
                    this.player.src({
                        src: newValue,
                        type: 'video/mp4'
                    });
                    this.player.thumbnails().src(newValue);
                }
            }
        },
        computed: {
            ...mapGetters('player', ['currentVideo', 'videosInQueue', 'hasQueue']),
            ...mapGetters('videos', [
                'videoThumbnail'
            ]),
            currentVideoStream() {
                return getStreamURLForVideo(this.currentVideo);
            },
            currentVideoThumbnail() {
                return getSelectedThumbnailURLForVideo(this.currentVideo);
            },
            videoPlayerOptions() {
                return {
                    autoplay: this.autoplay,
                    controls: true,
                    fill: true
                }
            }
        },
        mounted() {
            this.player = videojs(this.$refs.videoPlayer, this.videoPlayerOptions);
            this.player.thumbnails({ play: true });
            this.videoWatcher = this.$store.watch(
                (state) => state.player.currentlyPlayingVideo,
                (newValue) => {
                    if(newValue == null) return;
                    this.refreshCurrentVideo();
                }
            );

            if(this.currentVideo != null) {
                this.refreshCurrentVideo();
            }
        },
        beforeDestroy() {
            if(this.player) {
                this.player.dispose();
                this.videoWatcher();
            }
        },
        methods: {
            ...mapActions('videos', ['reloadVideo']),
            ...mapActions('player', ['nextVideo', 'removeVideoFromQueue', 'skipToVideo']),
            refreshCurrentVideo() {
                this.player.src([
                    {
                        src: this.currentVideoStream,
                        type: 'video/mp4'
                    }
                ]);
                this.player.poster(this.currentVideoThumbnail);
                this.player.thumbnails().src(this.currentVideoStream);
            },
            onVideoFinished() {
                if(!this.disableEvents) {
                    events.watchFinishEvent(this.currentVideo.id).then(() => this.reloadVideo(this.currentVideo.id));
                }
                this.nextVideo();
            },
            onVideoStarted() {
                if(!this.disableEvents) {
                    events.watchStartEvent(this.currentVideo.id).then(() => this.reloadVideo(this.currentVideo.id));
                }
            },
            _thumbnailForVideo(video) {
                return getSelectedThumbnailURLForVideo(video);
            }
        }
    }
</script>

<style scoped>
</style>
