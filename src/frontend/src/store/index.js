import Vue from 'vue';
import Vuex from 'vuex';
import settings from './settings';
import videos from './video';

Vue.use(Vuex);

export default new Vuex.Store({
    modules: {
        settings,
        videos
    }
});
