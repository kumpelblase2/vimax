<template>
    <div>
        <div v-if="type === 'TEXT'">
            <v-text-field v-model="options.defaultValue"
                          label="Default Value"></v-text-field>
            <v-switch v-model="options.suggest" label="Suggestions"></v-switch>
        </div>
        <div v-if="type === 'NUMBER' || type === 'RANGE'">
            <v-text-field type="number" v-model="options.defaultValue"
                          label="Default Value"></v-text-field>
            <v-text-field type="number" v-model="options.max"
                          label="Maximum"></v-text-field>
            <v-text-field type="number" v-model="options.min"
                          label="Minimum"></v-text-field>
            <v-text-field type="number" v-model="options.step"
                          label="Step"></v-text-field>
        </div>
        <div v-if="type === 'DURATION'">
            <v-text-field type="time" v-model="options.defaultValue"></v-text-field>
        </div>
        <div v-if="type === 'BOOLEAN'">
            <v-switch v-model="options.defaultValue" label="Default Value"></v-switch>
        </div>
        <div v-if="type === 'SELECTION'">
            <v-btn @click="addSelectionValue">
                <v-icon>add</v-icon>
            </v-btn>
            <v-list>
                <v-list-tile v-for="(item, index) in options.values" :key="index">
                    <v-text-field v-model="item.name" label="Value"></v-text-field>
                    <v-list-tile-action>
                        <v-btn icon @click="removeSelection(index)">
                            <v-icon>delete</v-icon>
                        </v-btn>
                    </v-list-tile-action>
                </v-list-tile>
            </v-list>
            <v-select v-model="options.defaultValue"
                      :items="options.values" item-text="name" label="Default Value" return-object></v-select>
        </div>
    </div>
</template>

<script>
    export default {
        name: "MetadataOptions",
        props: ['type', 'options'],
        methods: {
            addSelectionValue() {
                this.options.values.push({ name: '', id: null });
            },
            removeSelection(index) {
                this.options.values.splice(index, 1);
            }
        }
    }
</script>

<style scoped>

</style>
