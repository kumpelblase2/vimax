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
                        <v-icon>settings</v-icon>
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
        <v-app-bar app fixed clipped-left>
            <v-app-bar-nav-icon @click.stop="drawer = !drawer"></v-app-bar-nav-icon>
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
        </v-app-bar>
        <v-content>
            <v-container fluid>
                <router-view></router-view>
            </v-container>
        </v-content>
        <v-footer>
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
