<template>
    <div v-if="type === 'TEXT'">
        <v-text-field v-model="options.defaultValue" label="Default Value"/>
        <v-switch v-model="options.suggest" label="Suggestions"/>
    </div>
    <div v-else-if="type === 'NUMBER' || type === 'RANGE'">
        <v-text-field type="number" v-model="options.defaultValue" label="Default Value"/>
        <v-text-field type="number" v-model="options.max" label="Maximum"/>
        <v-text-field type="number" v-model="options.min" label="Minimum"/>
        <v-text-field type="number" v-model="options.step" label="Step"/>
    </div>
    <div v-else-if="type === 'DURATION'">
        <v-text-field type="time" v-model="options.defaultValue"/>
    </div>
    <div v-else-if="type === 'BOOLEAN'">
        <v-switch v-model="options.defaultValue" label="Default Value"/>
    </div>
    <div v-else-if="type === 'SELECTION'">
        <v-btn @click="addSelectionValue">
            <v-icon>add</v-icon>
        </v-btn>
        <v-list>
            <v-list-item v-for="(item, index) in options.values" :key="index">
                <v-text-field v-model="item.name" label="Value"/>
                <v-list-item-action>
                    <v-btn icon @click="removeSelection(index)">
                        <v-icon>delete</v-icon>
                    </v-btn>
                </v-list-item-action>
            </v-list-item>
        </v-list>
        <v-select v-model="options.defaultValue" :items="options.values" item-text="name" label="Default Value"
                  return-object/>
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
