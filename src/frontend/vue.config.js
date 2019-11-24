module.exports = {
    lintOnSave: false,
    transpileDependencies: [
        "vuetify"
    ],
    devServer: {
        port: 8081,
        proxy: {
            '^/api': {
                target: "localhost:8080"
            }
        }
    }
};
