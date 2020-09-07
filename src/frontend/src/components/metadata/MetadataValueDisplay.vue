<template>
    <div>
        <div v-if="metadataType === 'TAGLIST'">
            <v-chip small v-for="(item, index) in metadataValue" :key="index">{{item}}</v-chip>
        </div>
        <span v-else-if="displayAsText">{{ textValue }}</span>
        <span v-else><i>No display configured.</i></span>
    </div>
</template>

<script>
    import { canBeDisplayedAsText, toDisplayValue } from "../../helpers/metadata-display-helper";

    export default {
        name: "MetadataValueDisplay",
        props: ['metadata-value', 'metadata-definition'],
        computed: {
            metadataType() {
                return this.metadataDefinition.type;
            },
            displayAsText() {
                return canBeDisplayedAsText(this.metadataType);
            },
            textValue() {
                return toDisplayValue(this.metadataType, this.metadataValue);
            }
        }
    }
</script>
