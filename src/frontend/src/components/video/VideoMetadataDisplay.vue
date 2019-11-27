<template>
    <v-simple-table dense>
        <template v-slot:default>
            <tbody>
            <tr v-for="(metadata, index) in metadatas" :key="index">
                <td>{{ metadata.definition.name }}</td>
                <td>
                    <metadata-value-display :metadata-value="metadata.value"
                                            :metadata-definition="metadata.definition"></metadata-value-display>
                </td>
            </tr>
            </tbody>
        </template>
    </v-simple-table>
</template>
<script>
    import MetadataValueDisplay from "../metadata/MetadataValueDisplay"
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
                'visibleMetadata'
            ]),
            metadatas() {
                return this.visibleMetadata.map(definition => {
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
