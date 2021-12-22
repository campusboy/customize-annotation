package com.campusboy.annotationtest;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.campusboy.annotations.StringIntentKey;
import com.campusboy.annotationtest.Utils.DynamicUtil;

public class Main2Activity extends AppCompatActivity {

    @StringIntentKey("dynamic_data")
    String dynamicData;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getIntent().putExtra("dynamic_data", "动态注入");
        DynamicUtil.inject(this);

        TextView textHello = findViewById(R.id.textHello);
        textHello.setText(dynamicData);

        findViewById(R.id.btnExchange).setOnClickListener(v -> startActivity(new Intent(this, MainActivity.class)));
    }
}
