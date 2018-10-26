package cn.hhjjj.alipay;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.alipay.sdk.app.PayTask;
import com.alipay.sdk.app.AuthTask;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.util.Map;
import java.util.HashMap;

import android.widget.Toast;
import android.text.TextUtils;
import android.annotation.SuppressLint;

/**
 * This class echoes a string called from JavaScript.
 */
public class alipay extends CordovaPlugin {

   // private static final int SDK_PAY_FLAG = 1;



    	/** 商户私钥，pkcs8格式 */
    	/** 如下私钥，RSA2_PRIVATE 或者 RSA_PRIVATE 只需要填入一个 */
    	/** 如果商户两个都设置了，优先使用 RSA2_PRIVATE */
    	/** RSA2_PRIVATE 可以保证商户交易在更加安全的环境下进行，建议使用 RSA2_PRIVATE */
    	/** 获取 RSA2_PRIVATE，建议使用支付宝提供的公私钥生成工具生成， */
    	/** 工具地址：https://doc.open.alipay.com/docs/doc.htm?treeId=291&articleId=106097&docType=1 */

    	private static final int SDK_PAY_FLAG = 1;
    	private static final int SDK_AUTH_FLAG = 2;




    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (action.equals("payment")) {
            String orderInfo = args.getString(0);
            this.payment(orderInfo, callbackContext);
            return true;
        }else if(action.equals("auth")){
        String authInfo = args.getString(0);
        this.authV2(authInfo,callbackContext);
        return true;
        }
        return false;
    }

    private void payment(String orderInfo, final CallbackContext callbackContext) {

        final String payInfo = orderInfo;
        cordova.getThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                PayTask alipay = new PayTask(cordova.getActivity());
                Map<String, String> result = alipay.payV2(payInfo, true);
                Log.i("msp", result.toString());

                Message msg = new Message();
                msg.what = SDK_PAY_FLAG;
                msg.obj = result;
                mHandler.sendMessage(msg);

                PayResult payResult = new PayResult(result);
                String resultInfo = payResult.getResult();// 同步返回需要验证的信息
                String resultStatus = payResult.getResultStatus();
                // 判断resultStatus 为9000则代表支付成功
                if (TextUtils.equals(resultStatus, "9000")) {
                    // 该笔订单是否真实支付成功，需要依赖服务端的异步通知。
                    callbackContext.success(new JSONObject(result));
                } else {
                    // 该笔订单真实的支付结果，需要依赖服务端的异步通知。
                    callbackContext.error(new JSONObject(result));
                }
            }
        });

    }


        private void authV2(String orderInfo, final CallbackContext callbackContext) {

            final String payInfo = orderInfo;
            cordova.getThreadPool().execute(new Runnable() {
                @Override
                public void run() {
                    AuthTask alipay = new AuthTask(cordova.getActivity());
                    Map<String, String> result = alipay.authV2(payInfo, true);
                    Log.i("msp", result.toString());

                    Message msg = new Message();
                    msg.what = SDK_AUTH_FLAG;
                    msg.obj = result;
                    mHandler.sendMessage(msg);

                    AuthResult payResult = new AuthResult(result,true);
                    String resultInfo = payResult.getResult();// 同步返回需要验证的信息
                    String resultStatus = payResult.getResultStatus();
                    Map<String, String> keyValues = new HashMap<String, String>();
                    keyValues.put("auth_code",payResult.getAuthCode());
                    keyValues.put("user_id",payResult.getUserId());

                    // 判断resultStatus 为9000则代表支付成功
                    if (TextUtils.equals(resultStatus, "9000")) {
                        // 该笔订单是否真实支付成功，需要依赖服务端的异步通知。

                        callbackContext.success(new JSONObject(keyValues));
                    } else {
                        // 该笔订单真实的支付结果，需要依赖服务端的异步通知。
                        callbackContext.error(new JSONObject(result));
                    }
                }
            });

        }




    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @SuppressWarnings("unused")
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SDK_PAY_FLAG: {
                    @SuppressWarnings("unchecked")
                    PayResult payResult = new PayResult((Map<String, String>) msg.obj);
                    /**
                     对于支付结果，请商户依赖服务端的异步通知结果。同步通知结果，仅作为支付结束的通知。
                     */
                    String resultInfo = payResult.getResult();// 同步返回需要验证的信息
                    String resultStatus = payResult.getResultStatus();
                    // 判断resultStatus 为9000则代表支付成功
                    // 判断resultStatus 为9000则代表支付成功
                    if (TextUtils.equals(resultStatus, "9000")) {
                        // 该笔订单是否真实支付成功，需要依赖服务端的异步通知。
                        Toast.makeText(cordova.getActivity(), "支付成功" + resultStatus, Toast.LENGTH_SHORT);
                    } else {
                        // 该笔订单真实的支付结果，需要依赖服务端的异步通知。
                        Toast.makeText(cordova.getActivity(), "支付失败" + resultStatus, Toast.LENGTH_SHORT);
                    }
                    break;
                }
                default:
                    break;
            }
        }

        ;
    };

}
