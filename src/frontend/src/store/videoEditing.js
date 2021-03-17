import Vue from "vue";
import videoApi from "../api/videos";

export default {
    namespaced: true,
    state: {
        editingVideo: null,
        multiEditIds: [],
        multiEditValues: {}
    },
    mutations: {
        resetEditingVideo(state) {
            state.editingVideo = null;
        },
        resetMultiEdit(state) {
            state.multiEditIds = [];
            state.multiEditValues = {};
        },
        setEditingVideoValues(state, video) {
            // Can't use Object.assign() because it only creates a shallow copy...
            state.editingVideo = JSON.parse(JSON.stringify(video));
        },
        setEditingVideoIds(state, ids) {
            state.multiEditIds = [...ids];
        },
        setEditingMetadataValue(state, { id, value }) {
            state.editingVideo.metadata[id].value = value;
        },
        setMultiEditValue(state, { id, value }) {
            Vue.set(state.multiEditValues, id, value);
        },
        changeThumbnailsInEdit(state, thumbnailIndex) {
            state.editingVideo.selectedThumbnail = thumbnailIndex;
        },
        updateEditingThumbnails(state, thumbnails) {
            state.editingVideo.thumbnails = thumbnails;
        }
    },
    getters: {
        isEditingMultiple(state) {
            return state.multiEditIds.length > 0;
        },
        getMultiEditValue(state) {
            return (id) => state.multiEditValues[id];
        }
    },
    actions: {
        async editSelectedVideos({ commit, rootState }) {
            const videoIds = rootState.videos.selectedVideoIds;
            commit('setEditingVideoIds', videoIds);
        },
        async editVideo({ commit, rootGetters, dispatch }, videoId) {
            if(videoId == null) {
                commit('resetEditingVideo');
            } else {
                if(!rootGetters['videos/hasVideo'](videoId)) {
                    await dispatch('videos/reloadVideo', videoId, { root: true });
                }
                commit('setEditingVideoValues', rootGetters['videos/getVideo'](videoId));
            }
        },
        async saveEditingVideo({ commit, state }) {
            const editedVideo = await videoApi.saveVideo(state.editingVideo);
            commit('videos/addOrUpdateVideo', editedVideo, { root: true });
        },
        async refreshThumbnails({ commit, state }) {
            const newThumbnails = await videoApi.refreshThumbnails(state.editingVideo);
            commit('videos/updateThumbnail', { videoId: state.editingVideo.id, thumbnails: newThumbnails }, { root: true });
            commit('updateEditingThumbnails', newThumbnails);
        },
        async saveMultiVideoEdit({ commit, getters, state, rootGetters }, metadataToSave) {
            const newValues = { };
            const allMetadata = rootGetters['settings/metadata/editableMetadata'];
            for(let key of metadataToSave) {
                const metadata = allMetadata.find(m => m.id === key);
                if(metadata == null) {
                    console.error(`Tried to update metadata with id ${key} but it's not editable.`);
                    continue;
                }

                newValues[key] = {
                    'meta-type': metadata.type, // need this info for backend
                    value: state.multiEditValues[key]
                };
            }

            const saved = await videoApi.saveVideos(state.multiEditIds, newValues);
            saved.forEach(video => {
                commit('videos/addOrUpdateVideo', video, { root: true });
            });
            commit('resetMultiEdit');
        }
    }
}
