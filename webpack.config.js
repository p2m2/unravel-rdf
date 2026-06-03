const path = require('path');

module.exports = {
    mode: 'production',          // active Terser + optimisations webpack
    output: {
        library: 'Discovery',
        libraryTarget: 'umd',
        libraryExport: 'default'
    },
    optimization: {
        minimize: true
    }
};