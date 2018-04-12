package com.example.zf.httpexample;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.lang.annotation.Target;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    private static final String UrlPath = "https://www.rxez8.com/stuapi/";
    private static final String Tag = "MainActivity";
    private static String token = "";
    private String secret = "App:Android:Api:Secret";
    private String StuNo = "";
    private Button btgettoken;
    //创建一个handler
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 200) {
                try {
                    Log.i(Tag, msg.obj.toString());
                    JSONObject jsonObject = new JSONObject(msg.obj.toString());
                    Log.i(Tag, jsonObject.get("data1").toString());
                    Log.i(Tag, jsonObject.get("data2").toString());
                    Log.i(Tag, jsonObject.get("data3").toString());
                    Log.i(Tag, jsonObject.get("data4").toString());
                    Log.i(Tag, jsonObject.get("data5").toString());
                    Log.i(Tag, jsonObject.get("data6").toString());
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    };

    private static class TokenHandler extends Handler {
        private final WeakReference<MainActivity> mActivity;

        public TokenHandler(MainActivity activity) {
            mActivity = new WeakReference<MainActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            MainActivity activity = mActivity.get();
            if (activity != null) {
                if (msg.what == 200) {
                    try {
                        Button btgettoken = (Button) activity.findViewById(R.id.btgetdata);
                        btgettoken.setEnabled(true);
                        btgettoken.setText(R.string.bttoken);
                        JSONObject jsonObject = new JSONObject(msg.obj.toString());
                        boolean success = Boolean.valueOf(jsonObject.get("success").toString());
                        if (success) {
                            token = jsonObject.get("token").toString();
                            Log.i(Tag, token);
                            Intent intent = new Intent(activity, ShowDataActivity.class);
                            intent.putExtra("token", token);
                            intent.putExtra("stuno", activity.StuNo);
                            activity.startActivity(intent);
                        } else {
                            Toast.makeText(activity, "获取Token失败！" + jsonObject.get("msg").toString(), Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }
    }

    private final TokenHandler tokenHandler = new TokenHandler(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btgettoken = (Button) findViewById(R.id.btgetdata);
        btgettoken.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = UrlPath + "gettoken";
                EditText txtstuno = (EditText) findViewById(R.id.txtstuno);
                if (!"".equals(txtstuno.getText().toString())) {
                    StuNo = txtstuno.getText().toString();
                    String timestamp = String.valueOf(System.currentTimeMillis());
                    String code = getAuth(timestamp, StuNo, secret);
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("stuno", StuNo);
                    params.put("timestamp", timestamp);
                    params.put("code", code);
                    try {
                        btgettoken.setEnabled(false);
                        btgettoken.setText(R.string.bttoken_gettoken);
                        HttpUtil.requestPost(tokenHandler, url, params, 1);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                } else {
                    AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
                    alertDialog.setTitle("提示");
                    alertDialog.setMessage("请输入学号");
                    alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });
                    alertDialog.show();
                }
            }
        });

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        tokenHandler.removeCallbacksAndMessages(null);
    }

    private String getAuth(String timeStamp, String key, String secret) {
        if (timeStamp == null || key == null || secret == null)
            return "";
        String[] data = {timeStamp, key, secret};
        Arrays.sort(data);
        String newtxt = data[0] + data[1] + data[2];
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("SHA-256");
            md.update(newtxt.getBytes("GBK"));
        } catch (NoSuchAlgorithmException nsae) {
            return "";
        } catch (UnsupportedEncodingException e) {
            return "";
        }
        StringBuffer sb = new StringBuffer();
        for (byte b : md.digest())
            sb.append(String.format("%02x", b));
        return sb.toString();
    }
}
