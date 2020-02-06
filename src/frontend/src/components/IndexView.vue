<template>
    <div>
        <v-container fluid grid-list v-if="hasVideosToDisplay || isLoading">
            <v-row wrap justify="space-around">
                <VideoCard v-for="videoId in displayVideoIds" :key="videoId" :video-id="videoId" />
                <MugenScroll v-show="hasMoreVideos" :handler="loadVideosOfCurrentPage" :should-handle="!isLoading && hasMoreVideos">
                    <div class="mugen-loading">Loading...</div>
                </MugenScroll>
            </v-row>
            <video-edit-dialog />
            <multi-video-edit-dialog/>
        </v-container>
        <v-container v-else>
            <v-row>No videos found</v-row>
        </v-container>
    </div>
</template>

<script>
    import MugenScroll from 'vue-mugen-scroll'
    import { mapActions, mapGetters, mapMutations, mapState } from 'vuex';
    import VideoCard from "./video/VideoCard";
    import VideoEditDialog from "./video/VideoEditDialog";
    import MultiVideoEditDialog from "./video/MultiVideoEditDialog";

    export default {
        name: "Library",
        components: { MultiVideoEditDialog, VideoEditDialog, VideoCard, MugenScroll },
        async mounted() {
            this.resetPage();
            await this.loadMetadata();
            return this.search();
        },
        computed: {
            ...mapState('videos', ['isLoading','hasMoreVideos','displayVideoIds']),
            ...mapGetters('videos', ['hasVideosToDisplay'])
        },
        methods: {
            ...mapActions('videos', ['search','loadVideosOfCurrentPage']),
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
