<template>
    <div class="text-truncate clickable">
        <template v-if="metadataType === 'TAGLIST'">
            <v-chip small v-for="item in metadataValue" :key="item" @click="filterFor(item)">{{item}}</v-chip>
        </template>
        <span v-else-if="displayAsText" @click="filter">{{ textValue }}</span>
        <span v-else><i>No display configured.</i></span>
    </div>
</template>

<script>
    import { canBeDisplayedAsText, toDisplayValue } from "@/helpers/metadata-display-helper";
    import { mapActions } from "vuex";

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
                return toDisplayValue(this.metadataDefinition, this.metadataValue);
            }
        },
        methods: {
            ...mapActions('search', ['addFilterFor']),
            filterFor(value) {
                this.addFilterFor({ metadata: this.metadataDefinition, value });
            },
            filter() {
                this.addFilterFor({ metadata: this.metadataDefinition, value: this.textValue });
            }
        }
    }
</script>


<style>
    .clickable {
        cursor: pointer;
    }
</style>
