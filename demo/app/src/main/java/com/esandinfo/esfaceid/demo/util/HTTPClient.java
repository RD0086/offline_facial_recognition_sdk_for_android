package com.esandinfo.esfaceid.demo.util;

import android.util.Log;

import com.esandinfo.esfaceid.utils.MyLog;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * HTTP工具类
 */
public class HTTPClient {

    // WARNNING！！ : 为了保护密钥，这段代码建议写在服务器端，这里为了方便演示，把密钥写客户端了。
    // TODO 阿里云接入，请替换这里的APPCODE （为了保护APPCODE,此段代码通常放在服务器端）
    private String APPCODE;
    // TODO 非阿里云接入，请从管理控制台查询并替换这里的 APPCODE 和密钥, 参考文档： https://esandinfo.yuque.com/yv6e1k/aa4qsg/cdwove
    private String E_APPCODE;// = "d2808c1338ce01f3e3efdb486f9effb9";
    private String E_SECRET;// = "OUmYXYo5O5CrzXQakeF789PU4RSKVdObVtCSwp28g==";

    public HTTPClient() {
        if (APPCODE == null && (E_APPCODE == null&&E_SECRET == null)) {
            Log.i("FRC","如果是阿里云网关接入，请先设置 APPCODE , 如果非阿里云网关接入，请先设置 E_APPCODE, E_SECRET ， 如有疑问请联系 ：13691664797");
        }
    }

    /**
     * 获取授权证书， 参考： https://esandinfo.yuque.com/yv6e1k/aa4qsg/oe1zexnk63nudnu4
     *
     * @param initMsg 从SDK返回的初始化报文
     * @return
     */
    public String getCertificate(String initMsg) {
        FormBody body;
        String rspStr = null;
        FormBody.Builder bodyBuilder = new FormBody.Builder()
                .add("initMsg", initMsg);
        body = bodyBuilder.build();
        String url = String.format("https://edis.esandcloud.com/gateways?APPCODE=%s&ACTION=%s",E_APPCODE, "livingdetection/getCertificate");

        return post(url, body);
    }

    private String post(String url, FormBody body){
        String rspStr = null;
        try {
            OkHttpClient client = new OkHttpClient.Builder()
                    .retryOnConnectionFailure(true)
                    .connectTimeout(10, TimeUnit.SECONDS)
                    .readTimeout(20, TimeUnit.SECONDS)
                    .build();
            String appcode = null;
            if (APPCODE != null) {
                appcode = APPCODE;
            } else {
                appcode = E_SECRET;
            }

            Request.Builder requestBuilder = new Request.Builder().url(url);
            requestBuilder.addHeader("Authorization", "APPCODE " + appcode);
            requestBuilder.addHeader("X-Ca-Nonce", UUID.randomUUID().toString());
            requestBuilder.addHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
            requestBuilder.post(body);
            Request request = requestBuilder.build();
            Response response = client.newCall(request).execute();
            // 打印 header 数据
            MyLog.info("服务器返回header 数据 ：" + response.headers().toString());
            rspStr = response.body().string();
        }catch (Exception e){
            e.printStackTrace();
        }

        return rspStr;
    }
}
