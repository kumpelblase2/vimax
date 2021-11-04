<template>
    <div>
        <v-card>
            <v-card-title>
                {{ playlist.name }}
                <v-spacer></v-spacer>
                <v-btn icon @click="shufflePlay">
                    <v-icon>shuffle</v-icon>
                </v-btn>
                <v-btn icon @click="startPlaylist">
                    <v-icon>play_arrow</v-icon>
                </v-btn>
            </v-card-title>
            <v-card-text>
                <v-list>
                    <draggable v-model="orderedVideos" handle=".drag-item">
                        <v-list-item v-for="video in videos" :key="video.id">
                            <v-list-item-action>
                                <v-icon class="drag-item">reorder</v-icon>
                            </v-list-item-action>
                            <v-list-item-avatar horizontal height="100" width="150" class="mr-3" tile>
                                <v-img :src="getThumbnailFor(video)"></v-img>
                            </v-list-item-avatar>
                            <v-list-item-content>
                                {{ video.name }}
                            </v-list-item-content>
                            <v-list-item-action>
                                <v-row>
                                    <v-btn icon @click="startPlaylistAt(video.id)">
                                        <v-icon>play_arrow</v-icon>
                                    </v-btn>
                                    <v-btn icon @click="displayVideo(video.id)">
                                        <v-icon>info</v-icon>
                                    </v-btn>
                                    <v-btn icon @click="deleteVideo(video.id)">
                                        <v-icon>close</v-icon>
                                    </v-btn>
                                </v-row>
                            </v-list-item-action>
                        </v-list-item>
                    </draggable>
                </v-list>
            </v-card-text>
        </v-card>
        <video-info-dialog/>
    </div>
</template>

<script>
    import VideoInfoDialog from "@/components/video/VideoInfoDialog";
    import { getSelectedThumbnailURLForVideo } from "@/video";
    import draggable from 'vuedraggable';
    import { mapActions, mapGetters, mapMutations } from "vuex";

    export default {
        name: "PlaylistView",
        components: {
            VideoInfoDialog,
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
            ...mapActions('playlist', ['updateOrder', 'removeFromPlaylist']),
            ...mapActions('player', ['playPlaylist', 'playPlaylistRandom', 'skipToVideo']),
            ...mapMutations('videos', ["displayVideo"]),
            deleteVideo(videoId) {
                this.removeFromPlaylist({ playlistId: this.playlistId, videoIds: [videoId] });
            },
            getThumbnailFor(video) {
                return getSelectedThumbnailURLForVideo(video);
            },
            startPlaylist() {
                this.playPlaylist(this.playlist);
            },
            shufflePlay() {
                this.playPlaylistRandom(this.playlist);
            },
            async startPlaylistAt(id) {
                await this.startPlaylist();
                this.skipToVideo(id);
            }
        }
    }
</script>

<style scoped>
    .drag-item {
        cursor: pointer;
    }
</style>
