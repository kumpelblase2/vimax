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
                        <td><v-switch :input-value="props.item.enabled" @change="togglePlugin(props.item.name)"></v-switch></td>
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
    import { mapActions, mapState } from "vuex";

    export default {
        name: "PluginList",
        data() {
            return {
                loading: true
            }
        },
        computed: {
            ...mapState('settings/plugin', ['plugins', 'headers'])
        },
        methods: {
            ...mapActions('settings/plugin', ['togglePlugin', 'loadPlugins'])
        },
        async mounted() {
            this.loadPlugins().then(() => this.loading = false);
        }
    }
</script>

<style scoped>

</style>
