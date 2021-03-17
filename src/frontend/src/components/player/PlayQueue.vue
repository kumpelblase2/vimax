<template>
    <v-list style="max-height: 90%" class="overflow-y-auto">
        <div v-show="hasMoreBefore" class="hidden-items-entry">...</div>
        <draggable handle=".drag-item" v-model="orderedVideos">
            <PlayQueueVideoItem v-for="videoId in orderedVideos" :key="videoId" :video-id="videoId"
                                :current="currentVideoId === videoId"/>
        </draggable>
        <div v-show="hasMoreAfter" class="hidden-items-entry">...</div>
    </v-list>
</template>

<script>
    import PlayQueueVideoItem from "@/components/player/PlayQueueVideoItem";
    import draggable from 'vuedraggable';
    import { mapActions, mapGetters, mapMutations } from "vuex";

    const AMOUNT_OF_VIDEOS_TO_SHOW_AROUND_CURRENT = 20;

    export default {
        name: "PlayQueue",
        components: {
            PlayQueueVideoItem,
            draggable
        },
        computed: {
            ...mapGetters('player', ['videoIdsInQueue', 'currentVideoId']),
            videoDisplayIndex() {
                return this.videoIdsInQueue.indexOf(this.currentVideoId);
            },
            startDisplayIndex() {
                return Math.max(0, this.videoDisplayIndex - AMOUNT_OF_VIDEOS_TO_SHOW_AROUND_CURRENT);
            },
            endDisplayIndex() {
                return Math.min(this.videoIdsInQueue.length - 1, this.videoDisplayIndex + AMOUNT_OF_VIDEOS_TO_SHOW_AROUND_CURRENT);
            },
            hasMoreBefore() {
                return this.startDisplayIndex > 0;
            },
            hasMoreAfter() {
                return this.endDisplayIndex < this.videoIdsInQueue.length - 1;
            },
            orderedVideos: {
                get() {
                    return this.videoIdsInQueue.slice(this.startDisplayIndex, this.endDisplayIndex + 1);
                },
                set(value) {
                    this.updateOrderInSlice(value);
                }
            }
        },
        methods: {
            ...mapActions('player', ['skipToVideo', 'removeVideo']),
            ...mapActions('videos', ['loadVideos']),
            ...mapMutations('player', ['updateOrder']),
            updateOrderInSlice(value) {
                const copy = [...this.videoIdsInQueue];
                copy.splice(this.startDisplayIndex, this.endDisplayIndex + 1, ...value);
                this.updateOrder(copy);
            },
            loadVideosOfCurrentWindow() {
                this.loadVideos(this.orderedVideos);
            }
        },
        watch: {
            orderedVideos(newValue) {
                this.loadVideos(newValue);
            }
        },
        mounted() {
            this.loadVideosOfCurrentWindow();
        }
    }
</script>

<style scoped>
    .v-list-item__title {
        padding-left: 10px
    }

    .hidden-items-entry {
        height: 30px;
        width: 100%;
        text-align: center;
        font-size: 3em;
        transform: translateY(-30px);
    }
</style>
