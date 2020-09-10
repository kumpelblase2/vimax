<template>
    <v-flex row xs12 fill-height>
        <v-flex column fill-height>
            <video ref="videoPlayer" class="video-js vjs-big-play-centered"/>
        </v-flex>
    </v-flex>
</template>

<script>
    import { mapActions, mapGetters } from "vuex";
    import { getSelectedThumbnailURLForVideo, getStreamURLForVideo } from "@/video";
    import videojs from 'video.js';

    export default {
        name: "SingleVideoPlayer",
        props: {
            autoplay: {
                type: Boolean
            },
            video: Object
        },
        data() {
            return {
                player: null
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
                    this.hasStarted = false;
                }
            },
            video(newValue) {
                if(newValue != null) {
                    this.refreshCurrentVideo();
                }
            }
        },
        computed: {
            ...mapGetters('videos', ['videoThumbnail']),
            currentVideoStream() {
                return getStreamURLForVideo(this.video);
            },
            currentVideoThumbnail() {
                return getSelectedThumbnailURLForVideo(this.video);
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

            if(this.video != null) {
                this.refreshCurrentVideo();
            }
        },
        beforeDestroy() {
            if(this.player) {
                this.player.dispose();
            }
        },
        methods: {
            ...mapActions('videos', ['reloadVideo']),
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
            _thumbnailForVideo(video) {
                return getSelectedThumbnailURLForVideo(video);
            }
        }
    }
</script>
