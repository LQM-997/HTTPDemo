package com.example.zf.httpexample;

import android.content.DialogInterface;
import android.content.Intent;
import android.nfc.cardemulation.CardEmulation;
import android.os.Handler;
import android.os.Message;
import android.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShowDataActivity extends AppCompatActivity {
    private String token = "";
    private static final String UrlPath = "https://www.rxez8.com/stuapi/";
    private String StuNo = "";
    private List<Map<String, Object>> itemlist = new ArrayList<Map<String, Object>>();
    private ListView listView;
    private SimpleAdapter simpleAdapter;
    private ImageButton btclear;
    private EditText txtsearch;
    private ImageButton btadd;
    private ImageButton btsearch;
    private View imgloading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_data);
        Intent intent = getIntent();
        token = intent.getStringExtra("token");
        StuNo = intent.getStringExtra("stuno");
        btadd = (ImageButton) findViewById(R.id.btadd);
        btclear = (ImageButton) findViewById(R.id.btclear);
        txtsearch = (EditText) findViewById(R.id.txtsearch);
        listView = (ListView) findViewById(R.id.showdata);
        btsearch = (ImageButton) findViewById(R.id.btsearch);
        imgloading = (View) findViewById(R.id.showloading);
        simpleAdapter = new SimpleAdapter(this,
                itemlist,
                R.layout.msg_item,
                new String[]{"msg", "time"},
                new int[]{R.id.showmsg, R.id.showtime});
        listView.setAdapter(simpleAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final HashMap<String, Object> item = (HashMap<String, Object>) parent.getItemAtPosition(position);// 获取选择项的值
                AlertDialog alertDialog = new AlertDialog.Builder(ShowDataActivity.this).create();
                alertDialog.setTitle("提示");
                alertDialog.setMessage("你确定要删除该条数据吗？");
                alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String url = UrlPath + "deldata";
                        Map<String, String> params = new HashMap<String, String>();
                        params.put("stuno", StuNo);
                        params.put("token", token);
                        params.put("id", item.get("id").toString());
                        try {
                            HttpUtil.requestPost(getDataHandler, url, params, 3);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                });
                alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                alertDialog.show();
            }
        });
        GetData("");
        btadd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent1 = new Intent(ShowDataActivity.this, AddContentActivity.class);
                startActivityForResult(intent1, 1);
            }
        });

        btsearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!"".equals(txtsearch.getText().toString())) {
                    GetData(txtsearch.getText().toString());
                    btclear.setVisibility(View.VISIBLE);
                    btadd.setVisibility(View.GONE);
                    btsearch.setEnabled(false);
                }
            }
        });
        btclear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GetData("");
                btclear.setVisibility(View.GONE);
                txtsearch.setText("");
                btadd.setVisibility(View.VISIBLE);
                btsearch.setEnabled(true);
            }
        });
    }

    private void GetData(String query) {
        imgloading.setVisibility(View.VISIBLE);
        if (itemlist != null) {
            itemlist.clear();
        }
        String url = UrlPath + "getdata";
        Map<String, String> params = new HashMap<String, String>();
        params.put("stuno", StuNo);
        params.put("token", token);
        params.put("query", query);
        params.put("start", "0");
        params.put("limit", "100");
        try {
            HttpUtil.requestPost(getDataHandler, url, params, 1);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    //异步处理数据的handler
    private static class GetDataHandler extends Handler {
        private final WeakReference<ShowDataActivity> mActivity;

        public GetDataHandler(ShowDataActivity activity) {
            mActivity = new WeakReference<ShowDataActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            ShowDataActivity activity = mActivity.get();
            if (activity != null) {
                if (msg.what == 200) {
                    if (msg.arg1 == 2) { //1:获取数据接口；2: 添加数据接口；3:删除数据接口
                        try {
                            JSONObject jsonObject = new JSONObject(msg.obj.toString());
                            boolean success = Boolean.valueOf(jsonObject.get("success").toString());
                            if (success) {
                                activity.GetData("");
                            } else {
                                Toast.makeText(activity, jsonObject.get("msg").toString(), Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    } else if (msg.arg1 == 1) {
                        activity.imgloading.setVisibility(View.GONE);
                        if (activity.itemlist != null) {
                            activity.itemlist.clear();
                        }
                        try {
                            JSONObject jsonObject = new JSONObject(msg.obj.toString());
                            int totalcount = Integer.valueOf(jsonObject.get("totalCount").toString());
                            JSONArray jsonArray = new JSONArray(jsonObject.get("data").toString());
                            if (jsonArray.length() > 0) {
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject jsonitem = new JSONObject(jsonArray.get(i).toString());
                                    Map<String, Object> map = new HashMap<String, Object>();
                                    map.put("id", jsonitem.get("data1").toString());
                                    map.put("msg", jsonitem.get("data2").toString());
                                    map.put("time", jsonitem.get("data3").toString());
                                    activity.itemlist.add(map);
                                }
                            }
                            activity.simpleAdapter.notifyDataSetChanged();
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    } else if (msg.arg1 == 3) {
                        try {
                            JSONObject jsonObject = new JSONObject(msg.obj.toString());
                            boolean success = Boolean.valueOf(jsonObject.get("success").toString());
                            if (success) {
                                activity.GetData("");
                            } else {
                                Toast.makeText(activity, jsonObject.get("msg").toString(), Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    private final GetDataHandler getDataHandler = new GetDataHandler(this);


    // 为了获取结果
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // RESULT_OK，判断另外一个activity已经结束数据输入功能，Standard activity result:
        // operation succeeded. 默认值是-1
        if (resultCode == 2) {
            if (requestCode == 1) {
                String url = UrlPath + "insertdata";
                String msg = data.getStringExtra("data");
                Map<String, String> params = new HashMap<String, String>();
                params.put("stuno", StuNo);
                params.put("token", token);
                params.put("msg", msg);
                try {
                    HttpUtil.requestPost(getDataHandler, url, params, 2);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

            }
        }
    }
}
