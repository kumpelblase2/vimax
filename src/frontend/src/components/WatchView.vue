<template>
    <video-player></video-player>
</template>

<script>
    import VideoPlayer from "./VideoPlayer";
    import { mapActions, mapGetters } from "vuex";

    export default {
        name: "WatchView",
        components: { VideoPlayer },
        computed: {
            ...mapGetters('videos', [
                'activeVideo'
            ])
        },
        methods: {
            ...mapActions('videos', [
                'makeVideoActive'
            ])
        },
        mounted() {
            this.makeVideoActive(this.$route.params.id);
        }
    }
</script>

<style scoped>

</style>
