/**
 * Proxy config for redirecting relative /api calls to the backend running on port 8080
 *
 * @see https://github.com/chimurai/http-proxy-middleware#options
 */
module.exports = {
  "/jme-security-scs/api/*": {
    target: "http://localhost:8080",
    changeOrigin: true,
    secure: false,
    logLevel: "debug",
  },
};
