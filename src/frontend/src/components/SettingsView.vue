<template>
    <div class="scroll-container">
        <v-flex column>
            <library-list></library-list>
            <metadata-list></metadata-list>
            <plugin-list></plugin-list>
        </v-flex>
    </div>
</template>

<script>
    import LibraryList from "./LibraryList";
    import MetadataList from "./metadata/MetadataList";
    import PluginList from "./PluginList";

    export default {
        name: "SettingsView",
        components: { PluginList, MetadataList, LibraryList }
    }
</script>

<style scoped>

</style>
