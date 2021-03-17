<template>
    <v-combobox :label="metadataDefinition.name" :value="metadataValue"
                @change="update" clearable multiple :items="values" :solo="solo"
                :search-input.sync="search">
        <template v-slot:selection="{ item }">
            <v-chip close @click:close="removeValue(item)" :color="colorOf(item)" @click="toggleItem(item)">
                {{ item.substr(1) }}
            </v-chip>
        </template>
    </v-combobox>
</template>

<script>
import metadataApi from "@/api/metadata";

function filterUnique(array) {
    return array.filter((element, index, fullArray) => fullArray.indexOf(element) === index);
}

export default {
    name: "MultiTagValueEditor",
    props: ['metadata-definition', 'metadata-value', 'solo'],
    data() {
        return {
            values: [],
            isLoading: false,
            search: ""
        }
    },
    methods: {
        colorOf(item) {
            return item.startsWith("+") ? 'green' : 'red';
        },
        update(ev) {
            this.search = "";
            const resultingValues = filterUnique((ev || []).map(value => {
                if(value.startsWith("+") || value.startsWith("-")) {
                    return value;
                } else {
                    return "+" + value;
                }
            }));
            this.$emit('change', resultingValues);
        },
        removeValue(ev) {
            this.$emit('change', this.metadataValue.filter(value => value !== ev));
        },
        toggleItem(ev) {
            const toggledItem = (ev.startsWith("+") ? "-" : "+") + ev.substr(1);

            this.$emit('change', filterUnique(this.metadataValue.map(value => {
                if(value === ev) {
                    return toggledItem;
                } else {
                    return value;
                }
            })));
        }
    },
    mounted() {
        metadataApi.getMetadataValues(this.metadataDefinition.id).then(values => {
            values.forEach(value => this.values.push(value));
        });
    }
}
</script>

<style scoped>

</style>
