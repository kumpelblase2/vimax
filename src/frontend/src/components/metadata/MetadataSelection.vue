<template>
    <v-select class="nav-select single-line" :value="visibleMetadata" :items="orderedMetadata" item-text="name" return-object
              multiple @change="onlyShowMetadata" placeholder="No metadata visible" single-line hide-details>
        <template v-slot:prepend-item>
            <v-list-item @click="toggleAll">
                <v-list-item-content>
                    {{selectionText}}
                </v-list-item-content>
            </v-list-item>
            <v-divider class="mt-2"></v-divider>
        </template>
        <template v-slot:selection="{ item, index }">
            <span v-if="index === 2" class="grey--text">
                 &nbsp;+{{visibleMetadata.length - 2}} more
            </span>
            <span v-if="index < 2">
                {{item.name}}<span v-if="index + 1 < visibleMetadata.length">,</span>
            </span>
        </template>
    </v-select>
</template>

<script>
    import { mapGetters, mapActions, mapMutations } from 'vuex';

    export default {
        name: "MetadataSelection",
        computed: {
            allSelected() {
                return this.visibleMetadata.length === this.orderedMetadata.length;
            },
            ...mapGetters('settings/metadata', [
                'visibleMetadata',
                'orderedMetadata'
            ]),
            selectionText() {
                return this.allSelected ? "Select None" : "Select All"
            }
        },
        methods: {
            ...mapActions('settings/metadata', ['onlyShowMetadata']),
            ...mapMutations('settings/metadata', ['showAllMetadata', 'hideAllMetadata']),
            toggleAll() {
                if(this.allSelected) {
                    this.hideAllMetadata();
                } else {
                    this.showAllMetadata();
                }
            }
        }
    }
</script>

<style scoped>
    .nav-select {
        max-width: 500px;
    }
</style>
