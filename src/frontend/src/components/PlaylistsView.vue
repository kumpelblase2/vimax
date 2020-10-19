<template>
    <v-flex column class="scroll-container">
        <v-row wrap class="pa-3 ma-auto">
            <template v-for="playlist in allPlaylists">
                <v-flex column xs12 sm6 md4 lg3 xl2>
                    <v-card class="ma-3">
                        <v-img :src="collageUrl(playlist)"/>
                        <v-card-title>
                            <router-link :to="isSmartPlaylist(playlist) ? smartPlaylistRoute(playlist) : playlistRoute(playlist)">
                                {{ playlist.name }}
                            </router-link>
                        </v-card-title>
                        <v-card-text>
                            <div class="text-truncate" :title="textDisplayFor(playlist)">
                                {{ textDisplayFor(playlist) }}
                            </div>
                        </v-card-text>
                        <v-card-actions>
                            <v-btn icon @click="handlePlay(playlist)">
                                <v-icon>play_arrow</v-icon>
                            </v-btn>
                            <v-spacer/>
                            <v-btn icon @click="handleEdit(playlist)">
                                <v-icon>edit</v-icon>
                            </v-btn>
                            <v-btn icon @click="handleDelete(playlist)">
                                <v-icon>delete</v-icon>
                            </v-btn>
                        </v-card-actions>
                    </v-card>
                </v-flex>
            </template>
            <v-flex column xs12 sm6 md4 lg3 xl2>
                <div class="d-flex column align-center justify-center add-playlist" @click="handleCreateClicked">
                    <v-icon>add</v-icon>
                </div>
            </v-flex>
        </v-row>
        <SmartPlaylistEditDialog :editing="editingSmartPlaylist" @save="saveSmartPlaylist"
                                 @cancel="editingSmartPlaylist = null"/>
        <PlaylistEditDialog :editing="editingNormalPlaylist" @save="saveNormalPlaylist" @cancel="editingNormalPlaylist = null"/>
        <GenericPlaylistCreateDialog :show="showCreateDialog" @cancel="showCreateDialog = false"
                                     @create-smart-playlist="handleCreateSmartPlaylist"
                                     @create-normal-playlist="handleCreateNormalPlaylist"/>
    </v-flex>
</template>

<script>
    import smartPlaylists from "@/api/smart-playlists";
    import GenericPlaylistCreateDialog from "@/components/GenericPlaylistCreateDialog";
    import PlaylistEditDialog from "@/components/playlist/PlaylistEditDialog";
    import SmartPlaylistEditDialog from "@/components/playlist/SmartPlaylistEditDialog";
    import { mapActions, mapState } from "vuex";

    export default {
        name: "PlaylistsView",
        components: { PlaylistEditDialog, GenericPlaylistCreateDialog, SmartPlaylistEditDialog },
        data: () => ({
            loading: false,
            newPlaylistName: "",
            editingSmartPlaylist: null,
            editingNormalPlaylist: null,
            showCreateDialog: false
        }),
        computed: {
            ...mapState('playlist', ['playlists', 'smartPlaylists']),
            allPlaylists() {
                return [...this.playlists, ...this.smartPlaylists].sort((a, b) => a.name.localeCompare(b.name));
            }
        },
        mounted() {
            this.loadPlaylists();
            this.loadSmartPlaylists();
        },
        methods: {
            ...mapActions('playlist', ['createPlaylist', 'createSmartPlaylist', 'removePlaylist', 'removeSmartPlaylist',
                'loadPlaylists', 'loadSmartPlaylists', 'updateSmartPlaylist', 'updatePlaylistName']),
            ...mapActions('player', ['playPlaylist', 'playVideos']),
            async saveSmartPlaylist(playlist) {
                await this.updateSmartPlaylist(playlist);
                this.editingSmartPlaylist = null;
            },
            async saveNormalPlaylist(name) {
                await this.updatePlaylistName({ id: this.editingNormalPlaylist.id, name });
                this.editingNormalPlaylist = null;
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
            editNormalPlaylist(playlist) {
                this.editingNormalPlaylist = playlist;
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
            },
            isSmartPlaylist(playlist) {
                return playlist.query !== undefined;
            },
            collageUrl(playlist) {
                if(this.isSmartPlaylist(playlist)) {
                    return `/api/smart-playlists/${playlist.id}/poster`;
                } else {
                    return `/api/playlists/${playlist.id}/poster`;
                }
            },
            handlePlay(playlist) {
                if(this.isSmartPlaylist(playlist)) {
                    this.playSmartPlaylist(playlist);
                } else {
                    this.playPlaylist(playlist);
                }
            },
            handleEdit(playlist) {
                if(this.isSmartPlaylist(playlist)) {
                    this.editSmartPlaylist(playlist);
                } else {
                    this.editNormalPlaylist(playlist);
                }
            },
            handleDelete(playlist) {
                if(this.isSmartPlaylist(playlist)) {
                    this.deleteSmartPlaylist(playlist);
                } else {
                    this.deletePlaylist(playlist);
                }
            },
            handleCreateClicked() {
                this.showCreateDialog = true;
            },
            handleCreateSmartPlaylist(playlist) {
                this.createSmartPlaylist(playlist);
                this.showCreateDialog = false;
            },
            handleCreateNormalPlaylist(name) {
                this.createPlaylist({ name, videoIds: [] });
                this.showCreateDialog = false;
            },
            textDisplayFor(playlist) {
                return this.isSmartPlaylist(playlist) ? "Query: " + playlist.query : playlist.videoIds.length + " Videos";
            }
        }
    }
</script>

<style scoped>
    .add-playlist {
        margin: 10px;
        height: 350px;
        cursor: pointer;
        background-color: #1E1E1E;
        border-width: 2px;
        border-radius: 2%;
    }

    .add-playlist:hover {
        border-color: orange;
        border-style: inset;
    }
</style>
