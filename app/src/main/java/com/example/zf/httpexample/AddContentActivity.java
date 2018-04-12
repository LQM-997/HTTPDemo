package com.example.zf.httpexample;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

public class AddContentActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_content);
        Button btadd = (Button) findViewById(R.id.btconfirm);
        final EditText txtadd = (EditText) findViewById(R.id.txtaddcontent);
        btadd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!"".equals(txtadd.getText().toString())) {
                    Intent intent = new Intent();
                    intent.putExtra("data", txtadd.getText().toString());
                    setResult(2, intent);
                    finish();
                }
            }
        });
        Button btcancel = (Button) findViewById(R.id.btcancel);
        btcancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
