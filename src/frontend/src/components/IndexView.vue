<template>
    <v-container fluid grid-list>
        <v-layout row wrap justify-space-around>
            <VideoCard v-for="video in videos" :key="video.id" :video-id="video.id"></VideoCard>
        </v-layout>
        <video-edit-dialog></video-edit-dialog>
    </v-container>
</template>

<script>
    import { mapActions, mapState } from 'vuex';
    import VideoCard from "./VideoCard";
    import VideoEditDialog from "./VideoEditDialog";

    export default {
        name: "Library",
        components: { VideoEditDialog, VideoCard },
        mounted() {
            this.loadMetadata();
            this.loadAllVideos();
        },
        computed: {
            ...mapState('videos', [
                'videos'
            ])
        },
        methods: {
            ...mapActions('videos', [
                'loadRecentVideos',
                'loadAllVideos'
            ]),
            ...mapActions('settings/metadata',[
                'loadMetadata'
            ])
        }
    }
</script>

<style scoped>

</style>
