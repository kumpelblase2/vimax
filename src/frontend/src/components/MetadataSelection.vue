<template>
    <v-select class="nav-select single-line" :value="visibleMetadata" :items="orderedMetadata" item-text="name" return-object
              flat dense multiple @change="onlyShowMetadata" placeholder="No metadata visible" solo>
        <template v-slot:prepend-item>
            <v-list-tile @click="toggleAll">
                <v-list-tile-content>
                    Select All
                </v-list-tile-content>
            </v-list-tile>
            <v-divider class="mt-2"></v-divider>
        </template>
        <template v-slot:selection="{ item, index }">
            <span v-if="index === 2" class="grey--text">
                 &nbsp;+{{visibleMetadata.length - 2}} more
            </span>
            <span v-if="index < 2">
                {{item.name}}<span v-if="index + 1 < visibleMetadata.length">,&nbsp;</span>
            </span>
        </template>
    </v-select>
</template>

<script>
    import { mapGetters, mapActions } from 'vuex';

    export default {
        name: "MetadataSelection",
        computed: {
            allSelected() {
                return this.visibleMetadata.length === this.orderedMetadata.length;
            },
            ...mapGetters('settings/metadata', [
                'visibleMetadata',
                'orderedMetadata'
            ])
        },
        methods: {
            ...mapActions('settings/metadata', ['onlyShowMetadata', 'showAllMetadata', 'hideAllMetadata']),
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
        padding-top: 10px;
        max-width: 500px;
    }
</style>
