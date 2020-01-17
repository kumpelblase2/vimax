<template>
    <v-app-bar app fixed clipped-left v-if="selectedVideoIds.length === 0">
        <v-btn icon @click="goBack" v-show="shouldShowBack"><v-icon>arrow_back</v-icon></v-btn>
        <v-app-bar-nav-icon @click.stop="$emit('drawer', !drawer)"/>
        <v-toolbar-title>Vima</v-toolbar-title>
        <v-spacer v-show="shouldShowMetadata"/>
        <MetadataSelection v-show="shouldShowMetadata"/>
        <v-spacer v-show="shouldShowSort"/>
        <SortSelect v-show="shouldShowSort"/>
        <SortDirectionToggle v-show="shouldShowSort"/>
        <v-spacer/>
        <v-row v-show="shouldShowSearch" align-center style="max-width: 650px">
            <SearchComponent/>
        </v-row>
    </v-app-bar>
    <v-app-bar app fixed clipped-left v-else>
        <v-btn icon @click="clearSelectedVideos"><v-icon>close</v-icon></v-btn>
        <v-toolbar-title>{{selectedVideoIds.length}} Video(s) selected</v-toolbar-title>
        <v-spacer/>
        <v-btn @click="selectAllVideos" text color="primary">
            <v-icon>check</v-icon>
            Select All
        </v-btn>
        <v-btn @click="clearSelectedVideos" text>
            <v-icon>close</v-icon>
            Deselect All
        </v-btn>
        <v-spacer/>
        <v-btn @click="playSelected" icon>
            <v-icon>play_arrow</v-icon>
        </v-btn>
        <v-btn @click="editSelectedVideos" icon>
            <v-icon>edit</v-icon>
        </v-btn>
        <AddToPlaylistButton/>
    </v-app-bar>
</template>

<script>
    import SortDirectionToggle from "./metadata/SortDirectionToggle";
    import SortSelect from "./metadata/SortSelect";
    import MetadataSelection from "./metadata/MetadataSelection";
    import SearchComponent from "./video/SearchComponent";
    import AddToPlaylistButton from "./playlist/AddToPlaylistButton";
    import { mapActions, mapGetters, mapMutations, mapState } from "vuex";

    export default {
        name: "TopBar",
        props: ['drawer'],
        components: { SortDirectionToggle, SortSelect, MetadataSelection, SearchComponent, AddToPlaylistButton },
        computed: {
            ...mapGetters('page', [
                'shouldShowMetadata',
                'shouldShowSearch',
                'shouldShowSort',
                'shouldShowPlaylistAdd',
                'shouldShowBack'
            ]),
            ...mapState('videos', ['selectedVideoIds'])
        },
        methods: {
            ...mapMutations('videos', ['clearSelectedVideos', 'selectAllVideos']),
            ...mapActions('videos/editing', ['editSelectedVideos']),
            ...mapActions('player', ['playVideos']),
            playSelected() {
                this.playVideos(this.selectedVideoIds);
                this.$router.push({ path: '/watch' });
                this.clearSelectedVideos();
            },
            goBack() {
                this.$router.go(-1);
            }
        }
    }
</script>

<style scoped>

</style>
