<template>
    <v-flex xs12 fill-height>
        <video v-if="videoId != null" :src="myVideoStream" :poster="myVideoThumbnail" :autoplay="autoplay" controls></video>
    </v-flex>
</template>

<script>
    import { mapGetters } from "vuex";
    import { getStreamURLForVideo } from "../video";

    export default {
        name: "VideoPlayer",
        props: {
            autoplay: true,
            videoId: null
        },
        computed: {
            ...mapGetters('videos', [
                'videoThumbnail'
            ]),
            myVideoStream() {
                return getStreamURLForVideo(this.videoId);
            },
            myVideoThumbnail() {
                return this.videoThumbnail(this.videoId);
            }
        }
    }
</script>

<style scoped>
    video {
        width: 100%;
        max-height: 85vh;
        object-fit: fill;
    }
</style>
