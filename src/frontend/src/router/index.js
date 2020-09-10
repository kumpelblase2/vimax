import CheckinView from "@/components/CheckinView";
import IndexView from "@/components/IndexView";
import PlaylistsView from "@/components/PlaylistsView";
import PlaylistView from "@/components/PlaylistView";
import SettingsView from "@/components/SettingsView";
import SortingView from "@/components/SortingView";
import Vue from 'vue'
import Router from 'vue-router'
import { CHECKIN, INDEX, PLAYLIST, PLAYLISTS, SETTINGS, SMART_PLAYLIST, SORTING } from "./views";

Vue.use(Router);

export default new Router({
    routes: [
        {
            path: '/',
            name: INDEX,
            component: IndexView
        },
        {
            path: '/playlists',
            name: PLAYLISTS,
            component: PlaylistsView
        },
        {
            path: '/settings',
            name: SETTINGS,
            component: SettingsView
        },
        {
            path: '/sorting',
            name: SORTING,
            component: SortingView
        },
        {
            path: '/playlist/:id',
            name: PLAYLIST,
            component: PlaylistView
        },
        {
            path: '/checkin',
            name: CHECKIN,
            component: CheckinView
        }
    ]
})
