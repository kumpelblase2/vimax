<template>
    <v-dialog :value="editing != null" max-width="600px">
        <v-card>
            <v-card-title>
                <span class="headline">Edit Playlist</span>
            </v-card-title>

            <v-card-text>
                <v-text-field v-model="name" label="Name"/>
            </v-card-text>

            <v-card-actions>
                <v-spacer/>
                <v-btn text @click="cancel">Cancel</v-btn>
                <v-btn color="primary darken-1" text @click="save">Save</v-btn>
            </v-card-actions>
        </v-card>
    </v-dialog>
</template>

<script>
    export default {
        name: "PlaylistEditDialog",
        props: {
            editing: null
        },
        data() {
            return {
                name: ""
            }
        },
        watch: {
            editing(newValue) {
                this.name = newValue.name;
            }
        },
        methods: {
            save() {
                this.$emit('save', this.name);
            },
            cancel() {
                this.$emit('cancel');
            }
        }
    }
</script>

<style scoped>

</style>
