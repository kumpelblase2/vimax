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
            </v-list>
        </v-navigation-drawer>
        <top-bar :drawer="drawer" @drawer="drawer = $event"/>
        <v-content>
            <div class="scroll-container">
                <router-view/>
            </div>
        </v-content>
        <player-bar />
        <v-footer>
            <v-spacer/>
            <div>Vima made with ❤️ by kumpelblase2</div>
            <v-spacer/>
        </v-footer>
    </v-app>
</template>

<script>
    import TopBar from "./components/TopBar";
    import PlayerBar from "./components/PlayerBar";

    export default {
        name: 'Vima',
        components: { TopBar, PlayerBar },
        data: () => ({
            drawer: null
        })
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
        overflow-y: scroll;
    }

</style>
