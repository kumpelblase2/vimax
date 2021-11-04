<template>
    <v-dialog :value="shouldShowVideoInfo" @click:outside="close" v-if="shouldShowVideoInfo" max-width="75%">
        <v-card>
            <v-card-title>
                <span class="headline">{{video.name}}</span>
                <v-spacer />
                <v-btn text icon color="primary" @click="startEditing">
                    <v-icon>edit</v-icon>
                </v-btn>
            </v-card-title>
            <v-card-text>
                <v-row>
                    <v-col lg="6" cols="12">
                        <v-row>
                            <v-img :src="thumbnailUrl" :aspect-ratio="16/10" />
                        </v-row>
                        <v-row>
                            <v-img class="ma-5" :src="thumbUrl" :aspect-ratio="16/10"
                                   v-for="thumbUrl in nonThumbnailUrls" :key="thumbUrl" />
                        </v-row>
                    </v-col>
                    <v-col lg="6" cols="12">
                        <VideoMetadataDisplay :video-metadata="video.metadata" all />
                    </v-col>
                </v-row>
            </v-card-text>
            <v-divider></v-divider>
            <v-card-actions>
                <v-spacer/>
                <v-btn @click="close">Close</v-btn>
            </v-card-actions>
        </v-card>
    </v-dialog>
</template>

<script>
import { mapActions, mapGetters, mapMutations } from "vuex";
import { getSelectedThumbnailURLForVideo, getThumbnailURL } from "@/video";
import VideoMetadataDisplay from "@/components/video/VideoMetadataDisplay";

export default {
    name: "VideoInfoDialog",
    components: { VideoMetadataDisplay },
    computed: {
        ...mapGetters('videos', ["getVideo", "shouldShowVideoInfo", "videoInfoId"]),
        video() {
            return this.getVideo(this.videoInfoId);
        },
        thumbnailUrl() {
            return getSelectedThumbnailURLForVideo(this.video);
        },
        nonThumbnailUrls() {
            const thumbnails = [...this.video.thumbnails] || [];
            thumbnails.splice(this.video.selectedThumbnail, 1);
            return thumbnails.map(getThumbnailURL);
        }
    },
    methods: {
        ...mapMutations('videos', ["displayVideo"]),
        ...mapActions('videos/editing', ["editVideo"]),
        close() {
            this.displayVideo(null);
        },
        startEditing() {
            const videoId = this.videoInfoId;
            this.displayVideo(null);
            this.editVideo(videoId);
        }
    }
}
</script>
