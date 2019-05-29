<template>
    <v-app dark>
        <v-navigation-drawer v-model="drawer" clipped app temporary>
            <v-list dense>
                <v-list-tile to="/">
                    <v-list-tile-action>
                        <v-icon>video_library</v-icon>
                    </v-list-tile-action>
                    <v-list-tile-content>
                        <v-list-tile-title>Videos</v-list-tile-title>
                    </v-list-tile-content>
                </v-list-tile>
                <v-list-tile to="/sorting">
                    <v-list-tile-action>
                        <v-icon>sort</v-icon>
                    </v-list-tile-action>
                    <v-list-tile-content>
                        <v-list-tile-title>Sorting Mode</v-list-tile-title>
                    </v-list-tile-content>
                </v-list-tile>
                <v-list-tile to="/settings">
                    <v-list-tile-action>
                        <v-icon>settings</v-icon>
                    </v-list-tile-action>
                    <v-list-tile-content>
                        <v-list-tile-title>Settings</v-list-tile-title>
                    </v-list-tile-content>
                </v-list-tile>
            </v-list>
        </v-navigation-drawer>
        <v-toolbar app fixed clipped-left>
            <v-toolbar-side-icon @click.stop="drawer = !drawer"></v-toolbar-side-icon>
            <v-toolbar-title>Vima</v-toolbar-title>
            <v-spacer></v-spacer>
            <v-layout row align-center style="max-width: 650px">
                <SearchComponent></SearchComponent>
            </v-layout>
        </v-toolbar>
        <v-content>
            <router-view></router-view>
        </v-content>
        <v-footer app>
            <v-spacer></v-spacer>
            <div>Vima made with ❤️ by kumpelblase2</div>
            <v-spacer></v-spacer>
        </v-footer>
    </v-app>
</template>

<script>
    import SearchComponent from "./components/SearchComponent";
    export default {
        name: 'Vima',
        components: { SearchComponent },
        data: () => ({
            drawer: null
        })
    }
</script>

<style>
</style>
