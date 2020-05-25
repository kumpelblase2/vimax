<template>
    <div v-if="hasVideo" class="player-container" :class="{'expanded': !collapsed}">
        <div :class="{ 'video-collapsed': collapsed, 'video-expanded': !collapsed }">
            <div v-if="!collapsed" class="top-video-bar">
                <v-row no-gutters justify="space-between" align="center" class="fill-height" style="padding-left: 10px;
            padding-right: 10px;">
                    <v-btn icon @click="collapse"><v-icon>keyboard_arrow_down</v-icon></v-btn>
                    <v-btn icon @click="fullscreen"><v-icon>fullscreen</v-icon></v-btn>
                </v-row>
            </div>
            <div v-if="!collapsed && showPlaylist" class="playlist-view">
                <h1>Play Queue</h1>
                <v-list>
                    <draggable handle=".drag-item" v-model="orderedVideos">
                        <v-list-item v-for="video in videosInQueue" :key="video.id">
                            <v-list-item-action>
                                <v-icon class="drag-item">reorder</v-icon>
                            </v-list-item-action>
                            <v-list-item-avatar width="90" height="50" tile>
                                <v-img :src="_thumbnailForVideo(video)"></v-img>
                            </v-list-item-avatar>
                            <v-list-item-content>
                                <v-list-item-title>
                                    <v-icon v-if="currentVideo && currentVideo.id === video.id">play_arrow</v-icon>
                                    {{video.name}}
                                </v-list-item-title>
                            </v-list-item-content>
                            <v-list-item-action>
                                <v-flex column>
                                    <v-btn @click="skipToVideo(video.id)" icon>
                                        <v-icon>play_arrow</v-icon>
                                    </v-btn>
                                    <v-btn @click="removeVideo(video.id)" icon>
                                        <v-icon>delete</v-icon>
                                    </v-btn>
                                </v-flex>
                            </v-list-item-action>
                        </v-list-item>
                    </draggable>
                </v-list>
            </div>
            <video :src="videoUrl" ref="videoPlayer" @play="playing = true" @pause="playing = false" width="100%"
                   height="100%" autoplay @ended="onVideoFinished"
                   @loadedmetadata="onVideoLoaded" @timeupdate="refreshCurrentTime" @click="playPauseVideo"
                   preload="auto"></video>
        </div>
        <div class="controller-bar" :class="{'expanded': !collapsed}">
            <v-hover v-slot:default="{ hover }">
                <div @mousemove="correctThumbnailPosition">
                    <v-progress-linear :value="seekProgress" @change="scrubb" rounded
                                       :height="hover ? 10 : 5"></v-progress-linear>
                    <div class="video-thumbnail" v-show="hover" ref="thumbnail">
                        <video width="100%" height="100%" :src="videoUrl" ref="thumbnailPlayer"
                               onloadstart="this.volume = 0" preload="auto"></video>
                    </div>
                </div>
            </v-hover>
            <v-row style="margin: 0 !important;">
                <div style="padding: 10px; width: 38%; height: 100px;">
                    <v-hover v-slot:default="{ hover }" v-show="collapsed">
                        <div class="video-hover" :class="{ 'hovered': !!hover }">
                            <v-btn large v-if="!!hover" style="margin-top: 20px; margin-left: 50px" icon @click="expand">
                                <v-icon>keyboard_arrow_up</v-icon>
                            </v-btn>
                        </div>
                    </v-hover>
                    <div class="fill-height details" :class="{ 'collapsed': this.collapsed}">
                        <v-row no-gutters>{{currentVideo.name}}</v-row>
                        <v-row no-gutters>{{videoTimestamp}} / {{videoDuration}}</v-row>
                    </div>
                </div>
                <div style="padding: 10px; width: 24%; height: 100px">
                    <v-row justify="center" align="center" class="fill-height" no-gutters>
                        <v-btn icon v-if="hasPrevious" @click="previousVideo"><v-icon>skip_previous</v-icon></v-btn>
                        <v-btn icon large @click="playPauseVideo"><v-icon>{{playIcon}}</v-icon></v-btn>
                        <v-btn icon v-if="hasNext" @click="nextVideo"><v-icon>skip_next</v-icon></v-btn>
                    </v-row>
                </div>
                <div style="padding: 10px; width: 38%; height: 100px">
                    <v-row justify="end" align="center" class="fill-height" no-gutters>
                        <v-btn icon @click="clear"><v-icon>close</v-icon></v-btn>
                        <v-btn icon large @click="togglePlaylist"><v-icon>playlist_play</v-icon></v-btn>
                        <v-icon dense>volume_up</v-icon>
                        <v-slider vertical hide-details style="max-width: 50px;" step="0.1" max="1" min="0" :value="volume"
                                  @input="updateVolume">
                            <v-icon>volume_up</v-icon>
                        </v-slider>
                    </v-row>
                </div>
            </v-row>
        </div>
    </div>
