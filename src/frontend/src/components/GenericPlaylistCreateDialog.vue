<template>
    <v-dialog :value="show" max-width="500px">
        <v-card>
            <v-card-title>
                <span class="headline">Create Playlist</span>
            </v-card-title>

            <v-card-text>
                <v-tabs v-model="tab" centered>
                    <v-tab>Playlist</v-tab>
                    <v-tab>Smart Playlist</v-tab>
                </v-tabs>
                <v-tabs-items v-model="tab">
                    <v-tab-item>
                        <v-container grid-list-md>
                            <v-row wrap>
                                <v-flex xs12>
                                    <v-text-field v-model="name" label="Name"/>
                                </v-flex>
                            </v-row>
                        </v-container>
                    </v-tab-item>
                    <v-tab-item>
                        <v-container grid-list-md>
                            <v-row wrap>
                                <v-flex xs12>
                                    <v-text-field v-model="name" label="Name"/>
                                    <v-text-field v-model="query" label="Query"/>
                                    <v-select :items="sortableProperties" item-text="name" hide-details v-model="property"
                                              label="Sort By"/>
                                    <v-select :items="directions" v-model="direction" label="Direction"/>
                                </v-flex>
                            </v-row>
                        </v-container>
                    </v-tab-item>
                </v-tabs-items>

            </v-card-text>

            <v-card-actions>
                <v-spacer/>
                <v-btn color="darken-1" @click="cancel">Cancel</v-btn>
                <v-btn color="primary darken-1" @click="save">Save</v-btn>
            </v-card-actions>
        </v-card>
    </v-dialog>
</template>

<script>
    import { applicationSortableMetadata } from "@/store/metadata";
    import { mapActions, mapGetters } from "vuex";

    const possibleDirections = ['ASC', 'DESC'];

    export default {
        name: "GenericPlaylistCreateDialog",
        props: {
            show: Boolean
        },
        data() {
            return {
                tab: 0,
                name: "",
                query: "",
                property: null,
                direction: null
            }
        },
        computed: {
            ...mapGetters('settings/metadata', ['sortableMetadata']),
            directions() {
                return possibleDirections;
            },
            sortableProperties() {
                return applicationSortableMetadata.concat(this.sortableMetadata);
            }
        },
        mounted() {
            this.loadMetadata()
        },
        methods: {
            ...mapActions('settings/metadata', ['loadMetadata']),
            cancel() {
                this.reset();
                this.$emit('cancel');
            },
            save() {
                if(this.tab === 0) { // Normal playlist
                    this.$emit('create-normal-playlist', this.name);
                } else { // Smart Playlist
                    this.$emit('create-smart-playlist', {
                        name: this.name,
                        query: this.query,
                        orderBy: this.property,
                        orderDirection: this.direction
                    });
                }
                this.reset();
            },
            reset() {
                this.tab = 0;
                this.name = "";
                this.query = "";
                this.property = null;
                this.direction = null;
            }
        }
    }
</script>

<style scoped>

</style>
