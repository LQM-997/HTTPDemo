package com.example.zf.httpexample;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by ZF on 2017-11-17.
 */

public class HttpUtil {
    //get方式
    public static void requestGet(final Handler handler, final String path, final Map<String, String> paramsMap) {

        //在子线程中操作网络请求
        new Thread(new Runnable() {
            @Override
            public void run() {
                //urlConnection请求服务器，验证
                BufferedReader in = null;
                String result = "";
                HttpURLConnection conn = null;
                try {
                    StringBuilder tempParams = new StringBuilder();
                    int pos = 0;
                    for (String key : paramsMap.keySet()) {
                        if (pos > 0) {
                            tempParams.append("&");
                        }
                        tempParams.append(String.format("%s=%s", key, URLEncoder.encode(paramsMap.get(key), "utf-8")));
                        pos++;
                    }
                    String urlNameString = (paramsMap.isEmpty() ? path
                            : (path + "?" + tempParams.toString()));
                    //1：url对象
                    URL url = new URL(urlNameString);
                    //2;url.openconnection
                    conn = (HttpURLConnection) url.openConnection();
                    //3
                    conn.setRequestMethod("GET");
                    conn.setRequestProperty("Charset", "UTF-8");
                    conn.setReadTimeout(10 * 1000);
                    conn.setConnectTimeout(10 * 1000);
                    // 设置是否使用缓存  默认是true
                    conn.setUseCaches(true);
                    //设置本次连接是否自动处理重定向
                    conn.setInstanceFollowRedirects(true);
                    //urlConn设置请求头信息
                    //设置请求中的媒体类型信息。
                    conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
                    // 开始连接
                    conn.connect();
                    int code = conn.getResponseCode();
                    if (code == 200) {
                        InputStream is = conn.getInputStream();
                        result = getStringFromInputStream(is);
                    }
                    Message msg = Message.obtain();
                    msg.what = code;
                    msg.obj = result;
                    handler.sendMessage(msg);
                    Log.i("Http", result);

                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (in != null) {
                            in.close();
                        }
                        if (conn != null) {
                            conn.disconnect();
                        }
                    } catch (Exception e2) {
                        e2.printStackTrace();
                    }
                }
            }
        }).start();
    }

    public static void requestPost(final Handler handler, final String path, final Map<String, String> paramsMap, final int method) {
        //在子线程中操作网络请求
        new Thread(new Runnable() {
            @Override
            public void run() {
                BufferedReader in = null;
                String result = "";
                HttpURLConnection urlConn = null;
                try {
                    //合成参数
                    StringBuilder tempParams = new StringBuilder();
                    int pos = 0;
                    for (String key : paramsMap.keySet()) {
                        if (pos > 0) {
                            tempParams.append("&");
                        }
                        tempParams.append(String.format("%s=%s", key, paramsMap.get(key)));
                        pos++;
                    }
                    String params = tempParams.toString();
                    // 请求的参数转换为byte数组
                    byte[] postData = params.getBytes();
                    // 新建一个URL对象
                    URL url = new URL(path);
                    // 打开一个HttpURLConnection连接
                    urlConn = (HttpURLConnection) url.openConnection();
                    // 设置连接超时时间
                    urlConn.setConnectTimeout(5 * 1000);
                    //设置从主机读取数据超时
                    urlConn.setReadTimeout(5 * 1000);
                    // Post请求必须设置允许输出 默认false
                    urlConn.setDoOutput(true);
                    //设置请求允许输入 默认是true
                    urlConn.setDoInput(true);
                    // Post请求不能使用缓存
                    urlConn.setUseCaches(false);
                    // 设置为Post请求
                    urlConn.setRequestMethod("POST");

                    //设置本次连接是否自动处理重定向
                    urlConn.setInstanceFollowRedirects(true);
                    // 配置请求Content-Type
                    urlConn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");
                    // 开始连接
                    urlConn.connect();
                    // 发送请求参数
                    OutputStream out = urlConn.getOutputStream();// 获得一个输出流,向服务器写数据
                    out.write(postData);
                    out.flush();
                    out.close();

                    // 判断请求是否成功
                    int code = urlConn.getResponseCode();
                    if (code == 200) {
                        // 获取返回的数据
                        InputStream is = urlConn.getInputStream();
                        result = getStringFromInputStream(is);
                    }
                    Message msg = Message.obtain();
                    msg.what = code;
                    msg.arg1 = method;
                    msg.obj = result;
                    handler.sendMessage(msg);
                    Log.i("Http", result);

                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (in != null) {
                            in.close();
                        }
                        if (urlConn != null) {
                            // 关闭连接
                            urlConn.disconnect();
                        }
                    } catch (Exception e2) {
                        e2.printStackTrace();
                    }
                }
            }
        }).start();
    }

    private static String getStringFromInputStream(InputStream is)
            throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        // 模板代码 必须熟练
        byte[] buffer = new byte[1024];
        int len = -1;
        while ((len = is.read(buffer)) != -1) {
            os.write(buffer, 0, len);
        }
        is.close();
        String state = os.toString();// 把流中的数据转换成字符串,采用的编码是utf-8(模拟器默认编码)
        os.close();
        return state;
    }
}
