/**
 * FileName: HttpClientUtil
 * Author: wangtao
 * Date: 2018/5/3 14:10
 * Description:
 */
package com.example.bluetooth;


import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

/**
 * http
 *
 * @author wangtao
 * @create 2018/5/3
 */

public class HttpUtlis {
    /**
     * get请求封装
     */
    public static void getRequest(String url, Map<String, String> params, String encode) {
        StringBuffer sb = new StringBuffer(url);

        if (params != null && !params.isEmpty()) {
            sb.append("?");

            for (Map.Entry<String, String> entry : params.entrySet()) {    //增强for遍历循环添加拼接请求内容
                sb.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
            }
        }
        HttpClient httpclient = new DefaultHttpClient();
        try {
            HttpGet request = new HttpGet(String.valueOf(sb));
            request.addHeader("Accept", "text/json");
            HttpResponse response = httpclient.execute(request);
            //获取HttpEntity
            HttpEntity entity = response.getEntity();
            //获取响应的结果信息
            String json = EntityUtils.toString(entity, "UTF-8");


            Log.v("tttt", json);

        } catch (ClientProtocolException e) {
            Log.v("tttt", "get请求出错:"+e.getMessage());
        } catch (IOException e) {
            Log.v("tttt", "get请求出错"+e.getMessage());
        }
    }







    /**
     * POST请求
     */
    public static void postRequest(String url, Map<String, String> params, String encode) {
        StringBuffer sb = new StringBuffer();
        if (params != null && !params.isEmpty()) {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                sb.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
            }
            sb.deleteCharAt(sb.length() - 1);
        }

        try {
            URL path = new URL(url);
            if (path != null) {
                HttpURLConnection con = (HttpURLConnection) path.openConnection();
                con.setRequestMethod("POST");   //设置请求方法POST
                con.setConnectTimeout(3000);
                con.setDoOutput(true);
                con.setDoInput(true);
                byte[] bytes = sb.toString().getBytes();
                OutputStream outputStream = con.getOutputStream();
                outputStream.write(bytes);
                outputStream.close();
                if (con.getResponseCode() == 200) {

                }
                Log.v("tttt", con.getResponseMessage().toString());
            }
        } catch (Exception e) {
            e.printStackTrace();

        }

    }


}