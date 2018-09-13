var exec = require('cordova/exec');


exports.payment = function (payInfo, success, error) {
    exec(success, error, "alipay", "payment", [payInfo]);
};
exports.authV2 = function (payInfo, success, error) {
    exec(success, error, "alipay", "auth", [payInfo]);
};


