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
                <v-text-field
                    :append-icon-cb="() => {}"
                    placeholder="Search..."
                    single-line
                    append-icon="search"
                    color="white"
                    hide-details
                ></v-text-field>
            </v-layout>
        </v-toolbar>
        <v-content>
            <router-view></router-view>
        </v-content>
        <v-footer app></v-footer>
    </v-app>
</template>

<script>
    export default {
        name: 'Vima',
        data: () => ({
            drawer: null
        })
    }
</script>

<style>
</style>
