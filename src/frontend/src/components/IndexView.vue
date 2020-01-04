<template>
    <v-container fluid grid-list>
        <v-row wrap justify="space-around">
            <VideoCard v-for="video in displayedVideos" :key="video.id" :video-id="video.id"></VideoCard>
            <MugenScroll v-if="hasMoreVideos" :handler="loadVideosOfCurrentPage" :should-handle="!isLoading" handle-on-mount>
                <div class="mugen-loading">Loading...</div>
            </MugenScroll>
        </v-row>
        <video-edit-dialog></video-edit-dialog>
    </v-container>
</template>

<script>
    import MugenScroll from 'vue-mugen-scroll'
    import { mapActions, mapGetters, mapMutations, mapState } from 'vuex';
    import VideoCard from "./video/VideoCard";
    import VideoEditDialog from "./video/VideoEditDialog";

    export default {
        name: "Library",
        components: { VideoEditDialog, VideoCard, MugenScroll },
        async mounted() {
            this.resetPage();
            await this.loadMetadata();
        },
        computed: {
            ...mapState('videos', ['isLoading','hasMoreVideos']),
            ...mapGetters('videos', ['displayedVideos'])
        },
        methods: {
            ...mapActions('videos', ['loadVideosOfCurrentPage']),
            ...mapMutations('videos', ['resetPage']),
            ...mapActions('settings/metadata',['loadMetadata'])
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
