<template>
  <v-container fluid>
    <v-layout row wrap>
      <VideoCard v-for="video in videos" :key="video.id" :video="video"></VideoCard>
    </v-layout>
  </v-container>
</template>

<script>
  import VideoCard from "./VideoCard";

  export default {
    name: "Index",
    components: { VideoCard },
    mounted() {
      this.$store.dispatch('loadRecentVideos');
    },
    computed: {
      videos() {
        return this.$store.state.videos;
      }
    }
  }
</script>

<style scoped>

</style>
