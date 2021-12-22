package com.campusboy.annotationtest;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.campusboy.annotations.DynamicIntentKey;
import com.campusboy.annotations.StaticIntentKey;
import com.campusboy.annotationtest.Utils.DynamicUtil;
import com.campusboy.annotationtest.Utils.StaticUtil;

public class MainActivity extends AppCompatActivity {

    @DynamicIntentKey("dynamic_data")
    String dynamicData;
    @StaticIntentKey("static_data")
    String staticData;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TextView textHello = findViewById(R.id.text_hello);

        getIntent().putExtra("dynamic_data", "动态注入");
        getIntent().putExtra("static_data", "静态注入");

        long start = System.currentTimeMillis();
        for (int index = 0; index < 1000; index++) {
            StaticUtil.inject(this);
        }
        long duration = System.currentTimeMillis() - start;
        textHello.setText(staticData + ": " + duration);

        start = System.currentTimeMillis();
        for (int index = 0; index < 1000; index++) {
            DynamicUtil.inject(this);
        }
        duration = System.currentTimeMillis() - start;

        textHello.append("\n");
        textHello.append(dynamicData + ": " + duration);
    }
}
