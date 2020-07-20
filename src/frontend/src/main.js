import Vue from 'vue'
import App from './App'
import router from './router'
import store from './store';
import vuetify from './plugin/Vuetify';
import { setup } from './eventsource'

import 'video.js/dist/video-js.css';
import './video/setup-video-thumbnail-plugin';

Vue.config.productionTip = false;

router.afterEach((to, _) => {
    store.commit('page/switchView', to.name);
});

setup(store);

new Vue({
    router,
    store,
    vuetify,
    render: h => h(App)
}).$mount('#app');
