<template>
    <v-flex xs12 fill-height>
        <video v-if="videoId != null" ref="videoPlayer" class="video-js"></video>
    </v-flex>
</template>

<script>
    import { mapGetters } from "vuex";
    import { getStreamURLForVideo, getThumbnailURLForVideo } from "../video";
    import videojs from 'video.js';

    export default {
        name: "VideoPlayer",
        props: {
            autoplay: {
                type: Boolean
            },
            videoId: null,
        },
        data() {
            return {
                player: null
            };
        },
        watch: {
            videoId() {
                this.player.src([
                    {
                        src: this.myVideoStream,
                        type: 'video/mp4'
                    }
                ]);
                this.player.poster(this.myVideoThumbnail);
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
        }
    }
</script>

<style scoped>
</style>
