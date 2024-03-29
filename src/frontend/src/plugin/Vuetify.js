import Vue from 'vue';
import Vuetify from "vuetify/lib";
import 'material-design-icons-iconfont/dist/material-design-icons.css';

Vue.use(Vuetify);

export default new Vuetify({
    icons: {
        iconfont: 'md'
    },
    theme: {
        dark: true,
        themes: {
            dark: {
                primary: '#ff9800' // Alternative: #7fcd91
            }
        }
    }
});
