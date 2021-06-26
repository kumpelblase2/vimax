<template>
    <v-col class="fill-height">
        <v-row align="center" class="mb-2">
            <v-flex xs4 offset-4>
                <v-text-field label="Filtering Query" id="filter" :value="filter" @change="updateFilter"
                              solo hide-details></v-text-field>
            </v-flex>
            <v-btn class="ml-2" icon @click="refreshQueue">
                <v-icon>refresh</v-icon>
            </v-btn>
        </v-row>
        <v-row v-if="currentVideo" class="fill-height">
            <v-col class="pa-0" xs6 style="max-height: calc(100% - 60px)">
                <single-video-player :autoplay="false" :disable-events="true" :video="currentVideo"/>
            </v-col>
            <v-col xs6 class="fill-height" style="max-height: 100%">
                <v-row class="px-2">
                    <v-flex grow class="pa-1"><v-btn @click="skip" block>Skip</v-btn></v-flex>
                    <v-flex grow class="pa-1"><v-btn color="primary" block @click="next">Save and Next</v-btn></v-flex>
                </v-row>
                <v-card class="editing-card fill-height" max-height="calc(100% - 90px)">
                    <v-card-title>
                        {{ currentVideo.name }}
                        <v-spacer/>
                    </v-card-title>
                    <v-card-text>
                        <v-item-group mandatory @change="updateSelectedThumbnail" :value="currentVideo.selectedThumbnail">
                            <v-row wrap>
                                <v-col v-for="(thumbnail, index) in currentVideo.thumbnails" :key="index" cols="12" md="4">
                                    <v-item active-class="active" class="element">
                                        <v-img :src="thumbnailUrl(thumbnail)" :aspect-ratio="16/10"
                                               slot-scope="{ active, toggle }"
                                               @click="toggle"></v-img>
                                    </v-item>
                                </v-col>
                            </v-row>
                        </v-item-group>
                        <div v-for="metadata in editableMetadata" :key="metadata.id">
                            <metadata-value-editor :metadata-definition="metadata"
                                                   :metadata-value="getValueOf(metadata)"
                                                   @change="handleMetadataUpdate(metadata.id, $event)"></metadata-value-editor>
                        </div>
                    </v-card-text>
                </v-card>
            </v-col>
        </v-row>
        <v-row v-else-if="filter !== ''" justify="space-around">
            <div class="mt-4">
                No videos that need check-in.
            </div>
        </v-row>
    </v-col>
</template>

<script>
    import { mapActions, mapGetters, mapMutations } from "vuex";
    import SingleVideoPlayer from "./player/SingleVideoPlayer";
    import MetadataValueEditor from "./metadata/MetadataValueEditor";
    import { getThumbnailURL } from "@/video";

    export default {
        name: "CheckinView",
        computed: {
            ...mapGetters('checkin', ['filter', 'currentVideo']),
            ...mapGetters('settings/metadata', ['editableMetadata']),
        },
        components: {
            MetadataValueEditor,
            SingleVideoPlayer
        },
        methods: {
            ...mapMutations('checkin', ['setEditingMetadataValue','changeThumbnail']),
            ...mapActions('checkin', ['updateFilter','nextVideo', 'restartEditingIfPossible', 'saveAndContinue', 'refreshQueue']),
            ...mapActions('settings/metadata', ['loadMetadata']),
            handleMetadataUpdate(id, event) {
                this.setEditingMetadataValue({ id, value: event });
            },
            getValueOf(metadata) {
                const value = this.currentVideo.metadata[metadata.id].value;
                if(value != null) {
                    return value;
                } else {
                    return metadata.options.defaultValue;
                }
            },
            thumbnailUrl(thumbnail) {
                return getThumbnailURL(thumbnail);
            },
            updateSelectedThumbnail(thumbnailIndex) {
                this.changeThumbnail(thumbnailIndex);
            },
            async next() {
                await this.$nextTick();
                await this.saveAndContinue();
            },
            async skip() {
                await this.nextVideo();
            }
        },
        async mounted() {
            await this.loadMetadata();
            await this.restartEditingIfPossible();
        }
    }
</script>

<style scoped>
    .active {
        box-shadow: 0 0 0 5px orange;
    }

    .editing-card {
        overflow-y: auto;
        max-height: 76vh;
    }
</style>
