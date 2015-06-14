try {
    require("source-map-support").install();
} catch(err) {
}
require("./out/goog/bootstrap/nodejs.js");
require("./out/loom.js");
goog.require("loom.core");
goog.require("cljs.nodejscli");
