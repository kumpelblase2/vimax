<template>
    <v-simple-table dense class="minimal">
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
        return metadataValue != null && metadataValue.value != null ? metadataValue.value : definition.options.defaultValue;
    }

    export default {
        name: 'VideoMetadataDisplay',
        components: { MetadataValueDisplay },
        props: {
            videoMetadata: null,
            all: Boolean
        },
        computed: {
            ...mapGetters('settings/metadata', [
                'visibleMetadata',
                'orderedMetadata'
            ]),
            metadatas() {
                const usedMetadata = this.all ? this.orderedMetadata : this.visibleMetadata;

                return usedMetadata.map(definition => {
                    return {
                        value: valueOrDefault(this.videoMetadata[definition.id], definition),
                        definition
                    }
                });
            }
        }
    }
</script>

<style>
.minimal .v-data-table__wrapper table {
    table-layout: fixed;
}

.minimal table td {
    padding: 0;
}
</style>
