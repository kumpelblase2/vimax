<template>
    <v-dialog :value="editing != null">
        <v-card>
            <v-card-title>
                <span class="headline">Create Smart Playlist</span>
            </v-card-title>

            <v-card-text>
                <v-container grid-list-md>
                    <v-row wrap>
                        <v-flex xs12 sm6>
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
                <v-btn color="blue darken-1" text @click="cancel">Cancel</v-btn>
                <v-btn color="blue darken-1" text @click="save">Save</v-btn>
            </v-card-actions>
        </v-card>
    </v-dialog>
</template>

<script>
import { applicationSortableMetadata } from "@/store/metadata";
import { mapGetters } from "vuex";

const possibleDirections = ['ASC', 'DESC'];

export default {
    name: "SmartPlaylistEditDialog",
    props: {
        editing: null
    },
    data() {
        return {
            name: "",
            query: "",
            property: applicationSortableMetadata[0].value,
            direction: possibleDirections[0]
        }
    },
    watch: {
        editing(newEditing) {
            if(newEditing != null) {
                this.name = newEditing.name;
                this.query = newEditing.query;
                this.property = newEditing.orderBy;
                this.direction = newEditing.orderDirection;
            }
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
                ...this.editing,
                name: this.name,
                query: this.query,
                orderBy: this.property,
                orderDirection: this.direction
            })
        },
        cancel() {
            this.name = "";
            this.query = "";
            this.property = applicationSortableMetadata[0].value;
            this.direction = possibleDirections[0];
            this.$emit('cancel');
        }
    }
}
</script>

<style scoped>

</style>
