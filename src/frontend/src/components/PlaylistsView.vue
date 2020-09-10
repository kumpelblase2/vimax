<template>
    <v-flex column>
        <v-card>
            <div>
                <v-toolbar flat>
                    <v-toolbar-title>Playlists</v-toolbar-title>
                    <v-progress-circular v-if="loading" indeterminate width="3"></v-progress-circular>
                    <v-spacer></v-spacer>
                    <v-text-field style="max-width: 300px;" v-model="newPlaylistName" class="mr-5"
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
                                <v-icon :disabled="props.item.videoIds.length === 0" @click="playPlaylist(props.item)">
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
        <v-card>
            <div>
                <v-toolbar flat>
                    <v-toolbar-title>Smart Playlists</v-toolbar-title>
                    <v-progress-circular v-if="loading" indeterminate width="3"></v-progress-circular>
                    <v-spacer></v-spacer>
                    <SmartPlaylistCreateDialog @save="createSmartPlaylist"/>
                    <SmartPlaylistEditDialog :editing="editingSmartPlaylist" @save="updateSmartPlaylist"
                                             @cancel="stopEditingSmartPlaylist"/>
                </v-toolbar>
                <v-data-table :headers="smartPlaylistHeader" :items="smartPlaylists" class="elevation-1" :items-per-page="20">
                    <template slot="item" slot-scope="props">
                        <tr>
                            <td>{{ props.item.id }}</td>
                            <td>
                                <router-link :to="smartPlaylistRoute(props.item)">{{ props.item.name }}</router-link>
                            </td>
                            <td>{{ props.item.query }}</td>
                            <td>{{ props.item.orderBy }}</td>
                            <td>{{ props.item.orderDirection }}</td>
                            <td class="justify-center">
                                <v-icon @click="playSmartPlaylist(props.item)">play_arrow</v-icon>
                                <v-icon @click="editSmartPlaylist(props.item)">edit</v-icon>
                                <v-icon @click="deleteSmartPlaylist(props.item)">delete</v-icon>
                            </td>
                        </tr>
                    </template>
                    <template slot="no-data">
                        No Smart Playlists created yet.
                    </template>
                </v-data-table>
            </div>
        </v-card>
    </v-flex>
</template>

<script>
    import smartPlaylists from "@/api/smart-playlists";
    import SmartPlaylistCreateDialog from "@/components/smart-playlist/SmartPlaylistCreateDialog";
    import SmartPlaylistEditDialog from "@/components/smart-playlist/SmartPlaylistEditDialog";
    import { mapActions, mapState } from "vuex";

    export default {
        name: "PlaylistsView",
        components: { SmartPlaylistEditDialog, SmartPlaylistCreateDialog },
        data: () => ({
            loading: false,
            newPlaylistName: "",
            editingSmartPlaylist: null
        }),
        computed: {
            ...mapState('playlist', ['playlists', 'smartPlaylists']),
            playlistHeaders() {
                return [
                    { text: '#', value: 'id', width: 100 },
                    { text: 'Name', value: 'name' },
                    { text: 'Actions', value: 'actions', sortable: false, width: 150 }
                ];
            },
            smartPlaylistHeader() {
                return [
                    { text: '#', value: 'id', width: 100 },
                    { text: 'Name', value: 'name' },
                    { text: 'Query', value: 'query' },
                    { text: 'Sort By', value: 'orderBy' },
                    { text: 'Direction', value: 'orderDirection', width: 100 },
                    { text: 'Actions', value: 'actions', sortable: false, width: 150 }
                ];
            }
        },
        mounted() {
            this.loadPlaylists();
            this.loadSmartPlaylists();
        },
        methods: {
            ...mapActions('playlist', ['createPlaylist', 'createSmartPlaylist', 'removePlaylist', 'removeSmartPlaylist', 'loadPlaylists', 'loadSmartPlaylists', 'updateSmartPlaylist']),
            ...mapActions('player', ['playPlaylist', 'playVideos']),
            savePlaylist() {
                this.createPlaylist({ name: this.newPlaylistName, videoIds: [] });
                this.newPlaylistName = "";
            },
            playlistRoute(playlist) {
                return "/playlist/" + playlist.id;
            },
            smartPlaylistRoute(smartPlaylist) {
                return "/smart-playlist/" + smartPlaylist.id;
            },
            deletePlaylist(playlist) {
                this.removePlaylist(playlist.id);
            },
            editSmartPlaylist(playlist) {
                this.editingSmartPlaylist = playlist;
            },
            stopEditingSmartPlaylist() {
                this.editingSmartPlaylist = null;
            },
            deleteSmartPlaylist(playlist) {
                this.removeSmartPlaylist(playlist.id);
            },
            async playSmartPlaylist(playlist) {
                const videoIds = await smartPlaylists.getVideosOf(playlist.id);
                await this.playVideos(videoIds);
            }
        }
    }
</script>

<style scoped>

</style>
