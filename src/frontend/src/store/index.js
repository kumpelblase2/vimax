import Vue from 'vue';
import Vuex from 'vuex';
import videoApi from '../api/videos';
import settings from './settings';

Vue.use(Vuex);

export default new Vuex.Store({
  modules: {
    settings
  },
  state: {
    recentVideos: [],
    videos: []
  },
  mutations: {
    setRecentVideos(state, videos) {
      state.videos = videos;
    }
  },
  actions: {
    loadRecentVideos({ commit }) {
      return videoApi.getRecentVideos().then(response => {
        commit('setRecentVideos', response.data);
      }).catch((ex) => console.error(ex));
    }
  }
});
