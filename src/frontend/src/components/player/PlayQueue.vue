<template>
    <v-list>
        <draggable handle=".drag-item" v-model="orderedVideos">
            <v-list-item v-for="video in videosInQueue" :key="video.id">
                <v-list-item-action>
                    <v-icon class="drag-item">reorder</v-icon>
                </v-list-item-action>
                <v-list-item-avatar width="90" height="50" tile>
                    <v-img :src="_thumbnailForVideo(video)"></v-img>
                </v-list-item-avatar>
                <v-list-item-content>
                    <v-list-item-title>
                        <v-icon v-if="currentVideo && currentVideo.id === video.id">play_arrow</v-icon>
                        {{video.name}}
                    </v-list-item-title>
                </v-list-item-content>
                <v-list-item-action>
                    <v-flex column>
                        <v-btn @click="skipToVideo(video.id)" icon>
                            <v-icon>play_arrow</v-icon>
                        </v-btn>
                        <v-btn @click="removeVideo(video.id)" icon>
                            <v-icon>delete</v-icon>
                        </v-btn>
                    </v-flex>
                </v-list-item-action>
            </v-list-item>
        </draggable>
    </v-list>
</template>

<script>
    import draggable from 'vuedraggable';
    import { mapActions, mapGetters, mapMutations } from "vuex";
    import { getSelectedThumbnailURLForVideo } from "../../video";

    export default {
        name: "PlayQueue",
        components: {
            draggable
        },
        computed: {
            ...mapGetters('player', ['videosInQueue', 'currentVideo']),
            orderedVideos: {
                get() {
                    return this.videosInQueue;
                },
                set(value) {
                    this.updateOrder(value.map(v => v.id));
                }
            }
        },
        methods: {
            ...mapActions('player', ['skipToVideo', 'removeVideo']),
            ...mapMutations('player', ['updateOrder']),
            _thumbnailForVideo(video) {
                return getSelectedThumbnailURLForVideo(video);
            },
        }
    }
</script>

<style scoped>
    .v-list-item__title {
        padding-left: 10px
    }
</style>
