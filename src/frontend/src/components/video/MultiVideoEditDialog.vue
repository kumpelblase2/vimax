<template>
    <v-dialog :value="isEditingMultiple" persistent max-width="750px" scrollable>
        <v-card v-if="isEditingMultiple">
            <v-card-title>
                <span class="headline">Editing multiple Video(s)</span>
            </v-card-title>
            <v-card-text>
                <v-row class="pa-0" v-for="metadata in editableMetadata" :key="metadata.id">
                    <v-checkbox v-model="selected" :value="metadata.id" solo color="primary"/>
                    <v-col class="pa-0">
                        <multi-tag-value-editor v-if="metadata.type === 'TAGLIST'" class="col-md-auto" md="auto"
                                                :metadata-definition="metadata"
                                                :metadata-value="getValueOf(metadata)"
                                                @change="handleMetadataUpdate(metadata.id, $event)"/>
                        <metadata-value-editor v-else class="col-md-auto" md="auto" :metadata-definition="metadata"
                                               :metadata-value="getValueOf(metadata)"
                                               @change="handleMetadataUpdate(metadata.id, $event)"/>
                    </v-col>
                </v-row>
            </v-card-text>
            <v-divider/>
            <v-card-actions>
                <v-spacer/>
                <v-btn @click="save">Save</v-btn>
                <v-btn @click="close">Cancel</v-btn>
            </v-card-actions>
        </v-card>
    </v-dialog>
</template>

<script>
import MultiTagValueEditor from "@/components/metadata/MultiTagValueEditor";
import { mapActions, mapGetters, mapMutations } from "vuex";
import MetadataValueEditor from "../metadata/MetadataValueEditor";

export default {
    name: "MultiVideoEditDialog",
    components: { MultiTagValueEditor, MetadataValueEditor },
    data() {
        return {
            selected: []
        }
    },
    computed: {
        ...mapGetters('videos/editing', ['isEditingMultiple', 'getMultiEditValue']),
        ...mapGetters('settings/metadata', ['editableMetadata'])
    },
    methods: {
        ...mapActions('videos/editing', ['saveMultiVideoEdit']),
        ...mapMutations('videos/editing', ['setMultiEditValue', 'resetMultiEdit']),
        close() {
            this.resetMultiEdit();
            this.selected = [];
        },
        async save() {
            await this.saveMultiVideoEdit(this.selected);
            this.$emit('finish-edit');
            this.selected = [];
        },
        handleMetadataUpdate(id, event) {
            this.setMultiEditValue({ id, value: event });
            if(!this.selected.includes(id)) {
                this.selected.push(id);
            }
        },
        getValueOf(metadata) {
            return this.getMultiEditValue(metadata.id);
        }
    }
}
</script>

<style scoped>

</style>
