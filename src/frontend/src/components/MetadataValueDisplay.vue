<template>
    <div>
        <span v-if="displayAsText">{{ textValue }}</span>
        <div v-else-if="metadataValue.metadata.type === 'TAGLIST'">
            <v-chip v-for="(item, index) in metadataValue.metadata.value" :key="index">{{item}}</v-chip>
        </div>
    </div>
</template>

<script>
    export default {
        name: "MetadataValueDisplay",
        props: ['metadata-value'],
        computed: {
            displayAsText() {
                switch(this.metadataValue.metadata.type) {
                    case 'TEXT':
                    case 'NUMBER':
                    case 'RANGE':
                    case 'BOOLEAN':
                    case 'SELECTION':
                        return true;
                    default:
                        return false;
                }
            },
            textValue() {
                if(this.metadataValue != null && this.metadataValue.value != null) {
                    switch(this.metadataValue.metadata.type) {
                        case 'TEXT':
                        case 'NUMBER':
                        case 'RANGE':
                        case 'BOOLEAN':
                            return this.metadataValue.value.toString();
                        case 'SELECTION':
                            return this.metadataValue.value.name.toString();
                        default:
                            throw `No text representation for ${this.metadataValue.metadata.type}.`;
                    }
                } else {
                    return "";
                }
            }
        }
    }
</script>
