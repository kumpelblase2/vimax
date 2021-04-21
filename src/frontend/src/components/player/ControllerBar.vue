<template>
    <div class="controller-bar" :class="{'expanded': !collapsed}" @mouseenter="$emit('mouseenter', $event)"
         @mouseleave="$emit('mouseleave', $event)">
        <video-progress-bar :video-url="videoUrl" @scrubb="$emit('scrubb', $event)"
                            :seek-progress="seekProgress" :duration="duration"></video-progress-bar>
        <v-row class="ma-0 controller-bar-content">
            <div style="width: 38%;" class="pa-3">
                <v-hover v-slot:default="{ hover }" v-show="collapsed">
                    <div class="video-hover" :class="{ 'hovered': !!hover }">
                        <v-btn large v-if="!!hover" class="mt-5 mx-12" icon @click="$emit('expand')">
                            <v-icon>keyboard_arrow_up</v-icon>
                        </v-btn>
                    </div>
                </v-hover>
                <div class="fill-height details" :class="{ 'collapsed': this.collapsed}">
                    <v-row no-gutters>{{ (currentVideo || {}).name }}</v-row>
                    <v-row no-gutters>{{ videoTimestamp }} / {{ videoDuration }}</v-row>
                </div>
            </div>
            <div style="width: 24%;" class="pa-3">
                <v-row justify="center" align="center" class="fill-height" no-gutters>
                    <v-btn icon :disabled="!hasPrevious" @click="previousVideo">
                        <v-icon>skip_previous</v-icon>
                    </v-btn>
                    <v-btn icon large @click="$emit('playPauseVideo')">
                        <v-icon>{{ playIcon }}</v-icon>
                    </v-btn>
                    <v-btn icon :disabled="!hasNext" @click="nextVideo">
                        <v-icon>skip_next</v-icon>
                    </v-btn>
                </v-row>
            </div>
            <div style="width: 38%;" class="pa-3">
                <v-row justify="end" align="center" class="fill-height" no-gutters>
                    <v-btn icon @click="clear">
                        <v-icon>close</v-icon>
                    </v-btn>
                    <v-btn icon large @click="$emit('togglePlaylist')">
                        <v-icon>playlist_play</v-icon>
                    </v-btn>
                    <v-icon dense @click="toggleMuted">{{ volumeIcon }}</v-icon>
                    <v-slider :disabled="isMuted" vertical hide-details class="volume-slider"
                              step="0.1" max="1" min="0" :value="currentVolume" @input="updateVolume" height="80px">
                    </v-slider>
                </v-row>
            </div>
        </v-row>
    </div>
</template>

<script>
    import { getStreamURLForVideo } from "@/video";
    import { mapActions, mapGetters, mapMutations } from "vuex";
    import VideoProgressBar from "./VideoProgressBar";

    function padLeft(number) {
        const numberString = number.toString();
        if(numberString.length < 2) {
            return '0' + numberString;
        } else {
            return numberString;
        }
    }

    function formatTime(duration) {
        const secondsDisplay = Math.floor(duration % 60);
        const minutesDisplay = Math.floor((duration / 60) % 60);
        const hoursDisplay = Math.floor((duration / 60) / 60);
        if(hoursDisplay > 0) {
            return `${padLeft(hoursDisplay)}:${padLeft(minutesDisplay)}:${padLeft(secondsDisplay)}`;
        } else {
            return `${padLeft(minutesDisplay)}:${padLeft(secondsDisplay)}`;
        }
    }

    export default {
        name: "ControllerBar",
        props: ['collapsed', 'seekProgress', 'duration', 'currentTime', 'playing'],
        data() {
            return {
                previousVolume: 0
            }
        },
        components: {
            VideoProgressBar
        },
        computed: {
            ...mapGetters('player', ['currentVideo', 'hasNext', 'hasPrevious', 'currentVolume', 'isMuted']),
            videoUrl() {
                return getStreamURLForVideo(this.currentVideo);
            },
            videoDuration() {
                return formatTime(this.duration);
            },
            videoTimestamp() {
                return formatTime(this.currentTime);
            },
            playIcon() {
                return this.playing ? "pause" : "play_arrow";
            },
            volumeIcon() {
                return this.isMuted ? "volume_off" : "volume_up";
            }
        },
        methods: {
            ...mapActions('player', ['clear', 'nextVideo', 'previousVideo']),
            ...mapMutations('player', ['toggleMuted', 'updateVolume'])
        }
    }
</script>

<style scoped>
    .controller-bar {
        position: absolute;
        bottom: 0;
        width: 100%;
        height: 105px;
        background-color: #323335;
    }

    .controller-bar.expanded {
        background-color: rgba(50, 51, 53, 0.85);
    }

    .controller-bar-content {
        height: 100px;
    }

    .volume-slider {
        max-width: 50px;
        max-height: 100px;
    }

    .details {
        display: flex;
        flex-direction: column;
        justify-content: space-between;
        padding-left: 10px;
    }

    .details.collapsed {
        margin-left: 140px;
    }

    .video-hover {
        color: #323335;
        z-index: 11;
        width: 140px;
        height: 90px;
        position: absolute;
        left: 10px;
        bottom: 5px;
    }

    .video-hover.hovered {
        background-color: rgba(60, 60, 60, 0.6);
    }
</style>
