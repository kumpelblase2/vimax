<template>
    <v-dialog v-model="dialog" max-width="500px">
        <template v-slot:activator="{ on }">
            <v-btn text icon v-on="on">
                <v-icon>playlist_add</v-icon>
            </v-btn>
        </template>
        <v-card>
            <v-card-title>Select Playlist</v-card-title>
            <v-card-text>
                <v-list>
                    <v-list-item v-for="playlist in playlists" :key="playlist.id" @click="selectPlaylist(playlist)">
                        <v-list-item-content>
                            {{playlist.name}}
                        </v-list-item-content>
                    </v-list-item>
                </v-list>
            </v-card-text>
            <v-card-actions>
                <v-text-field single-line v-model="newPlaylistName" style="margin-right: 20px"
                              placeholder="Create new..."></v-text-field>
                <v-btn color="primary" @click="createPlaylistsWithSelection">Create</v-btn>
            </v-card-actions>
        </v-card>
    </v-dialog>
</template>

<script>
    import { mapActions, mapGetters, mapMutations, mapState } from "vuex";

    export default {
        name: "AddToPlaylistButton",
        data() {
            return {
                dialog: false,
                newPlaylistName: ""
            }
        },
        mounted() {
            this.loadPlaylists();
        },
        computed: {
            ...mapState('playlist', ['playlists']),
            ...mapGetters('videos', ['selectedVideos']),
            selectedVideoIds() {
                return this.selectedVideos.map(video => video.id);
            }
        },
        methods: {
            ...mapActions('playlist', ['loadPlaylists', 'createPlaylist', 'addToPlaylist']),
            ...mapMutations('videos', ['clearSelectedVideos']),
            createPlaylistsWithSelection() {
                this.createPlaylist({
                    name: this.newPlaylistName,
                    videoIds: this.selectedVideoIds
                }).then(this.close).then(this.clearSelectedVideos);
            },
            selectPlaylist(playlist) {
                this.addToPlaylist({
                    playlistId: playlist.id,
                    videoIds: this.selectedVideoIds
                }).then(this.close).then(this.clearSelectedVideos);
            },
            close() {
                this.newPlaylistName = "";
                this.dialog = false;
            }
        }
    }
</script>

<style scoped>

</style>
