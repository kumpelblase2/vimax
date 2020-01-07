<template>
    <v-dialog v-model="dialog" persistent max-width="750px" scrollable v-if="video">
        <v-card>
            <v-card-title>
                <span class="headline">Editing Video</span>
            </v-card-title>
            <v-card-text>
                <v-text-field v-model="video.name" label="Name"></v-text-field>
                <v-item-group mandatory @change="updateSelectedThumbnail" :value="video.selectedThumbnail">
                    <v-row wrap>
                        <v-col v-for="(thumbnail, index) in video.thumbnails" :key="index" cols="12" md="4">
                            <v-item active-class="active" class="element">
                                <v-img :src="thumbnailUrl(thumbnail)" :aspect-ratio="16/10" slot-scope="{ active, toggle }"
                                       @click="toggle"></v-img>
                            </v-item>
                        </v-col>
                    </v-row>
                </v-item-group>
                <div v-for="metadata in editableMetadata" :key="metadata.id">
                    <metadata-value-editor :metadata-definition="metadata"
                                           :metadata-value="video.metadata[metadata.id].value"
                                           @change="handleMetadataUpdate(metadata.id, $event)"></metadata-value-editor>
                </div>
            </v-card-text>
            <v-divider></v-divider>
            <v-card-actions>
                <v-btn @click="refreshThumbnails">Refresh Thumbs</v-btn>
                <v-spacer></v-spacer>
                <v-btn @click="save">Save</v-btn>
                <v-btn @click="close">Cancel</v-btn>
            </v-card-actions>
        </v-card>
    </v-dialog>
</template>

<script>
    import { mapActions, mapGetters, mapState } from "vuex";
    import MetadataValueEditor from "../metadata/MetadataValueEditor";
    import { getThumbnailURL } from "../../video";

    export default {
        name: "VideoEditDialog",
        components: { MetadataValueEditor },
        computed: {
            ...mapState('videos', ['editingVideo']),
            ...mapGetters('settings/metadata', ['editableMetadata']),
            dialog() {
                return this.editingVideo != null;
            },
            video() {
                return this.editingVideo;
            }
        },
        methods: {
            ...mapActions('videos', [
                'editVideo',
                'saveEditingVideo',
                'changeSelectedThumbnail',
                'setEditingMetadataValue',
                'refreshThumbnails'
            ]),
            close() {
                this.editVideo(null);
            },
            save() {
                this.saveEditingVideo().then(() => this.close());
                this.$emit('finish-edit');
            },
            thumbnailUrl(thumbnail) {
                return getThumbnailURL(thumbnail);
            },
            updateSelectedThumbnail(thumbnailIndex) {
                this.changeSelectedThumbnail(thumbnailIndex);
            },
            handleMetadataUpdate(id, event) {
                this.setEditingMetadataValue({ id, value: event });
            }
        }
    }
</script>

<style scoped>
    .active {
        box-shadow: 0 0 0 5px orange;
    }
</style>
