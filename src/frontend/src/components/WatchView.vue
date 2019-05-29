<template>
    <video-player :video-id="videoId"></video-player>
</template>

<script>
    import VideoPlayer from "./VideoPlayer";

    export default {
        name: "WatchView",
        components: { VideoPlayer },
        computed: {
            videoId() {
                return parseInt(this.$route.params.id);
            }
        }
    }
</script>

<style scoped>

</style>
