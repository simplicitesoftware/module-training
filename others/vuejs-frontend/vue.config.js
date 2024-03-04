
module.exports = {
    publicPath: '/',
    configureWebpack: {
        devtool: 'source-map',
        optimization: {
          nodeEnv: false,
        },
        performance: {
          hints: false,
          maxEntrypointSize: 512000,
          maxAssetSize: 512000
        }
    },
    chainWebpack: (config) => {
        config.plugin('define').tap((definitions) => {
          Object.assign(definitions[0], {
            __VUE_PROD_HYDRATION_MISMATCH_DETAILS__: 'false'
          })
          return definitions
        })
      }
      
}