<template>
    <v-card class="my-5">
        <div>
            <v-toolbar flat>
                <v-toolbar-title>Metadata</v-toolbar-title>
                <v-progress-circular v-if="loading" indeterminate width="3"/>
                <v-spacer/>
                <v-dialog v-model="dialog" max-width="500px">
                    <template v-slot:activator="{ on }">
                        <v-btn v-on="on" color="primary" dark class="mb-2">New Metadata</v-btn>
                    </template>
                    <v-card>
                        <v-card-title>
                            <span class="headline">{{ formTitle }}</span>
                        </v-card-title>

                        <v-card-text>
                            <v-container grid-list-md>
                                <v-row wrap>
                                    <v-flex xs12 sm6>
                                        <v-text-field v-model="editingItem.name" label="Name"/>
                                    </v-flex>
                                    <v-flex xs12 sm6>
                                        <v-select v-model="editingItem.type" :items="possibleMetadataTypes" label="Type"/>
                                    </v-flex>
                                    <v-flex xs12 sm6>
                                        <v-select v-model="editingItem.ordering"
                                                  :items="[{text:'Ascending',value:'ASC'},{text:'Descending', value:'DESC'}]"
                                                  label="Ordering"/>
                                    </v-flex>
                                    <v-flex xs12>
                                        <metadata-options :type="editingItem.type"
                                                          :options="editingItem.options"/>
                                    </v-flex>
                                </v-row>
                            </v-container>
                        </v-card-text>

                        <v-card-actions>
                            <v-spacer/>
                            <v-btn color="blue darken-1" text @click="close">Cancel</v-btn>
                            <v-btn color="blue darken-1" text @click="save">Save</v-btn>
                        </v-card-actions>
                    </v-card>
                </v-dialog>
            </v-toolbar>
            <v-data-table :headers="headers" :items="metadatas" class="elevation-1" :items-per-page="15"
                          :custom-sort="sort">
                <template slot="item" slot-scope="props">
                    <tr>
                        <td>{{ props.item.displayOrder }}</td>
                        <td>{{ props.item.name }}</td>
                        <td>{{ props.item.type }}</td>
                        <td>{{ defaultValueToText(props.item) }}</td>
                        <td class="justify-center">
                            <v-icon v-if="props.item.displayOrder < metadataCount" small class="mr-2"
                                    @click="moveDown(props.item)" :title="'Move ' + props.item.name + ' down'">
                                arrow_downward
                            </v-icon>
                            <v-icon v-if="props.item.displayOrder > 0" small class="mr-2" @click="moveUp(props.item)"
                                    :title="'Move ' + props.item.name + ' up'">arrow_upward
                            </v-icon>
                            <v-icon v-if="!props.item.systemSpecified" small class="mr-2" @click="editItem(props.item)"
                                    :title="'Edit ' + props.item.name">
                                edit
                            </v-icon>
                            <v-icon v-if="!props.item.systemSpecified" small @click="deleteItem(props.item)"
                                    :title="'Delete ' + props.item.name">delete
                            </v-icon>
                        </td>
                    </tr>
                </template>
                <template slot="no-data">
                    No Metadata configured yet.
                </template>
            </v-data-table>
        </div>
    </v-card>
</template>

<script>
    import { mapActions, mapState } from 'vuex';
    import MetadataOptions from "./MetadataOptions";
    import { toDisplayValue } from "../../helpers/metadata-display-helper";

    export default {
        name: "MetadataList",
        components: { MetadataOptions },
        data: () => ({
            loading: false,
            dialog: false,
            editedIndex: -1
        }),
        watch: {
            dialog(val) {
                val || this.close()
            }
        },
        computed: {
            formTitle() {
                return this.editedIndex === -1 ? 'New Metadata' : 'Edit Metadata'
            },
            metadataCount() {
                return this.metadatas.length;
            },
            ...mapState({
                metadatas: state => state.settings.metadata.metadata
            }),
            ...mapState('settings/metadata', [
                'metadata',
                'headers',
                'editingItem',
                'possibleMetadataTypes'
            ])
        },
        methods: {
            sort(items) {
                return items.sort((a, b) => a.displayOrder - b.displayOrder);
            },
            editItem(item) {
                this.editedIndex = this.metadatas.indexOf(item);
                this.startEditItem(item);
                this.dialog = true
            },

            deleteItem(item) {
                if(confirm('Are you sure you want to delete this metadata?')) {
                    this.deleteMetadata(item);
                }
            },
            close() {
                this.dialog = false;
                setTimeout(() => {
                    this.resetEditItem();
                    this.editedIndex = -1
                }, 300)
            },
            save() {
                this.saveMetadata(this.editingItem).then(() => {
                    this.close();
                });
            },
            moveUp(metadata) {
                this.moveMetadataUp(metadata);
            },
            moveDown(metadata) {
                this.moveMetadataDown(metadata);
            },
            ...mapActions('settings/metadata', [
                'deleteMetadata',
                'saveMetadata',
                'loadMetadata',
                'resetEditItem',
                'startEditItem',
                'moveMetadataDown',
                'moveMetadataUp'
            ]),
            defaultValueToText(metadata) {
                return toDisplayValue(metadata, metadata.options.defaultValue);
            }
        },
        mounted() {
            this.loading = true;
            this.loadMetadata().then(() => {
                this.loading = false;
            });
        }
    }
</script>

<style scoped>

</style>
