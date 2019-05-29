<template>
    <v-layout column>
        <v-layout v-for="(metadata, index) in videoMetadata" xs12 column :key="metadata.id">
            <v-layout justift-space-between row my-1>
                <v-flex>{{ metadata.metadata.name }}</v-flex>
                <v-flex class="text-xs-right">
                    <metadata-value-display :metadata-value="metadata"></metadata-value-display>
                </v-flex>
            </v-layout>
            <v-divider xs12 v-if="needsDivider(index)"></v-divider>
        </v-layout>
    </v-layout>
</template>
<script>
    import MetadataValueDisplay from "./MetadataValueDisplay"

    export default {
        name: 'VideoMetadataDisplay',
        components: { MetadataValueDisplay },
        props: {
            videoMetadata: null
        },
        methods: {
            needsDivider(index) {
                return index < this.videoMetadata.length - 1
            }
        }
    }
</script>
