<template>
    <div>
        <v-combobox v-if="metadataDefinition.type === 'TEXT' && metadataDefinition.options.suggest" hide-no-data
                      :items="loadedItems" :label="metadataDefinition.name" :value="metadataValue"
                      :search-input.sync="search" :loading="isLoading" @change="update"></v-combobox>
        <v-text-field v-if="metadataDefinition.type === 'TEXT' && !metadataDefinition.options.suggest"
                      :label="metadataDefinition.name" :value="metadataValue" @change="update"></v-text-field>
        <v-switch v-else-if="metadataDefinition.type === 'BOOLEAN'" @change="update"
                  :label="metadataDefinition.name" :value="metadataValue"></v-switch>
        <v-text-field v-else-if="metadataDefinition.type === 'NUMBER'" type="number"
                      :label="metadataDefinition.name" :value="metadataValue"
                      @change="updateNumber"></v-text-field>
        <v-select v-else-if="metadataDefinition.type === 'SELECTION'" :label="metadataDefinition.name"
                  @change="update" :value="metadataValue" :items="metadataDefinition.options.values"
                  item-text="name" return-object></v-select>
        <v-combobox v-else-if="metadataDefinition.type === 'TAGLIST'" :label="metadataDefinition.name" :value="metadataValue"
                    @change="update" chips clearable multiple :items="tagValues">
            <template #selection="data">
                <v-chip :selected="data.selected" close @input="removeTag(data.item)">
                    {{ data.item }}
                </v-chip>
            </template>
        </v-combobox>
    </div>
</template>

<script>
    import metadataApi from "../api/metadata";

    export default {
        name: "MetadataValueEditor",
        props: ['metadata-definition', 'metadata-value'],
        data() {
            return {
                tagValues: [],
                isLoading: false,
                loadedItems: [],
                search: null,
            }
        },
        methods: {
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
        watch: {
            search(value) {
                if(this.loadedItems.length > 0) return;
                if(this.isLoading) return;
                this.isLoading = true;

                metadataApi.getMetadataValues(this.metadataDefinition.id).then(values => {
                    this.loadedItems = values;
                }).finally(() => {
                    this.isLoading = false;
                });
            }
        },
        mounted() {
            if(this.metadataDefinition.type === 'TAGLIST' && this.metadataValue != null) {
                this.tagValues = [...this.metadataValue];
            }
        }
    }
</script>

<style scoped>

</style>
