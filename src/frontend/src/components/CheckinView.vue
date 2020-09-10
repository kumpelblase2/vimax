<template>
    <v-col fill-height>
        <v-row>
            <v-flex xs4 offset-4>
                <v-text-field label="Filtering Query" id="filter" :value="filter" @change="updateFilter"
                              solo hide-details></v-text-field>
            </v-flex>
        </v-row>
        <v-row v-if="editingVideo">
            <v-col fill-height>
                <single-video-player :autoplay="false" :disable-events="true" :video="editingVideo"/>
            </v-col>
            <v-col>
                <v-row>
                    <v-flex xs6><v-btn @click="skip" block>Skip</v-btn></v-flex>
                    <v-flex xs6><v-btn color="primary" block @click="next">Save and Next</v-btn></v-flex>
                </v-row>
                <v-card class="editing-card">
                    <v-card-title>
                        {{editingVideo.name}}
                        <v-spacer/>
                    </v-card-title>
                    <v-card-text>
                        <v-item-group mandatory @change="updateSelectedThumbnail" :value="editingVideo.selectedThumbnail">
                            <v-row wrap>
                                <v-col v-for="(thumbnail, index) in editingVideo.thumbnails" :key="index" cols="12" md="4">
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
    </v-col>
</template>

<script>
    import { mapActions, mapGetters, mapMutations, mapState } from "vuex";
    import SingleVideoPlayer from "./player/SingleVideoPlayer";
    import MetadataValueEditor from "./metadata/MetadataValueEditor";
    import { getThumbnailURL } from "../video";

    export default {
        name: "CheckinView",
        computed: {
            ...mapGetters('checkin', ['filter']),
            ...mapGetters('settings/metadata', ['editableMetadata']),
            ...mapState('videos/editing', ['editingVideo'])
        },
        components: {
            MetadataValueEditor,
            SingleVideoPlayer
        },
        methods: {
            ...mapMutations('videos/editing', ['changeThumbnailsInEdit', 'setEditingMetadataValue', 'resetEditingVideo']),
            ...mapActions('videos/editing', ['saveEditingVideo']),
            ...mapActions('checkin', ['updateFilter','nextVideo', 'restartEditingIfPossible']),
            ...mapActions('settings/metadata', ['loadMetadata']),
            handleMetadataUpdate(id, event) {
                this.setEditingMetadataValue({ id, value: event });
            },
            getValueOf(metadata) {
                const value = this.editingVideo.metadata[metadata.id].value;
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
                this.changeThumbnailsInEdit(thumbnailIndex);
            },
            async next() {
                await this.$nextTick();
                await this.saveEditingVideo();
                await this.nextVideo();
            },
            async skip() {
                await this.nextVideo();
            }
        },
        async mounted() {
            await this.loadMetadata();
            this.restartEditingIfPossible();
        },
        beforeRouteLeave(_to, _from, next) {
            this.resetEditingVideo();
            next();
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
