<template>
    <v-card>
        <v-card-title>About</v-card-title>
        <v-card-text>
            <div class="text-center">
                Vimax is made with ❤️ by <a href="https://github.com/kumpelblase2">kumpelblase2</a>.<br/>
                Source code is located at <a href="https://github.com/kumpelblase2/vimax" target="_blank"
                                             referrerpolicy="no-referrer">GitHub</a> as well
                as <a href="https://aura.eternalwings.de/tim/vimax" target="_blank" referrerpolicy="no-referrer">my mirror</a>.
            </div>
            <hr/>
            <div class="text-center">
                Vimax uses some open source libraries:
            </div>

            <v-row>
                <v-col>
                    <h3>Backend</h3>
                    <span v-for="dependency in backendDependencies" :key="dependency.name">
                        <a :href="dependency.url">{{dependency.name}}</a> | {{dependency.license}}
                        <br />
                    </span>
                </v-col>
                <v-col>
                    <h3>Frontend</h3>
                    <span v-for="dependency in frontendDependencies" :key="dependency.name">
                        <a :href="dependency.url">{{dependency.name}}</a> | {{dependency.license}}
                        <br/>
                    </span>
                </v-col>
            </v-row>
        </v-card-text>
        <v-card-actions>
            <v-spacer/>
            <v-btn @click="$emit('close')" type="primary">Close</v-btn>
        </v-card-actions>
    </v-card>
</template>

<script>
    const frontendDependencies = [
        {
            name: "Vue.js",
            url: "https://github.com/vuejs/vue",
            license: "MIT License"
        },
        {
            name: "Vuetify",
            url: "https://github.com/vuetifyjs/vuetify",
            license: "MIT License"
        },
        {
            name: "Axios",
            url: "https://github.com/axios/axios",
            license: "MIT License"
        },
        {
            name: "Video.js",
            url: "https://github.com/videojs/video.js",
            license: "Apache License, 2.0"
        },
        {
            name: "Vue Mugen Scroll",
            url: "https://github.com/egoist/vue-mugen-scroll",
            license: "MIT License"
        },
        {
            name: "Vue Router",
            url: "https://github.com/vuejs/vue-router",
            license: "MIT License"
        },
        {
            name: "Vue Draggable",
            url: "https://github.com/SortableJS/Vue.Draggable",
            license: "MIT License"
        },
        {
            name: "Vuex",
            url: "https://github.com/vuejs/vuex",
            license: "MIT License"
        }
    ];

    const backendDependencies = [
        {
            name: "Spring Boot",
            url: "https://github.com/spring-projects/spring-boot",
            license: "Apache License, 2.0"
        },
        {
            name: "Flyway",
            url: "https://github.com/flyway/flyway",
            license: "Apache License, 2.0"
        },
        {
            name: "BetterParse",
            url: "https://github.com/h0tk3y/better-parse",
            license: "Apache License, 2.0"
        },
        {
            name: "KTSRunner",
            url: "https://github.com/s1monw1/KtsRunner",
            license: "MIT License"
        },
        {
            name: "Sqlite JDBC",
            url: "https://github.com/xerial/sqlite-jdbc",
            license: "Apache License, 2.0"
        },
        {
            name: "Faster XML Kotlin",
            url: "https://github.com/FasterXML/jackson-module-kotlin",
            license: "Apache License, 2.0"
        },
        {
            name: "JobRunr",
            url: "https://github.com/jobrunr/jobrunr",
            license: "LGPL, 3.0"
        }
    ];

    export default {
        name: "AboutDialog",
        data() {
            return {
                frontendDependencies,
                backendDependencies
            };
        }
    }
</script>

<style scoped>

</style>
