<template>
    <v-hover v-slot:default="{ hover }">
        <div @mousemove="correctThumbnailPosition">
            <v-progress-linear :value="seekProgress" @change="$emit('scrubb', $event)" rounded
                               :height="hover ? 10 : 5"></v-progress-linear>
            <div class="video-thumbnail" v-show="hover" ref="thumbnail">
                <video width="100%" height="100%" :src="videoUrl" ref="thumbnailPlayer"
                       onloadstart="this.volume = 0" preload="auto"></video>
            </div>
        </div>
    </v-hover>
</template>

<script>
    export default {
        name: "VideoProgressBar",
        props: ['videoUrl', 'seekProgress'],
        methods: {
            correctThumbnailPosition(ev) {
                const x = ev.clientX;
                const documentWidth = document.body.clientWidth;
                const leftOffset = 140 / 2; // Half of the 140 width
                const maxValue = documentWidth - 140;
                const left = Math.max(0, Math.min(maxValue, x - leftOffset));
                this.$refs.thumbnail.style.left = left + "px";

                const progress = x / documentWidth;
                this.$refs.thumbnailPlayer.currentTime = this.duration * progress;
            }
        }
    }
</script>

<style scoped>
    .video-thumbnail {
        width: 140px;
        height: 90px;
        position: absolute;
        top: -100px;
        background-color: black;
        left: 0;
    }
</style>
