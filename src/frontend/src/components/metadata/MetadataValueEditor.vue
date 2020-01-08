<template>
    <div style="padding: 0">
        <v-combobox v-if="metadataDefinition.type === 'TEXT' && metadataDefinition.options.suggest" hide-no-data
                    :items="values" :label="metadataDefinition.name" :value="metadataValue"
                    :loading="isLoading" @change="update" :solo="solo"></v-combobox>
        <v-text-field v-else-if="metadataDefinition.type === 'TEXT' && !metadataDefinition.options.suggest"
                      :label="metadataDefinition.name" :value="metadataValue" @change="update" :solo="solo"></v-text-field>
        <v-switch v-else-if="metadataDefinition.type === 'BOOLEAN'" @change="update"
                  :label="metadataDefinition.name" :input-value="metadataValue" :solo="solo" dense></v-switch>
        <v-text-field v-else-if="metadataDefinition.type === 'NUMBER'" type="number"
                      :label="metadataDefinition.name" :value="metadataValue"
                      @change="updateNumber" :solo="solo" dense></v-text-field>
        <v-select v-else-if="metadataDefinition.type === 'SELECTION'" :label="metadataDefinition.name"
                  @change="update" :value="metadataValue" :items="metadataDefinition.options.values"
                  item-text="name" return-object :solo="solo" :search-input.sync="search" dense></v-select>
        <v-combobox v-else-if="metadataDefinition.type === 'TAGLIST'" :label="metadataDefinition.name" :value="metadataValue"
                    @change="update" chips clearable multiple :items="values" :solo="solo">
            <template v-slot:selection="{item, selected, parent}">
                <v-chip :value="selected">
                    <span class="pr-2">
                        {{ item }}
                    </span>
                    <v-icon small @click="parent.selectItem(item)">close</v-icon>
                </v-chip>
            </template>
        </v-combobox>
        <v-slider v-else-if="metadataDefinition.type === 'RANGE'" :label="metadataDefinition.name"
                  :value="metadataValue" @change="updateNumber" :solo="solo" :min="metadataDefinition.options.min"
                  :max="metadataDefinition.options.max" thumb-label>
        </v-slider>
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
                search: null
            }
        },
        computed: {
            withSuggestions() {
                return this.metadataDefinition.type === 'TAGLIST' || this.metadataDefinition.type === 'TEXT' &&
                    this.metadataDefinition.options.suggest;
            }
        },
        methods: {
            update(ev) {
                this.$emit('change', ev);
            },
            updateNumber(ev) {
                this.$emit('change', parseInt(ev));
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
