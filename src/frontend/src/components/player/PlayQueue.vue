<template>
    <v-list style="max-height: 90%" class="overflow-y-auto">
        <draggable handle=".drag-item" v-model="orderedVideos">
            <PlayQueueVideoItem v-for="videoId in orderedVideos" :key="videoId" :video-id="videoId"
                                :current="currentVideoId === videoId"/>
        </draggable>
    </v-list>
</template>

<script>
    import PlayQueueVideoItem from "@/components/player/PlayQueueVideoItem";
    import draggable from 'vuedraggable';
    import { mapActions, mapGetters, mapMutations } from "vuex";

    export default {
        name: "PlayQueue",
        components: {
            PlayQueueVideoItem,
            draggable
        },
        computed: {
            ...mapGetters('player', ['videoIdsInQueue', 'currentVideoId']),
            orderedVideos: {
                get() {
                    return this.videoIdsInQueue;
                },
                set(value) {
                    this.updateOrder(value);
                }
            }
        },
        methods: {
            ...mapActions('player', ['skipToVideo', 'removeVideo']),
            ...mapActions('videos', ['loadVideos']),
            ...mapMutations('player', ['updateOrder'])
        },
        mounted() {
            this.loadVideos(this.videoIdsInQueue);
        }
    }
</script>

<style scoped>
    .v-list-item__title {
        padding-left: 10px
    }
</style>
