<template>
    <div>
        <v-text-field v-if="metadataDefinition.type === 'TEXT'"
                      :label="metadataDefinition.name" :value="metadataValue"
                      @change="update"></v-text-field>
        <v-switch v-else-if="metadataDefinition.type === 'BOOLEAN'" @change="update"
                  :label="metadataDefinition.name"></v-switch>
        <v-text-field v-else-if="metadataDefinition.type === 'NUMBER'" type="number"
                      :label="metadataDefinition.name" :value="metadataValue"
                      @change="updateNumber"></v-text-field>
        <v-select v-else-if="metadataDefinition.type === 'SELECTION'" @change="update" :value="metadataValue"
                  :items="metadataDefinition.options.values" item-text="name" return-object></v-select>
    </div>
</template>

<script>
    export default {
        name: "MetadataValueEditor",
        props: ['metadata-definition', 'metadata-value'],
        methods: {
            update(ev) {
                this.$emit('change', ev);
            },
            updateNumber(ev) {
                this.$emit('change', parseInt(ev));
            }
        }
    }
</script>

<style scoped>

</style>
