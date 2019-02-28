<template>
  <v-flex xs4>
    <v-card>
      <v-img height="200px"
             v-bind:src="thumbnailUrl"></v-img>
      <v-card-title>{{ video.name }}</v-card-title>
    </v-card>
  </v-flex>
</template>

<script>
  export default {
    name: "VideoCard",
    props: ['video'],
    computed: {
      thumbnailUrl() {
        return `/api/video/${this.video.id}/thumbnail/${this.video.thumbnails[this.video.selectedThumbnail].id}`;
      }
    }
  }
</script>

<style scoped>
</style>
