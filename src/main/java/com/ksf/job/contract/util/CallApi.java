package com.ksf.job.contract.util;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CallApi {

    private static final Logger logger = LogManager.getLogger();

    public static String callGet(String url, String token) {
        try {
            OkHttpClient client = new OkHttpClient.Builder().build();
            //logger.info("Request:" + url);

            Request requestVHT = new Request.Builder()
                    .url(url)
                    .method("GET", null)
                    .addHeader("Authorization", "Bearer " + token)
                    .addHeader("Content-Type", "application/json")
                    .build();
            Response executeVht = client.newCall(requestVHT).execute();
            String response = executeVht.body().string();
            //logger.info("Response:" + response);
            return response;
        } catch (Exception e) {
            logger.error(e);
            e.printStackTrace();
        }
        return null;
    }

    /*public static boolean callPut(String url, String token, String orderId) {
        try {
            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(10, TimeUnit.SECONDS)
                    .writeTimeout(10, TimeUnit.SECONDS)
                    .readTimeout(300, TimeUnit.SECONDS)
                    .build();
            MediaType mediaType = MediaType.parse("application/json");
            RequestBody body = RequestBody.create(mediaType, "{\"ord_id\":"+orderId+",\"con_upgrate\":true,\"online_is\":false}");
            Request requestVHT = new Request.Builder()
                    .url(url)
                    .method("PUT", body)
                    .addHeader("Authorization", "Bearer " + token)
                    .addHeader("Content-Type", "application/json")
                    .build();
            Response executeVht = client.newCall(requestVHT).execute();
            logger.debug(executeVht);
            if (executeVht.code() == 200) {
                executeVht.body().close();
                return true;
            }
            return false;
        } catch (Exception e) {
            logger.error(e);
        }
        return false;
    }

    public static boolean callPost(String url, String token, String orderId) {
        try {
            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(10, TimeUnit.SECONDS)
                    .writeTimeout(10, TimeUnit.SECONDS)
                    .readTimeout(300, TimeUnit.SECONDS)
                    .build();
            MediaType mediaType = MediaType.parse("application/json");
            RequestBody body = RequestBody.create(mediaType, "{\"ord_id\":"+orderId+"}");
            Request requestVHT = new Request.Builder()
                    .url(url)
                    .method("POST", body)
                    .addHeader("Authorization", "Bearer " + token)
                    .addHeader("Content-Type", "application/json")
                    .build();
            Response executeVht = client.newCall(requestVHT).execute();
            if (executeVht.code() == 200) {
                executeVht.body().close();
                return true;
            }
            return false;
        } catch (Exception e) {
            logger.error(e);
        }
        return false;
    }*/

}
