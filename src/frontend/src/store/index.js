import Vue from 'vue';
import Vuex from 'vuex';
import settings from './settings';
import videos from './video';
import sorting from './sorting';

Vue.use(Vuex);

export default new Vuex.Store({
    modules: {
        settings,
        videos,
        sorting
    }
});
