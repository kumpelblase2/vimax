import axios from 'axios';

export default {
  getLibraries() {
    return axios.get("/api/library").then(response => response.data);
  },

  saveLibrary(library) {
    return axios.post("/api/library", library).then(response => response.data);
  },

  deleteLibrary(id) {
    return axios.delete("/api/library/" + id).then(response => response.data);
  }
};
