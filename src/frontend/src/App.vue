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
                <v-list-tile to="/playlists">
                    <v-list-tile-action>
                        <v-icon>settings</v-icon>
                    </v-list-tile-action>
                    <v-list-tile-content>
                        <v-list-tile-title>Playlists</v-list-tile-title>
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
            <v-spacer v-if="shouldShowMetadata"></v-spacer>
            <MetadataSelection v-if="shouldShowMetadata"></MetadataSelection>
            <v-spacer v-if="shouldShowSort"></v-spacer>
            <SortSelect v-if="shouldShowSort"></SortSelect>
            <SortDirectionToggle v-if="shouldShowSort"></SortDirectionToggle>
            <v-spacer></v-spacer>
            <v-layout v-if="shouldShowSearch" row align-center style="max-width: 650px">
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
    import MetadataSelection from "./components/MetadataSelection";
    import { mapGetters } from "vuex";
    import SortSelect from "./components/SortSelect";
    import SortDirectionToggle from "./components/SortDirectionToggle";

    export default {
        name: 'Vima',
        components: { SortDirectionToggle, SortSelect, MetadataSelection, SearchComponent },
        computed: {
            ...mapGetters('page', [
                'shouldShowMetadata',
                'shouldShowSearch',
                'shouldShowSort'
            ])
        },
        data: () => ({
            drawer: null
        })
    }
</script>

<style>
</style>
