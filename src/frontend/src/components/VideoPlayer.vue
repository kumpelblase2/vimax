<template>
    <v-flex xs12 fill-height>
        <video :src="activeVideoStream" autoplay controls></video>
    </v-flex>
</template>

<script>
    import { mapGetters } from "vuex";

    export default {
        name: "VideoPlayer",
        computed: {
            ...mapGetters('videos', [
                'activeVideoStream'
            ])
        }
    }
</script>

<style scoped>
    video {
        width: 100%;
        max-height: 85vh;
        object-fit: fill;
    }
</style>
