process.env.VUE_APP_VERSION = require('./package.json').version;

module.exports = {
    lintOnSave: false,
    transpileDependencies: [
        "vuetify"
    ],
    devServer: {
        port: 8081,
        proxy: {
            '^/(api|notifications)': {
                target: "http://localhost:8080",
                changeOrigin: true
            }
        }
    }
};
