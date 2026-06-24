const path = require('path');
const { createRequire } = require('module');

function requireEnv(name) {
    const val = process.env[name];
    if (!val) {
        throw new Error(
            `Variable d'environnement manquante : ${name}. Lancer via 'sbt cdnPrepare' ou 'sbt cdnDebugPrepare'.`
        );
    }
    return val;
}

const requireFromCwd = createRequire(path.join(process.cwd(), 'package.json'));
const webpack = requireFromCwd('webpack');

const entry = requireEnv('UNRAVEL_ENTRY');
const outputPath = requireEnv('UNRAVEL_OUTPUT_PATH');
const isDebug = process.env.UNRAVEL_DEBUG === '1';

module.exports = {
    mode: isDebug ? 'development' : 'production',
    devtool: false,
    entry,
    output: {
        path: outputPath,
        filename: 'unravel-rdf.min.js',
        library: 'UnravelRdf',
        libraryTarget: 'umd',
        globalObject: 'typeof self !== "undefined" ? self : this',
        devtoolModuleFilenameTemplate: info =>
            `file://${path.resolve(info.absoluteResourcePath).replace(/\\/g, '/')}`,
        devtoolFallbackModuleFilenameTemplate: info =>
            `file://${path.resolve(info.absoluteResourcePath).replace(/\\/g, '/')}`
    },
    plugins: isDebug
        ? [
            new webpack.SourceMapDevToolPlugin({
                filename: 'unravel-rdf.min.js.map',
                noSources: false
            })
        ]
        : [],
    module: isDebug
        ? {
            rules: [
                {
                    test: /unravel-rdf\.js$/,
                    enforce: 'pre',
                    include: [path.dirname(entry)],
                    use: ['source-map-loader']
                }
            ]
        }
        : undefined,
    ignoreWarnings: isDebug
        ? [
            {
                module: /unravel-rdf\.js$/,
                message: /Failed to parse source map.*(URL is not supported|ENOENT)/
            }
        ]
        : [],
    resolve: {
        fallback: {
            crypto: false,
            buffer: false,
            stream: false,
            path: false,
            fs: false
        }
    },
    optimization: {
        minimize: !isDebug
    }
};