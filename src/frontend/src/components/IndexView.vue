<template>
    <v-container fluid grid-list>
        <v-layout row wrap justify-space-around>
            <VideoCard v-for="video in videos" :key="video.id" :video-id="video.id"></VideoCard>
            <MugenScroll :handler="loadVideosOfCurrentPage" :should-handle="!isLoading" handle-on-mount>
                <div class="mugen-loading">Loading...</div>
            </MugenScroll>
        </v-layout>
        <video-edit-dialog></video-edit-dialog>
    </v-container>
</template>

<script>
    import MugenScroll from 'vue-mugen-scroll'
    import { mapActions, mapState } from 'vuex';
    import VideoCard from "./VideoCard";
    import VideoEditDialog from "./VideoEditDialog";

    export default {
        name: "Library",
        components: { VideoEditDialog, VideoCard, MugenScroll },
        mounted() {
            this.loadMetadata();
        },
        computed: {
            ...mapState('videos', [
                'videos',
                'isLoading'
            ])
        },
        methods: {
            ...mapActions('videos', [
                'loadRecentVideos',
                'loadVideosOfCurrentPage'
            ]),
            ...mapActions('settings/metadata',[
                'loadMetadata'
            ]),
        }
    }
</script>

<style>
    .mugen-scroll {
        flex: 1;
        display: flex;
        align-items: center;
    }

    .mugen-loading {
        display: flex;
        flex: 1;
        justify-content: center;
        padding-top: 20px;
        padding-bottom: 20px;
    }
</style>
