<template>
    <v-flex xs12 fill-height>
        <video v-if="videoId != null" ref="videoPlayer" class="video-js" @ended="onVideoFinished"
               @playing="onVideoStarted"></video>
    </v-flex>
</template>

<script>
    import { mapGetters, mapActions } from "vuex";
    import { getStreamURLForVideo, getThumbnailURLForVideo } from "../video";
    import videojs from 'video.js';
    import events from '../api/event';

    export default {
        name: "VideoPlayer",
        props: {
            autoplay: {
                type: Boolean
            },
            videoId: null,
            disableEvents: false
        },
        data() {
            return {
                player: null
            };
        },
        watch: {
            videoId(newValue) {
                this.player.src([
                    {
                        src: this.myVideoStream,
                        type: 'video/mp4'
                    }
                ]);
                this.player.poster(this.myVideoThumbnail);
                this.player.thumbnails().src(this.myVideoStream);
                events.watchStartEvent(newValue);
            }
        },
        computed: {
            ...mapGetters('videos', [
                'videoThumbnail'
            ]),
            myVideoStream() {
                return getStreamURLForVideo(this.videoId);
            },
            myVideoThumbnail() {
                const thumbnail = this.videoThumbnail(this.videoId);
                if(thumbnail == null) {
                    return "";
                } else {
                    return getThumbnailURLForVideo(this.videoId, thumbnail);
                }
            },
            videoPlayerOptions() {
                return {
                    autoplay: this.autoplay,
                    controls: true,
                    sources: [
                        {
                            src: this.myVideoStream,
                            type: 'video/mp4'
                        }
                    ],
                    fill: true,
                    poster: this.myVideoThumbnail
                }
            }
        },
        mounted() {
            this.player = videojs(this.$refs.videoPlayer, this.videoPlayerOptions);
            this.player.thumbnails({ play: true });
        },
        beforeDestroy() {
            if(this.player) {
                this.player.dispose();
            }
        },
        methods: {
            ...mapActions('videos', ['reloadVideo']),
            onVideoFinished() {
                if(!this.disableEvents) {
                    events.watchFinishEvent(this.videoId).then(() => this.reloadVideo(this.videoId));
                }
            },
            onVideoStarted() {
                if(!this.disableEvents) {
                    events.watchStartEvent(this.videoId).then(() => this.reloadVideo(this.videoId));
                }
            }
        }
    }
</script>

<style scoped>
</style>
