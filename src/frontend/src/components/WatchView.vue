<template>
    <video-player autoplay></video-player>
</template>

<script>
    import VideoPlayer from "./VideoPlayer";
    import { mapActions, mapMutations } from "vuex";

    export default {
        name: "WatchView",
        components: { VideoPlayer },
        async mounted() {
            if(this.$route.params.id != null) {
                const videoId = parseInt(this.$route.params.id);
                await this.loadVideos([videoId]);
                this.setCurrentVideo(videoId);
            }
        },
        methods: {
            ...mapActions('videos', ['loadVideos']),
            ...mapMutations('player', ['setCurrentVideo'])
        }
    }
</script>

<style scoped>

</style>
