<template>
    <div class="scroll-container" ref="container">
        <v-container fluid grid-list v-resize="onResize">
            <VirtualFlexWrap :cell-component="videoComponent" :items="videoItems" :column-count="columns"
                             :item-height="videoHeight"
                             :scroll-container="container" :row-buffer-count="2"/>
            <video-edit-dialog/>
            <multi-video-edit-dialog/>
            <video-info-dialog/>
        </v-container>
    </div>
</template>

<script>
import VideoCardWrapper from "@/components/video/VideoCardWrapper";
import VideoInfoDialog from "@/components/video/VideoInfoDialog";
import VirtualFlexWrap from 'virtual-flex-wrap/src/virtual-flex-wrap';
import { mapActions, mapGetters, mapMutations, mapState } from 'vuex';
import MultiVideoEditDialog from "./video/MultiVideoEditDialog";
import VideoCard from "./video/VideoCard";
import VideoEditDialog from "./video/VideoEditDialog";

export default {
    name: "Library",
    components: { VirtualFlexWrap, VideoInfoDialog, MultiVideoEditDialog, VideoEditDialog, VideoCard },
    data() {
        return {
            videoComponent: VideoCardWrapper,
            container: null,
            columns: 6,
            baseVideoHeight: 284
        }
    },
    async mounted() {
        if(this.$route.query.search != null) {
            this.updateQuery(this.$route.query.search);
        }
        this.container = this.$refs.container;
        this.onResize();
        this.setLoading(true);
        this.resetPage();
        await this.loadMetadata();
        await this.search();
    },
    computed: {
        ...mapState('videos', ['isLoading', 'displayVideoIds']),
        ...mapGetters('videos', ['hasVideosToDisplay']),
        ...mapGetters('settings/metadata', ['visibleMetadata']),
        videoItems() {
            return this.displayVideoIds.map(id => ({ id }));
        },
        videoHeight() {
            const paddingFromTable = this.visibleMetadata.length > 0 ? 16 : 0;
            return this.baseVideoHeight + this.visibleMetadata.length * 32 + paddingFromTable;
        }
    },
    methods: {
        ...mapActions('videos', ['search', 'loadVideos']),
        ...mapMutations('videos', ['resetPage', 'setLoading']),
        ...mapMutations('search', ['updateQuery']),
        ...mapActions('settings/metadata', ['loadMetadata']),
        refreshVideoSize(containerWidth, columns) {
            const availableWidth = containerWidth - (12 * 2) /*Remove padding*/;
            const columnWidth = availableWidth / columns;
            const imageWidth = columnWidth - (4 * 2) /*Remove padding*/;
            const imageHeight = imageWidth * (10 / 16); // 16/10 aspect ratio is being preserved
            const textSize = 2 * parseFloat(window.getComputedStyle(document.documentElement).fontSize); // convert 2rem to pixels
            this.baseVideoHeight = imageHeight + (4 * 2) /*Padding Card*/ + (8 * 2) /*Padding Title*/ + textSize;
        },
        onResize() {
            const windowSize = window.innerWidth;
            if(windowSize < 600) {
                this.columns = 1;
            } else if(windowSize < 960) {
                this.columns = 2;
            } else if(windowSize < 1264) {
                this.columns = 4;
            } else {
                this.columns = 6;
            }
            this.refreshVideoSize(this.$refs.container.clientWidth, this.columns);
        }
    }
}
</script>

<style>
</style>
