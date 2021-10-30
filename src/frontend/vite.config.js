import { createVuePlugin } from 'vite-plugin-vue2';
import path from 'path';
import Components from 'unplugin-vue-components/vite';
import {VuetifyResolver} from "unplugin-vue-components/resolvers";

const version = require('./package.json').version;

module.exports = {
    define: {
        VIMAX_VERSION: `"${version}"`
    },
    resolve: {
        alias: [
            {
                find: '@/',
                replacement: `${path.resolve(__dirname, './src')}/`,
            },
            {
                find: 'src/',
                replacement: `${path.resolve(__dirname, './src')}/`,
            },
        ],
        extensions: ['.mjs', '.js', '.ts', '.jsx', '.tsx', '.json', '.vue']
    },
    plugins: [
        createVuePlugin(),
        Components({
            resolvers: [
                VuetifyResolver(),
            ]
        }),
    ],
    server: {
        host: 'localhost',
        port: 8081,
        proxy: {
            '^/(api|notifications)': {
                target: "http://localhost:8080",
                changeOrigin: true
            }
        }
    },
    css: {
        preprocessorOptions: {
            scss: {
                additionalData: '@import "./node_modules/vuetify/src/styles/styles.sass";',
            },
        },
    },
};
