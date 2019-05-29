import Vue from 'vue'
import Router from 'vue-router'
import SettingsView from "../components/SettingsView";
import IndexView from "../components/IndexView";
import WatchView from "../components/WatchView";
import SortingView from "../components/SortingView";

Vue.use(Router);

export default new Router({
    routes: [
        {
            path: '/',
            name: 'Index',
            component: IndexView
        },
        {
            path: '/settings',
            name: 'Settings',
            component: SettingsView
        },
        {
            path: '/watch/:id',
            name: 'Watch',
            component: WatchView
        },
        {
            path: '/sorting',
            name: 'Sorting Mode',
            component: SortingView
        }
    ]
})