</template>

<script>
    import { mapActions, mapGetters, mapMutations } from "vuex";
    import draggable from 'vuedraggable';
    import { getSelectedThumbnailURLForVideo, getStreamURLForVideo } from "../video";
    import events from "../api/event";

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
        name: "PlayerBar",
        components: {
            draggable
        },
        data() {
            return {
                collapsed: true,
                currentTime: 0,
                duration: 0,
                playing: false,
                volume: 1,
                showPlaylist: false
            }
        },
        computed: {
            ...mapGetters('player', ['currentVideo', 'hasQueue', 'hasNext', 'hasPrevious', 'videosInQueue']),
            hasVideo() {
                return this.currentVideo != null;
            },
            playIcon() {
                return this.playing ? "pause" : "play_arrow";
            },
            videoUrl() {
                return getStreamURLForVideo(this.currentVideo);
            },
            videoDuration() {
                return formatTime(this.duration);
            },
            videoTimestamp() {
                return formatTime(this.currentTime);
            },
            seekProgress() {
                return (100 / this.duration) * this.currentTime;
            },
            orderedVideos: {
                get() {
                    return this.videosInQueue;
                },
                set(value) {
                    this.updateOrder(value.map(v => v.id));
                }
            }
        },
        watch: {
            hasVideo(newValue, oldValue) {
                if(newValue && !oldValue) {
                    this.collapsed = false;
                    this.showPlaylist = false;
                }
            }
        },
        methods: {
            ...mapActions('player', ['clear', 'nextVideo', 'previousVideo', 'skipToVideo', 'removeVideo']),
            ...mapMutations('player', ['updateOrder']),
            playPauseVideo() {
                if(this.playing) {
                    this.$refs.videoPlayer.pause();
                } else {
                    this.$refs.videoPlayer.play();
                }
            },
            onVideoLoaded() {
                this.duration = this.$refs.videoPlayer.duration;
            },
            refreshCurrentTime() {
                if(this.$refs.videoPlayer) { // it may just have been disposed
                    this.currentTime = this.$refs.videoPlayer.currentTime;
                }
            },
            updateVolume(amount) {
                this.$refs.videoPlayer.volume = amount;
            },
            expand() {
                this.collapsed = false;
            },
            collapse() {
                this.collapsed = true;
            },
            fullscreen() {
                this.$refs.videoPlayer.requestFullscreen();
            },
            togglePlaylist() {
                this.showPlaylist = !this.showPlaylist;
                if(this.showPlaylist) {
                    this.collapsed = false;
                }
            },
            _thumbnailForVideo(video) {
                return getSelectedThumbnailURLForVideo(video);
            },
            onVideoFinished() {
                events.watchFinishEvent(this.currentVideo.id).then(() => this.reloadVideo(this.currentVideo.id));
                this.nextVideo();
            },
            scrubb(value) {
                this.$refs.videoPlayer.currentTime = (value / 100) * this.duration;
            },
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

<style>
    .player-container {
        position: absolute;
        bottom: 0;
        height: 110px;
        width: 100vw;
        left: 0;
        z-index: 1;
    }

    .player-container.expanded {
        position: absolute;
        top: 0;
        left: 0;
        z-index: 10;
        height: 100vh;
    }

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

    .controller-bar .v-slider--vertical {
        min-height: 60px !important;
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

    .video-collapsed {
        position: absolute;
        bottom: 5px;
        left: 10px;
        width: 140px;
        height: 90px;
        z-index: 10;
    }

    .video-expanded {
        position: absolute;
        top: 0;
        left: 0;
        width: 100vw;
        height: 100vh;
        background-color: black;
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

    .top-video-bar {
        position: absolute;
        width: 100%;
        height: 50px;
        background-color: rgba(60, 60, 60, 0.7);
    }

    .playlist-view {
        width: 100%;
        height: 100%;
        background-color: #323335;
        padding: 75px 100px;
    }

    .video-thumbnail {
        width: 140px;
        height: 90px;
        position: absolute;
        top: -100px;
        background-color: black;
        left: 0;
    }
</style>
