<template>
    <v-dialog v-model="dialog" @keydown.esc="cancel">
        <v-card>
            <v-toolbar dark dense flat>
                <v-toolbar-title class="white--text">Confirm Library Deletion</v-toolbar-title>
            </v-toolbar>
            <v-card-text class="pa-4">
                Are you sure you want to delete this library?
                <v-checkbox v-model="deleteThumbs" label="Delete Thumbnails"></v-checkbox>
            </v-card-text>
            <v-card-actions class="pt-0">
                <v-spacer></v-spacer>
                <v-btn color="primary darken-1" text @click.native="agree">Yes</v-btn>
                <v-btn color="grey" text @click.native="cancel">Cancel</v-btn>
            </v-card-actions>
        </v-card>
    </v-dialog>
</template>

<script>
    export default {
        name: "LibraryDeleteConfirmDialog",
        data: () => ({
            dialog: false,
            resolve: null,
            reject: null,
            deleteThumbs: true
        }),
        methods: {
            open() {
                this.dialog = true
                return new Promise((resolve, reject) => {
                    this.resolve = resolve
                    this.reject = reject
                })
            },
            agree() {
                this.resolve({confirm: true, thumbnails: this.deleteThumbs})
                this.dialog = false
            },
            cancel() {
                this.resolve({confirm: false})
                this.dialog = false
            }
        }
    }
</script>
