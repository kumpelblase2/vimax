<template>
    <v-list-item>
        <v-list-item-action>
            <v-icon class="drag-item">reorder</v-icon>
        </v-list-item-action>
        <v-list-item-avatar width="90" height="50" tile>
            <v-img :src="thumbnail"></v-img>
        </v-list-item-avatar>
        <v-list-item-content>
            <v-list-item-title>
                <v-icon v-if="current">play_arrow</v-icon>
                {{ (video || {}).name }}
            </v-list-item-title>
        </v-list-item-content>
        <v-list-item-action>
            <v-flex column>
                <v-btn @click="skipToVideo(videoId)" icon>
                    <v-icon>play_arrow</v-icon>
                </v-btn>
                <v-btn @click="removeVideo(videoId)" icon>
                    <v-icon>delete</v-icon>
                </v-btn>
            </v-flex>
        </v-list-item-action>
    </v-list-item>
</template>

<script>
    import { getSelectedThumbnailURLForVideo } from "@/video";
    import { mapActions, mapGetters } from "vuex";

    export default {
        name: "PlayQueueVideoItem",
        props: {
            videoId: null,
            current: false
        },
        computed: {
            ...mapGetters('videos', ['getVideo']),
            video() {
                return this.getVideo(this.videoId);
            },
            thumbnail() {
                return getSelectedThumbnailURLForVideo(this.video);
            },
        },
        methods: {
            ...mapActions('player', ['skipToVideo', 'removeVideo'])
        }
    }
</script>

<style scoped>

</style>
