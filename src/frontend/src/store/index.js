import Vue from 'vue';
import Vuex from 'vuex';
import settings from './settings';
import videos from './video';
import sorting from './sorting';
import page from "./page";
import search from "./search";
import playlist from "./playlist";

Vue.use(Vuex);

export default new Vuex.Store({
    modules: {
        settings,
        videos,
        sorting,
        page,
        search,
        playlist
    }
});
