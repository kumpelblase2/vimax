<template>
    <v-flex column>
        <v-card>
            <div>
                <v-toolbar flat>
                    <v-toolbar-title>Playlists</v-toolbar-title>
                    <v-progress-circular v-if="loading" indeterminate width="3"></v-progress-circular>
                    <v-spacer></v-spacer>
                    <v-text-field style="max-width: 300px; margin-right: 20px" v-model="newPlaylistName"
                                  hide-details></v-text-field>
                    <v-btn color="primary" @click="savePlaylist" :disabled="newPlaylistName.length === 0">Create</v-btn>
                </v-toolbar>
                <v-data-table :headers="playlistHeaders" :items="playlists" class="elevation-1" :items-per-page="20">
                    <template slot="item" slot-scope="props">
                        <tr>
                            <td>{{ props.item.id }}</td>
                            <td>
                                <router-link :to="playlistRoute(props.item)">{{ props.item.name }}</router-link>
                            </td>
                            <td class="justify-center">
                                <v-icon :disabled="props.item.videoIds.length === 0" @click="startPlaylist(props.item)">
                                    play_arrow
                                </v-icon>
                                <v-icon @click="deletePlaylist(props.item)">delete</v-icon>
                            </td>
                        </tr>
                    </template>
                    <template slot="no-data">
                        No Playlists created yet.
                    </template>
                </v-data-table>
            </div>
        </v-card>
    </v-flex>
</template>

<script>
    import { mapActions, mapState } from "vuex";

    export default {
        name: "PlaylistsView",
        data: () => ({
            loading: false,
            newPlaylistName: ""
        }),
        computed: {
            ...mapState('playlist', ['playlists']),
            playlistHeaders() {
                return [
                    { text: '#', value: 'id', width: 100 },
                    { text: 'Name', value: 'name' },
                    { text: 'Actions', value: 'actions', sortable: false, width: 150 }
                ];
            }
        },
        mounted() {
            this.loadPlaylists();
        },
        methods: {
            ...mapActions('playlist', ['createPlaylist', 'removePlaylist', 'loadPlaylists']),
            ...mapActions('player', ['playPlaylist']),
            savePlaylist() {
                this.createPlaylist({ name: this.newPlaylistName, videoIds: [] });
                this.newPlaylistName = "";
            },
            playlistRoute(playlist) {
                return "/playlist/" + playlist.id;
            },
            deletePlaylist(playlist) {
                this.removePlaylist(playlist.id);
            },
            startPlaylist(playlist) {
                this.playPlaylist(playlist);
                this.$router.push({ path: `/watch` });
            }
        }
    }
</script>

<style scoped>

</style>
