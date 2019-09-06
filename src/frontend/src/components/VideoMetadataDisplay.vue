<template>
    <v-layout column>
        <v-layout v-for="(metadata, index) in metadatas" xs12 column :key="metadata.definition.id">
            <v-layout justift-space-between row my-1>
                <v-flex>{{ metadata.definition.name }}</v-flex>
                <v-flex class="text-xs-right">
                    <metadata-value-display :metadata-value="metadata.value"
                                            :metadata-definition="metadata.definition"></metadata-value-display>
                </v-flex>
            </v-layout>
            <v-divider xs12 v-if="needsDivider(index)"></v-divider>
        </v-layout>
    </v-layout>
</template>
<script>
    import MetadataValueDisplay from "./MetadataValueDisplay"
    import { mapGetters } from "vuex";

    function valueOrDefault(metadataValue, definition) {
        return metadataValue != null && metadataValue.value != null && metadataValue.value.value != null ?
            metadataValue.value.value : definition.options.defaultValue;
    }

    export default {
        name: 'VideoMetadataDisplay',
        components: { MetadataValueDisplay },
        props: {
            videoMetadata: null
        },
        computed: {
            ...mapGetters('settings/metadata', [
                'orderedMetadata'
            ]),
            metadatas() {

                return this.orderedMetadata.map(definition => {
                    return {
                        value: valueOrDefault(this.videoMetadata.find(metadata => metadata.definition.id === definition.id), definition),
                        definition
                    }
                });
            }
        },
        methods: {
            needsDivider(index) {
                return index < this.videoMetadata.length
            }
        }
    }
</script>
