package com.campusboy.annotationtest;

import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.campusboy.annotations.DynamicIntentKey;
import com.campusboy.annotations.StaticIntentKey;
import com.campusboy.annotationtest.Utils.DynamicUtil;
import com.campusboy.annotationtest.Utils.StaticUtil;

public class Main2Activity extends AppCompatActivity {

    @DynamicIntentKey("dynamic_data")
    String dynamicData;
    @StaticIntentKey("static_data")
    String staticData;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getIntent().putExtra("dynamic_data", "动态注入");
        getIntent().putExtra("static_data", "静态注入");

        StaticUtil.inject(this);
        DynamicUtil.inject(this);
        TextView textHello = findViewById(R.id.text_hello);
        textHello.setText(dynamicData);
        textHello.append("\n");
        textHello.append(staticData + "");
    }
}
