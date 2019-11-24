<template>
    <v-dialog v-model="open" scrollable max-width="500px">
        <v-card>
            <v-card-title>Select Playlist</v-card-title>
            <v-divider></v-divider>
            <v-card-text>
                <v-list>
                    <v-list-tile v-for="playlist in playlists" @click="select(playlist)">
                        <v-list-tile-content>{{ playlist.name }}</v-list-tile-content>
                    </v-list-tile>
                </v-list>
            </v-card-text>
            <v-card-actions>
                <v-text-field v-model="playlistName" placeholder="Create new..."></v-text-field>
                <v-btn text @click="createPlaylistWithVideos"></v-btn>
            </v-card-actions>
        </v-card>
    </v-dialog>
</template>

<script>
    import { mapActions, mapState } from "vuex";

    export default {
        name: "PlaylistSelectDialog",
        data() {
            return {
                playlistName: "",
                open: false
            }
        },
        computed: {
            ...mapState('playlist', ['playlists'])
        },
        methods: {
            ...mapActions('playlist', ['createPlaylist', 'addSelectedToPlaylist']),
            createPlaylistWithVideos() {
                this.createPlaylist({
                    name: this.playlistName,
                    videos: this.selectedVideos.map(video => video.id)
                }).then(this.close);
            },
            select(playlist) {
                this.addSelectedToPlaylist(playlist.id).then(this.close);
            },
            close() {
                this.open = false;
            }
        }
    }
</script>

<style scoped>

</style>
