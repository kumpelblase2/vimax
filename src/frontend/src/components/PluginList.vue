<template>
    <v-sheet class="my-5">
        <v-toolbar flat>
            <v-toolbar-title>Plugins</v-toolbar-title>
            <v-spacer></v-spacer>
            <v-progress-circular v-if="loading" indeterminate width="3"></v-progress-circular>
        </v-toolbar>
        <v-data-table :headers="headers" :items="plugins" class="elevation-1" :items-per-page="20">
            <template slot="item" slot-scope="props">
                <tr>
                    <td>{{ props.item.name }}</td>
                    <td>{{ props.item.information.description }}</td>
                    <td>{{ props.item.information.author }}</td>
                    <td>{{ props.item.information.version }}</td>
                    <td>
                        <v-row>
                            <v-switch :input-value="props.item.enabled" @change="togglePlugin(props.item.name)" dense
                                      class="mt-0" hide-details></v-switch>
                            <v-icon class="ml-2" small @click="refreshPlugin(props.item.name)"
                                    title="Reload Metadata for all videos">refresh
                            </v-icon>
                            <v-icon class="ml-2" small @click="reload(props.item.name)"
                                    title="Reload the plugin from disk">sync
                            </v-icon>
                        </v-row>
                    </td>
                    <td>{{ displayDate(props.item.creationTime) }}</td>
                    <td>{{ displayDate(props.item.enabledAt) }}</td>
                    <td>{{ displayDate(props.item.disabledAt) }}</td>
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
    </v-sheet>
</template>

<script>
    import { mapActions, mapState } from "vuex";
    import pluginApi from "../api/plugin";

    export default {
        name: "PluginList",
        data() {
            return {
                loading: true,
                updating: false
            }
        },
        computed: {
            ...mapState('settings/plugin', ['plugins', 'headers'])
        },
        methods: {
            ...mapActions('settings/plugin', ['togglePlugin', 'loadPlugins', 'reloadPlugin']),
            async refreshPlugin(name) {
                this.updating = true;
                await pluginApi.refresh(name);
                this.updating = false;
            },
            async reload(name) {
                this.updating = true;
                await this.reloadPlugin(name);
                this.updating = false;
            },
            displayDate(date) {
                if(date != null) {
                    const parsed = new Date(Date.parse(date));
                    return parsed.getFullYear() + "-" + (parsed.getMonth() + 1).toString().padStart(2, "0") +
                        "-" + parsed.getDate().toString().padStart(2, "0") +
                        " " + parsed.getHours().toString().padStart(2, "0") +
                        ":" + parsed.getMinutes().toString().padStart(2, "0");
                } else {
                    return "";
                }
            }
        },
        async mounted() {
            this.loadPlugins().then(() => this.loading = false);
        }
    }
</script>

<style scoped>

</style>
