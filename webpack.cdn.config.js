const path = require('path');

module.exports = {
    mode: 'production',
    entry: './unravel-rdf.js',              // relatif à target/npm/ (cwd au moment du lancement)
    output: {
        path: path.resolve(__dirname, 'target/cdn'),  // __dirname = racine du projet
        filename: 'unravel-rdf.min.js',
        library: 'UnravelRdf',
        libraryTarget: 'umd',
        globalObject: 'typeof self !== "undefined" ? self : this'
    },
    resolve: {
        fallback: {
            crypto: false,
            buffer: false,
            stream: false,
            path:   false,
            fs:     false
        }
    },
    optimization: { minimize: true }
};
