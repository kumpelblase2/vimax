<template>
    <v-dialog :value="shouldShowVideoInfo" @click:outside="close" v-if="shouldShowVideoInfo">
        <v-card>
            <v-card-title>
                <span class="headline">{{video.name}}</span>
                <v-spacer />
                <v-btn text icon color="orange" @click="startEditing">
                    <v-icon>edit</v-icon>
                </v-btn>
            </v-card-title>
            <v-card-text>
                <v-row>
                    <v-col md="6">
                        <v-img :src="thumbnailUrl" />
                    </v-col>
                    <v-col md="6">
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
import { getSelectedThumbnailURLForVideo } from "@/video";
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
