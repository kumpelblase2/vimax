<template>
    <div class="fill-height">
        <v-card class="px-2" height="100%">
            <v-card-title>
                {{ (playlist || {}).name }}
                <span class="text--disabled ml-5">{{ videoIds.length }} Video(s)</span>
                <v-spacer></v-spacer>
                <v-btn icon @click="shufflePlaylist">
                    <v-icon>shuffle</v-icon>
                </v-btn>
                <v-btn icon @click="startPlaylist">
                    <v-icon>play_arrow</v-icon>
                </v-btn>
            </v-card-title>
            <v-card-text class="scroll-container" style="height: calc(100% - 75px)">
                <v-list class="pa-0">
                    <v-list-item v-for="[id, video] in videos" :key="id">
                        <v-list-item-avatar horizontal height="100" width="150" class="mr-3" tile>
                            <v-img :src="getThumbnailFor(video)"></v-img>
                        </v-list-item-avatar>
                        <v-list-item-content>
                            {{ (video || {}).name }}
                        </v-list-item-content>
                        <v-list-item-action>
                            <v-row>
                                <v-btn icon @click="startPlaylistAt(id)">
                                    <v-icon >play_arrow</v-icon>
                                </v-btn>

                                <v-btn icon @click="displayVideo(id)">
                                    <v-icon>info</v-icon>
                                </v-btn>
                            </v-row>
                        </v-list-item-action>
                    </v-list-item>
                    <v-list-item v-if="hasMore">
                        <v-list-item-content>
                            <span class="text-center grey--text">There are more ({{ remainingVideoCount }}) videos in this playlist.</span>
                        </v-list-item-content>
                    </v-list-item>
                </v-list>
            </v-card-text>
        </v-card>
        <video-info-dialog/>
    </div>
</template>

<script>
    import smartPlaylists from "@/api/smart-playlists";
    import VideoInfoDialog from "@/components/video/VideoInfoDialog";
    import { shuffle } from "@/helpers/array-helper";
    import { getSelectedThumbnailURLForVideo } from "@/video";
    import { mapActions, mapGetters, mapMutations } from "vuex";

    const LOAD_MAX = 100;

    export default {
        name: "SmartPlaylistView",
        components: { VideoInfoDialog },
        data() {
            return {
                playlistId: -1,
                videoIds: []
            }
        },
        beforeMount() {
            this.playlistId = parseInt(this.$route.params.id);
        },
        async mounted() {
            await this.loadSmartPlaylists();
            this.videoIds = await smartPlaylists.getVideosOf(this.playlistId);
            await this.loadVideos(this.displayedVideoIds);
        },
        computed: {
            ...mapGetters('videos', ['getVideo']),
            ...mapGetters('playlist', ['getSmartPlaylist']),
            hasMore() {
                return this.videoIds.length > LOAD_MAX;
            },
            videoCountToDisplay() {
                return Math.min(this.videoIds.length, LOAD_MAX);
            },
            displayedVideoIds() {
                return this.videoIds.slice(0, this.videoCountToDisplay);
            },
            videos() {
                return this.displayedVideoIds.map(id => [id, this.getVideo(id)]);
            },
            playlist() {
                return this.getSmartPlaylist(this.playlistId);
            },
            remainingVideoCount() {
                return this.videoIds.length - LOAD_MAX;
            }
        },
        methods: {
            ...mapActions('videos', ['loadVideos']),
            ...mapActions('playlist', ['loadSmartPlaylists']),
            ...mapMutations('videos', ["displayVideo"]),
            ...mapActions('player', ['playVideos', 'skipToVideo']),
            getThumbnailFor(video) {
                return getSelectedThumbnailURLForVideo(video);
            },
            async startPlaylist() {
                await this.playVideos(this.videoIds);
            },
            async shufflePlaylist() {
                await this.playVideos(shuffle(this.videoIds));
            },
            async startPlaylistAt(id) {
                await this.startPlaylist();
                this.skipToVideo(id);
            }
        }
    }
</script>

<style scoped>

</style>
