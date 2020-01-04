<template>
    <v-card>
        <div>
            <v-toolbar flat>
                <v-toolbar-title>Plugins</v-toolbar-title>
                <v-spacer></v-spacer>
                <v-progress-circular v-if="loading" indeterminate width="3"></v-progress-circular>
            </v-toolbar>
            <v-data-table :headers="headers" :items="plugins" class="elevation-1" :items-per-page="20">
                <template slot="item" slot-scope="props">
                    <tr>
                        <td>{{ props.item.name }}</td>
                        <td style="display: flex;">
                            <v-switch :input-value="props.item.enabled" @change="togglePlugin(props.item.name)" dense
                                      style="margin-top: 10px"></v-switch>
                            <v-icon class="ml-2" small @click="refreshPlugin(props.item.name)">refresh</v-icon>
                        </td>
                        <td>{{ props.item.creationTime }}</td>
                        <td>{{ props.item.enabledAt }}</td>
                        <td>{{ props.item.disabledAt }}</td>
                    </tr>
                </template>
                <template slot="no-data">
                    No plugins added yet.
                </template>
            </v-data-table>

            <v-dialog v-model="updating" persistent width="300">
                <v-card dark>
                    <v-card-text>
                        Updating videos...
                        <v-progress-linear indeterminate class="mb-0"></v-progress-linear>
                    </v-card-text>
                </v-card>
            </v-dialog>
        </div>
    </v-card>
</template>

<script>
    import { mapActions, mapState } from "vuex";
    import pluginApi from "../api/plugin";

    export default {
        name: "PluginList",
        data() {
            return {
                loading: true,
                updating: false,
            }
        },
        computed: {
            ...mapState('settings/plugin', ['plugins', 'headers'])
        },
        methods: {
            ...mapActions('settings/plugin', ['togglePlugin', 'loadPlugins']),
            async refreshPlugin(name) {
                this.updating = true;
                await pluginApi.refresh(name);
                this.updating = false;
            }
        },
        async mounted() {
            this.loadPlugins().then(() => this.loading = false);
        }
    }
</script>

<style scoped>

</style>
