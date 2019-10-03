<template>
    <v-select class="nav-select single-line" :value="visibleMetadata" :items="orderedMetadata" item-text="name" return-object
              flat dense multiple @change="onlyShowMetadata" placeholder="No metadata visible">
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
        computed: {
            ...mapGetters('settings/metadata', [
                'visibleMetadata',
                'orderedMetadata'
            ])
        },
        methods: {
            ...mapActions('settings/metadata', ['onlyShowMetadata'])
        }
    }
</script>

<style scoped>
    .nav-select {
        padding-top: 12px;
        padding-left: 25px;
        padding-right: 25px;
        max-width: 500px;
    }
</style>
