<template>
  <v-flex column>
    <library-list></library-list>
    <metadata-list></metadata-list>
  </v-flex>
</template>

<script>
  import LibraryList from "./LibraryList";
  import MetadataList from "./MetadataList";

  export default {
    name: "SettingsView",
    components: { MetadataList, LibraryList }
  }
</script>

<style scoped>

</style>
