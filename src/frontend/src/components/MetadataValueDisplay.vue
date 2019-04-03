<template>
    <div>
        <div v-if="metadataType === 'TAGLIST'">
            <v-chip small v-for="(item, index) in metadataValue.value" :key="index">{{item}}</v-chip>
        </div>
        <span v-else-if="displayAsText">{{ textValue }}</span>
    </div>
</template>

<script>
    import { canBeDisplayedAsText, toDisplayValue } from "../helpers/metadata-display-helper";

    export default {
        name: "MetadataValueDisplay",
        props: ['metadata-value'],
        computed: {
            metadataType() {
                return this.metadataValue.metadata.type;
            },
            displayAsText() {
                return canBeDisplayedAsText(this.metadataType);
            },
            textValue() {
                if(this.metadataValue != null && this.metadataValue.value != null) {
                    return toDisplayValue(this.metadataType, this.metadataValue.value);
                } else {
                    return "";
                }
            }
        }
    }
</script>
