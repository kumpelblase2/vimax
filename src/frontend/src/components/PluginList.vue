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
                        <td><v-switch v-model="props.item.enabled" @change="toggle(props.item, $event)"></v-switch></td>
                        <td>{{ props.item.creationTime }}</td>
                        <td>{{ props.item.enabledAt }}</td>
                        <td>{{ props.item.disabledAt }}</td>
                    </tr>
                </template>
                <template slot="no-data">
                    No plugins added yet.
                </template>
            </v-data-table>
        </div>
    </v-card>
</template>

<script>
    import pluginApi from "../api/plugin";

    export default {
        name: "PluginList",
        data() {
            return {
                headers: [
                    { text: "Name", value: "name" },
                    { text: "Enabled", value: "enabled" },
                    { text: "Added On", value: "createdAt" },
                    { text: "Enabled On", value: "enabledAt" },
                    { text: "Disabled On", value: "disabledAt" }
                ],
                plugins: [],
                loading: true
            }
        },
        methods: {
            async toggle(plugin, newValue) {
                if(!newValue) {
                    await pluginApi.disable(plugin.name);
                } else {
                    await pluginApi.enable(plugin.name);
                }
            }
        },
        async mounted() {
            const plugins = await pluginApi.getPlugins();
            plugins.forEach(plugin => this.plugins.push(plugin));
            this.loading = false;
        }
    }
</script>

<style scoped>

</style>
