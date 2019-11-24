<template>
    <v-flex column>
        <v-card>
            <div>
                <v-toolbar flat>
                    <v-toolbar-title>Playlists</v-toolbar-title>
                    <v-progress-circular v-if="loading" indeterminate width="3"></v-progress-circular>
                    <v-spacer></v-spacer>
                    <v-text-field style="max-width: 300px" v-model="newPlaylistName"></v-text-field>
                    <v-btn color="primary" @click="savePlaylist" :disabled="newPlaylistName.length == 0">Create</v-btn>
                </v-toolbar>
                <v-data-table :headers="playlistHeaders" :items="playlists" class="elevation-1" items-per-page="20">
                    <template slot="items" slot-scope="props">
                        <td>{{ props.item.id }}</td>
                        <td>{{ props.item.name }}</td>
                        <td class="justify-center">
                            <v-icon small @click="playPlaylist(props.item)">play_arrow</v-icon>
                            <v-icon small @click="deletePlaylist(props.item)">delete</v-icon>
                        </td>
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
        name: "PlaylistView",
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
                    { text: 'Actions', value: 'actions', sortable: false, width: 150 },
                ];
            }
        },
        methods: {
            ...mapActions('playlist', ['createPlaylist', 'removePlaylist']),
            savePlaylist() {
                this.createPlaylist({name: this.newPlaylistName, videos: []});
                this.newPlaylistName = "";
            },
            deletePlaylist(playlist) {
                this.removePlaylist(playlist.id);
            },
            playPlaylist(playlist) {

            }
        }
    }
</script>

<style scoped>

</style>
