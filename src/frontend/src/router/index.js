import Vue from 'vue'
import Router from 'vue-router'
import SettingsView from "../components/SettingsView";
import IndexView from "../components/IndexView";
import WatchView from "../components/WatchView";
import SortingView from "../components/SortingView";
import PlaylistsView from "../components/PlaylistsView";
import { INDEX, PLAYLISTS, SETTINGS, SORTING, WATCH, PLAYLIST, CHECKIN } from "./views";
import PlaylistView from "../components/PlaylistView";
import CheckinView from "../components/CheckinView";

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
            path: '/watch/:id?',
            name: WATCH,
            component: WatchView
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
    ],
})
