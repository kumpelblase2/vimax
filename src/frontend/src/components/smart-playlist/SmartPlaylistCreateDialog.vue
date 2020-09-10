<template>
    <v-dialog v-model="dialog" max-width="800px" width="50%">
        <template v-slot:activator="{ on }">
            <v-btn color="primary" v-on="on">Create</v-btn>
        </template>
        <v-card>
            <v-card-title>
                <span class="headline">Create Smart Playlist</span>
            </v-card-title>

            <v-card-text>
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
import { mapGetters } from "vuex";

const possibleDirections = ['ASC', 'DESC'];

export default {
    name: "SmartPlaylistCreateDialog",
    data() {
        return {
            dialog: false,
            name: "",
            query: "",
            property: applicationSortableMetadata[0].value,
            direction: possibleDirections[0]
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
    methods: {
        save() {
            this.$emit('save', {
                name: this.name,
                query: this.query,
                orderBy: this.property,
                orderDirection: this.direction
            })
            this.dialog = false;
        },
        cancel() {
            this.dialog = false;
            this.name = "";
            this.query = "";
            this.property = applicationSortableMetadata[0].value;
            this.direction = possibleDirections[0];
        }
    }
}
</script>

<style scoped>

</style>
