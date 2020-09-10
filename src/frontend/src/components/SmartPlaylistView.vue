<template>
    <div>
        <v-card>
            <v-card-title>
                {{ (playlist || {}).name }}
                <v-spacer></v-spacer>
                <v-icon @click="startPlaylist">play_arrow</v-icon>
            </v-card-title>
            <v-card-text class="scroll-container" style="height: 85vh">
                <v-list>
                    <v-list-item v-for="[id, video] in videos" :key="id">
                        <v-list-item-avatar horizontal height="100" width="150" class="mr-3">
                            <v-img :src="getThumbnailFor(video)"></v-img>
                        </v-list-item-avatar>
                        <v-list-item-content>
                            {{ (video || {}).name }}
                        </v-list-item-content>
                        <v-list-item-action>
                            <v-row>
                                <v-icon @click="displayVideo(id)" class="mr-1">info</v-icon>
                            </v-row>
                        </v-list-item-action>
                    </v-list-item>
                    <v-list-item v-if="hasMore">
                        <v-list-item-content>More videos</v-list-item-content>
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
        }
    },
    methods: {
        ...mapActions('videos', ['loadVideos']),
        ...mapActions('playlist', ['loadSmartPlaylists']),
        ...mapMutations('videos', ["displayVideo"]),
        ...mapActions('player', ['playVideos']),
        getThumbnailFor(video) {
            return getSelectedThumbnailURLForVideo(video);
        },
        startPlaylist() {
            this.playVideos(this.videoIds);
        }
    }
}
</script>

<style scoped>

</style>
