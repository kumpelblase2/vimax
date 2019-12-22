<template>
    <v-card>
        <v-card-title>
            {{playlist.name}}
            <v-spacer></v-spacer>
            <v-icon @click="startPlaylist">play_arrow</v-icon>
        </v-card-title>
        <v-card-text>
            <v-list>
                <draggable v-model="orderedVideos" handle=".drag-item">
                    <v-list-item v-for="video in videos" :key="video.id">
                        <v-list-item-action>
                            <v-icon class="drag-item">reorder</v-icon>
                        </v-list-item-action>
                        <v-list-item-avatar horizontal height="100" width="150" class="mr-3">
                            <v-img :src="getThumbnailFor(video)"></v-img>
                        </v-list-item-avatar>
                        <v-list-item-content>
                            {{video.name}}
                        </v-list-item-content>
                        <v-list-item-action>
                            <v-icon @click="deleteVideo(video.id)">delete</v-icon>
                        </v-list-item-action>
                    </v-list-item>
                </draggable>
            </v-list>
        </v-card-text>
    </v-card>
</template>

<script>
    import { mapActions, mapGetters } from "vuex";
    import draggable from 'vuedraggable';
    import { getSelectedThumbnailURLForVideo } from "../video";

    export default {
        name: "PlaylistView",
        components: {
            draggable
        },
        data() {
            return {
                playlistId: -1
            };
        },
        beforeMount() {
            this.playlistId = parseInt(this.$route.params.id);
        },
        async mounted() {
            await this.loadVideos(this.playlist.videoIds);
        },
        computed: {
            ...mapGetters('playlist', ['getPlaylist']),
            ...mapGetters('videos', ['getVideo']),
            playlist() {
                return this.getPlaylist(this.playlistId);
            },
            orderedVideos: {
                get() {
                    return this.videos;
                },
                set(value) {
                    const videoIds = value.map(video => video.id);
                    this.updateOrder({ playlistId: this.playlistId, videoIds });
                }
            },
            videos() {
                if(this.playlist != null) {
                    return this.playlist.videoIds.map(this.getVideo);
                } else {
                    return [];
                }
            }
        },
        methods: {
            ...mapActions('videos', ['loadVideos']),
            ...mapActions('playlist', ['updateOrder','removeFromPlaylist']),
            ...mapActions('player', ['playPlaylist']),
            deleteVideo(videoId) {
                this.removeFromPlaylist({playlistId: this.playlistId, videoIds: [videoId]});
            },
            getThumbnailFor(video) {
                return getSelectedThumbnailURLForVideo(video);
            },
            startPlaylist() {
                this.playPlaylist(this.playlist);
                this.$router.push({ path: `/watch` });
            }
        }
    }
</script>

<style scoped>
    .drag-item {
        cursor: pointer;
    }
</style>
