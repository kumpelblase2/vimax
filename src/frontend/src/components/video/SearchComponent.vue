<template>
    <v-row align="center">
        <v-col sm="auto" v-if="shouldShowVideoCount" class="subtitle-1 text--disabled">{{ videosAmount }} Videos</v-col>
        <v-col class="pa-0">
            <form action="#" @submit.prevent="doSearch">
                <v-text-field
                    v-model="query"
                    @click:append.prevent="doSearch"
                    placeholder="Search..."
                    :append-icon="icon"
                    color="white"
                    :hide-details="!hasError"
                    clearable
                    :error-messages="queryErrors"
                    @click:clear="reset"
                />
            </form>
        </v-col>
    </v-row>
</template>

<script>
    import { mapActions, mapGetters, mapMutations } from 'vuex';
    import router from '@/router';

    export default {
        name: "SearchComponent",
        data() {
            return {
                query: ""
            }
        },
        computed: {
            ...mapGetters('search', ['searchQuery', 'error']),
            ...mapGetters('videos', ['videosAmount']),
            shouldShowVideoCount() {
                return ['lg', 'xl'].includes(this.$vuetify.breakpoint.name);
            },
            icon() {
                if(this.searchQuery == this.queryValue) {
                    return "refresh";
                } else {
                    return "search";
                }
            },
            hasError() {
                return this.error != null && this.error.length > 0;
            },
            queryErrors() {
                if(this.hasError) {
                    return [this.error];
                } else {
                    return [];
                }
            },
            queryValue() {
                return this.query || "";
            }
        },
        watch: {
            searchQuery(newValue) {
                router.push(`/?search=${encodeURIComponent(newValue)}`).catch(ex => {
                    console.warn("Url seems to already be the same.", ex);
                });
                if(newValue !== this.query) {
                    this.query = newValue;
                    this.search();
                }
            }
        },
        methods: {
            ...mapActions('videos', ['search']),
            ...mapMutations('search', ['updateQuery']),
            doSearch() {
                if(this.queryValue != this.searchQuery) {
                    this.updateQuery(this.queryValue);
                }
                this.search();
            },
            reset() {
                this.query = "";
                this.doSearch();
            }
        },
        mounted() {
            this.query = this.searchQuery || "";
        }
    }
</script>

<style scoped>
    form {
        width: 100%;
    }
</style>
