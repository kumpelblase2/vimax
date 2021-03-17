<template>
    <div class="pa-0">
        <v-combobox v-if="metadataDefinition.type === 'TEXT' && metadataDefinition.options.suggest" hide-no-data
                    :items="values" :label="metadataDefinition.name" :value="metadataValue"
                    :loading="isLoading" @change="update" :solo="solo"/>
        <v-text-field v-else-if="metadataDefinition.type === 'TEXT' && !metadataDefinition.options.suggest"
                      :label="metadataDefinition.name" :value="metadataValue" @change="update" :solo="solo"/>
        <v-switch v-else-if="metadataDefinition.type === 'BOOLEAN'" @change="update"
                  :label="metadataDefinition.name" :input-value="metadataValue" :solo="solo" dense/>
        <v-text-field v-else-if="metadataDefinition.type === 'NUMBER'" type="number"
                      :label="metadataDefinition.name" :value="metadataValue"
                      @change="updateNumber" :solo="solo" dense/>
        <v-select v-else-if="metadataDefinition.type === 'SELECTION'" :label="metadataDefinition.name"
                  @change="updateSelection" :value="selectionValue" :items="metadataDefinition.options.values"
                  item-text="name" return-object :solo="solo" dense/>
        <v-combobox v-else-if="metadataDefinition.type === 'TAGLIST'" :label="metadataDefinition.name" :value="metadataValue"
                    @change="update" chips deletable-chips clearable multiple :items="values" :solo="solo"
                    :search-input.sync="search"/>
        <v-slider v-else-if="metadataDefinition.type === 'RANGE'" :label="metadataDefinition.name"
                  :value="metadataValue" @change="updateNumber" :solo="solo" :min="metadataDefinition.options.min"
                  :max="metadataDefinition.options.max" thumb-label/>
    </div>
</template>

<script>
    import metadataApi from "../../api/metadata";

    export default {
        name: "MetadataValueEditor",
        props: ['metadata-definition', 'metadata-value', 'solo'],
        data() {
            return {
                values: [],
                isLoading: false,
                search: ""
            }
        },
        computed: {
            withSuggestions() {
                return this.metadataDefinition.type === 'TAGLIST' || this.metadataDefinition.type === 'TEXT' &&
                    this.metadataDefinition.options.suggest;
            },
            selectionValue() {
                if(this.metadataDefinition.type !== 'SELECTION') {
                    return {};
                } else if(this.metadataValue != null && this.metadataValue.id != null) {
                    return this.metadataValue;
                } else {
                    return this.metadataDefinition.options.values.find(value => value.id === this.metadataValue);
                }
            }
        },
        methods: {
            update(ev) {
                if(this.metadataDefinition.type === 'TAGLIST') {
                    this.search = "";
                }
                this.$emit('change', ev);
            },
            updateNumber(ev) {
                this.$emit('change', parseInt(ev));
            },
            updateSelection(ev) {
                this.$emit('change', ev.id);
            }
        },
        mounted() {
            if(this.metadataDefinition.type === 'TAGLIST' && this.metadataValue != null) {
                this.metadataValue.forEach(value => this.values.push(value));
            }

            if(this.withSuggestions) {
                metadataApi.getMetadataValues(this.metadataDefinition.id).then(values => {
                    values.forEach(value => this.values.push(value));
                });
            }
        }
    }
</script>

<style scoped>

</style>
