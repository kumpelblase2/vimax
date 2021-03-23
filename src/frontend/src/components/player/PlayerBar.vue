<template>
    <div v-if="hasVideoPlaying" class="player-container" :class="{'expanded': !collapsed}">
        <div :class="{ 'video-collapsed': collapsed, 'video-expanded': !collapsed }">
            <div v-if="!collapsed" class="top-video-bar" :class="{ 'faded': hasFaded }">
                <v-row no-gutters justify="space-between" align="center" class="fill-height px-3">
                    <v-btn icon @click="collapse">
                        <v-icon>keyboard_arrow_down</v-icon>
                    </v-btn>
                    <v-btn icon @click="fullscreen">
                        <v-icon>fullscreen</v-icon>
                    </v-btn>
                </v-row>
            </div>
            <div v-if="!collapsed && showPlaylist" class="playlist-view">
                <h1>Play Queue</h1>
                <play-queue/>
            </div>
            <video :src="videoUrl" ref="videoPlayer" @play="playing = true" @pause="playing = false" width="100%"
                   height="100%" autoplay @ended="onVideoFinished"
                   @loadedmetadata="onVideoLoaded" @timeupdate="refreshCurrentTime" @click="playPauseVideo"
                   preload="auto"></video>
        </div>
        <controller-bar :collapsed="collapsed" :playing="playing" :seek-progress="seekProgress" :duration="duration"
                        :current-time="currentTime" :volume="volume" @expand="expand" @togglePlaylist="togglePlaylist"
                        @playPauseVideo="playPauseVideo" @scrubb="scrubb" :class="{ 'faded': hasFaded }"/>
    </div>
</template>

<script>
    import { getStreamURLForVideo } from "@/video";
    import { mapActions, mapGetters, mapMutations } from "vuex";
    import events from "../../api/event";
    import ControllerBar from "./ControllerBar";
    import PlayQueue from "./PlayQueue";
    import VideoProgressBar from "./VideoProgressBar";

    export default {
        name: "PlayerBar",
        components: {
            ControllerBar,
            PlayQueue,
            VideoProgressBar
        },
        data() {
            return {
                collapsed: true,
                currentTime: 0,
                duration: 0,
                playing: false,
                volume: 1,
                showPlaylist: false,
                keyListener: null,
                mouseListener: null,
                fadeTimeout: null,
                hasFaded: false
            }
        },
        computed: {
            ...mapGetters('player', ['currentVideo', 'currentVideoId', 'hasQueue', 'hasNext', 'hasPrevious', 'videosInQueue', 'currentVolume', 'isMuted', 'hasVideoPlaying']),
            videoUrl() {
                if(this.currentVideo == null) {
                    return "";
                }
                return getStreamURLForVideo(this.currentVideo);
            },
            seekProgress() {
                return (100 / this.duration) * this.currentTime;
            },
            canFade() {
                return this.currentVideo != null && !this.collapsed && this.playing;
            }
        },
        watch: {
            hasVideoPlaying(newValue, oldValue) {
                if(newValue && !oldValue) {
                    this.collapsed = false;
                    this.showPlaylist = false;
                    setTimeout(() => {
                        this.$refs.videoPlayer.muted = this.isMuted;
                        this.$refs.videoPlayer.volume = this.currentVolume;
                    });
                } else if(!newValue && oldValue) {
                    this.$refs.videoPlayer.src = ""
                    this.$refs.videoPlayer.removeAttribute("src");
                }
            },
            currentVideoId(newValue) {
                if(newValue > 0) {
                    this.loadVideos([newValue]);
                }
            },
            isMuted(newValue) {
                this.$refs.videoPlayer.muted = newValue;
            },
            currentVolume(newValue) {
                this.$refs.videoPlayer.volume = newValue;
            },
            canFade(newValue) {
                if(newValue) {
                    this.resetFadeTimeout();
                } else {
                    clearTimeout(this.fadeTimeout);
                    this.fadeTimeout = null;
                }
            }
        },
        methods: {
            mountListener() {
                this.keyListener = event => {
                    if(this.hasVideo) {
                        if(event.key === " ") {
                            this.playPauseVideo();
                        } else if(event.key === "ArrowRight") {
                            this.scrubbSeconds(5);
                        } else if(event.key === "ArrowLeft") {
                            this.scrubbSeconds(-5);
                        } else if(event.key === "ArrowUp" && !this.isMuted) {
                            this.updateVolume(this.currentVolume + 0.1);
                        } else if(event.key === "ArrowDown" && !this.isMuted) {
                            this.updateVolume(this.currentVolume - 0.1);
                        }
                    }
                };

                this.mouseListener = () => {
                    this.hasFaded = false;
                    if(this.canFade) {
                        this.resetFadeTimeout();
                    }
                };

                document.addEventListener('keyup', this.keyListener);
                document.addEventListener('mousemove', this.mouseListener);
            },
            unmountListener() {
                document.removeEventListener('keyup', this.keyListener)
                document.removeEventListener('mousemove', this.mouseListener);
            },
            ...mapActions('player', ['clear', 'nextVideo', 'previousVideo']),
            ...mapMutations('player', ['updateVolume']),
            ...mapActions('videos', ['reloadVideo', 'loadVideos']),
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
            onVideoFinished() {
                events.watchFinishEvent(this.currentVideoId).then(() => this.reloadVideo(this.currentVideoId));
                if(this.hasNext) {
                    this.nextVideo();
                } else {
                    this.collapse();
                }
            },
            scrubb(value) {
                this.$refs.videoPlayer.currentTime = (value / 100) * this.duration;
            },
            scrubbSeconds(amount) {
                this.$refs.videoPlayer.currentTime += amount;
            },
            resetFadeTimeout() {
                if(this.fadeTimeout != null) {
                    clearTimeout(this.fadeTimeout);
                }

                this.fadeTimeout = setTimeout(() => {
                    this.hasFaded = true;
                }, 4000);
            }
        },
        mounted() {
            this.mountListener();
        },
        destroyed() {
            this.unmountListener();
        }
    }
</script>

<style>
    .player-container {
        position: absolute;
        top: calc(100vh - 110px);
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
</style>
