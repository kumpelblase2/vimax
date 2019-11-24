<template>
    <v-card>
        <div>
            <v-toolbar flat>
                <v-toolbar-title>Libraries</v-toolbar-title>
                <v-progress-circular v-if="loading" indeterminate width="3"></v-progress-circular>
                <v-spacer></v-spacer>
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
                                <v-layout wrap>
                                    <v-flex xs12 sm6>
                                        <v-text-field v-model="editingItem.path"
                                                      label="Location"></v-text-field>
                                    </v-flex>
                                </v-layout>
                            </v-container>
                        </v-card-text>

                        <v-card-actions>
                            <v-spacer></v-spacer>
                            <v-btn color="blue darken-1" text @click="close">Cancel</v-btn>
                            <v-btn color="blue darken-1" text @click="save">Save</v-btn>
                        </v-card-actions>
                    </v-card>
                </v-dialog>
            </v-toolbar>
            <v-list>
                <v-list-tile v-for="library in libraries" :key="library.id">
                    <v-list-tile-content>
                        <v-list-tile-title>{{ library.path }}</v-list-tile-title>
                    </v-list-tile-content>

                    <v-list-tile-action>
                        <v-btn icon ripple>
                            <v-icon small @click="deleteItem(library)">delete</v-icon>
                        </v-btn>
                    </v-list-tile-action>
                </v-list-tile>
            </v-list>
        </div>
    </v-card>
</template>

<script>
    import { mapActions, mapState } from 'vuex';

    export default {
        name: "LibraryList",
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
            deleteItem(item) {
                if(confirm('Are you sure you want to delete this library?')) {
                    this.deleteLibrary(item);
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
