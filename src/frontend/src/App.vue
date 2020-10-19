<template>
    <v-app>
        <v-navigation-drawer v-model="drawer" clipped app temporary>
            <v-list dense>
                <v-list-item to="/">
                    <v-list-item-action>
                        <v-icon>video_library</v-icon>
                    </v-list-item-action>
                    <v-list-item-content>
                        <v-list-item-title>Videos</v-list-item-title>
                    </v-list-item-content>
                </v-list-item>
                <v-list-item to="/playlists">
                    <v-list-item-action>
                        <v-icon>playlist_play</v-icon>
                    </v-list-item-action>
                    <v-list-item-content>
                        <v-list-item-title>Playlists</v-list-item-title>
                    </v-list-item-content>
                </v-list-item>
                <v-list-item to="/sorting">
                    <v-list-item-action>
                        <v-icon>sort</v-icon>
                    </v-list-item-action>
                    <v-list-item-content>
                        <v-list-item-title>Sorting Mode</v-list-item-title>
                    </v-list-item-content>
                </v-list-item>
                <v-list-item to="/checkin">
                    <v-list-item-action>
                        <v-icon>meeting_room</v-icon>
                    </v-list-item-action>
                    <v-list-item-content>
                        <v-list-item-title>Checkin Mode</v-list-item-title>
                    </v-list-item-content>
                </v-list-item>
                <v-list-item to="/settings">
                    <v-list-item-action>
                        <v-icon>settings</v-icon>
                    </v-list-item-action>
                    <v-list-item-content>
                        <v-list-item-title>Settings</v-list-item-title>
                    </v-list-item-content>
                </v-list-item>
                <v-list-item @click="openAbout">
                    <v-list-item-action>
                        <v-icon>info</v-icon>
                    </v-list-item-action>
                    <v-list-item-content>
                        <v-list-item-title>About</v-list-item-title>
                    </v-list-item-content>
                </v-list-item>
            </v-list>
        </v-navigation-drawer>
        <top-bar :drawer="drawer" @drawer="drawer = $event"/>
        <v-content>
            <v-dialog v-model="about" max-width="700px">
                <about-dialog @close="closeAbout"/>
            </v-dialog>
            <router-view/>
        </v-content>
        <player-bar />
    </v-app>
</template>

<script>
    import TopBar from "./components/TopBar";
    import PlayerBar from "./components/player/PlayerBar";
    import AboutDialog from "./components/AboutDialog";

    export default {
        name: 'Vimax',
        components: { AboutDialog, TopBar, PlayerBar },
        data: () => ({
            drawer: null,
            about: false
        }),
        methods: {
            openAbout() {
                this.about = true;
            },
            closeAbout() {
                this.about = false;
            }
        }
    }
</script>

<style>
    html {
        overflow: hidden !important;
    }

    .v-content {
        max-height: 100vh;
    }

    .scroll-container {
        height: 100%;
        overflow-y: auto;
    }

    ::-webkit-scrollbar {
        width: 10px;
        height: 10px;
    }

    ::-webkit-scrollbar-track {
        background: rgb(18, 18, 18);
    }

    ::-webkit-scrollbar-thumb {
        background: #ff9800;
    }

    ::-webkit-scrollbar-thumb:hover {
        background: #7a5000;
    }
</style>
