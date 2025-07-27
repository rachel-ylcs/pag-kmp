const NodePolyfillPlugin = require("node-polyfill-webpack-plugin");

config.plugins.push(new NodePolyfillPlugin());
config.devServer.static.push("../../node_modules/libpag/lib");
