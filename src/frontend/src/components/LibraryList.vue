<template>
    <v-card class="mb-5">
        <div>
            <v-toolbar flat>
                <v-toolbar-title>Libraries</v-toolbar-title>
                <v-progress-circular v-if="loading" indeterminate width="3"></v-progress-circular>
                <v-spacer></v-spacer>
                <library-delete-confirm-dialog ref="deleteConfirmation"/>
                <v-dialog v-model="dialog" max-width="500px">
                    <template v-slot:activator="{ on }">
                        <v-btn v-on="on" color="primary" dark class="mb-2">Add Library</v-btn>
                    </template>
                    <v-card>
                        <v-card-title>
                            <span class="headline">Create Library</span>
                        </v-card-title>

                        <v-card-text>
                            <v-container grid-list-md>
                                <v-row wrap>
                                    <v-flex xs12 sm6>
                                        <v-text-field v-model="editingItem.path" label="Location"/>
                                    </v-flex>
                                </v-row>
                            </v-container>
                        </v-card-text>

                        <v-card-actions>
                            <v-spacer/>
                            <v-btn @click="close">Cancel</v-btn>
                            <v-btn color="primary" @click="save">Save</v-btn>
                        </v-card-actions>
                    </v-card>
                </v-dialog>
            </v-toolbar>
            <v-list>
                <v-list-item v-for="library in libraries" :key="library.id">
                    <v-list-item-content>
                        <v-list-item-title>{{ library.path }}</v-list-item-title>
                    </v-list-item-content>

                    <v-list-item-action>
                        <v-btn icon ripple>
                            <v-icon small @click="deleteItem(library)" :title="'Delete library at ' + library.path">
                                delete
                            </v-icon>
                        </v-btn>
                    </v-list-item-action>
                </v-list-item>
            </v-list>
        </div>
    </v-card>
</template>

<script>
    import { mapActions, mapState } from 'vuex';
    import LibraryDeleteConfirmDialog from "./LibraryDeleteConfirmDialog";

    export default {
        name: "LibraryList",
        components: { LibraryDeleteConfirmDialog },
        data: () => ({
            loading: false,
            dialog: false
        }),
        watch: {
            dialog(val) {
                val || this.close()
            }
        },
        computed: {
            ...mapState('settings/library', [
                'libraries',
                'editingItem',
                'headers'
            ])
        },
        methods: {
            async deleteItem(item) {
                const result = await this.$refs.deleteConfirmation.open();
                if(result.confirm) {
                    this.deleteLibrary({ library: item, deleteThumbnails: result.thumbnails });
                }
            },
            close() {
                this.dialog = false;
                setTimeout(() => {
                    this.resetEditItem();
                }, 300)
            },
            save() {
                this.saveLibrary(this.editingItem).then(() => {
                    this.close();
                });
            },
            ...mapActions('settings/library', [
                'loadLibraries',
                'saveLibrary',
                'deleteLibrary',
                'resetEditItem'
            ])
        },
        mounted() {
            this.loading = true;
            this.loadLibraries().then(() => {
                this.loading = false;
            });
        }
    }
</script>

<style scoped>

</style>
