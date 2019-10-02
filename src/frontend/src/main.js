// The Vue build version to load with the `import` command
// (runtime-only or standalone) has been set in webpack.base.conf with an alias.
import Vue from 'vue'
import App from './App'
import router from './router'
import Vuetify from 'vuetify';
import store from './store';

import 'material-design-icons-iconfont/dist/material-design-icons.css';
import 'vuetify/dist/vuetify.min.css';
import 'video.js/dist/video-js.css';
import './video/setup-video-thumbnail-plugin';

import colors from 'vuetify/es5/util/colors'

Vue.config.productionTip = false;

Vue.use(Vuetify, {
    theme: {
        primary: colors.orange.base
    }
});

router.afterEach((to, _) => {
    store.commit('page/switchView', to.name);
});

/* eslint-disable no-new */
new Vue({
    el: '#app',
    router,
    store,
    components: { App },
    template: '<App/>'
});
