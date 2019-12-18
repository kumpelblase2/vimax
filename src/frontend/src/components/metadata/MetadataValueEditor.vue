<template>
    <div>
        <v-combobox v-if="metadataDefinition.type === 'TEXT' && metadataDefinition.options.suggest" hide-no-data
                    :items="autocompleteValues" :label="metadataDefinition.name" :value="metadataValue"
                    :loading="isLoading" @change="update" :solo="solo"></v-combobox>
        <v-text-field v-if="metadataDefinition.type === 'TEXT' && !metadataDefinition.options.suggest"
                      :label="metadataDefinition.name" :value="metadataValue" @change="update" :solo="solo"></v-text-field>
        <v-switch v-else-if="metadataDefinition.type === 'BOOLEAN'" @change="update"
                  :label="metadataDefinition.name" :value="metadataValue" :solo="solo"></v-switch>
        <v-text-field v-else-if="metadataDefinition.type === 'NUMBER'" type="number"
                      :label="metadataDefinition.name" :value="metadataValue"
                      @change="updateNumber" :solo="solo"></v-text-field>
        <v-select v-else-if="metadataDefinition.type === 'SELECTION'" :label="metadataDefinition.name"
                  @change="update" :value="metadataValue" :items="metadataDefinition.options.values"
                  item-text="name" return-object :solo="solo" :search-input.sync="search"></v-select>
        <v-combobox v-else-if="metadataDefinition.type === 'TAGLIST'" :label="metadataDefinition.name" :value="metadataValue"
                    @change="update" chips clearable multiple :items="autocompleteValues" :solo="solo">
            <template #selection="data">
                <v-chip :value="data.selected" close @input="removeTag(data.item)">
                    {{ data.item }}
                </v-chip>
            </template>
        </v-combobox>
    </div>
</template>

<script>
    import { mapActions, mapGetters } from "vuex";

    export default {
        name: "MetadataValueEditor",
        props: ['metadata-definition', 'metadata-value', 'solo'],
        data() {
            return {
                tagValues: [],
                isLoading: false,
                search: null
            }
        },
        computed: {
            ...mapGetters('settings/metadata', ['possibleValues']),
            withSuggestions() {
                return this.metadataDefinition.type === 'TAGLIST' || this.metadataDefinition.type === 'TEXT' &&
                    this.metadataDefinition.options.suggest;
            },
            autocompleteValues() {
                return this.possibleValues(this.metadataDefinition.id);
            }
        },
        methods: {
            ...mapActions('settings/metadata', ['loadValuesFor']),
            update(ev) {
                this.$emit('change', ev);
                if(this.metadataDefinition.type === 'TAGLIST') {
                    for(let elem of ev) {
                        if(!this.tagValues.includes(elem)) {
                            this.tagValues.push(elem);
                        }
                    }
                }
            },
            updateNumber(ev) {
                this.$emit('change', parseInt(ev));
            },
            removeTag(tag) {
                this.$emit('change', this.metadataValue.filter(elem => elem !== tag));
            }
        },
        mounted() {
            if(this.metadataDefinition.type === 'TAGLIST' && this.metadataValue != null) {
                this.tagValues = [...this.metadataValue];
            }

            if(this.withSuggestions) {
                this.loadValuesFor(this.metadataDefinition.id);
            }
        }
    }
</script>

<style scoped>

</style>
