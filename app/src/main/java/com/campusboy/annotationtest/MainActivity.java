package com.campusboy.annotationtest;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.campusboy.annotations.StringIntentKey;
import com.campusboy.annotationtest.Utils.StaticUtil;

public class MainActivity extends AppCompatActivity {

    @StringIntentKey("static_data")
    String staticData;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TextView textHello = findViewById(R.id.textHello);

        getIntent().putExtra("static_data", "静态注入");
        StaticUtil.inject(this);
        textHello.setText(staticData);

        findViewById(R.id.btnExchange).setOnClickListener(v -> startActivity(new Intent(this, Main2Activity.class)));
    }
}
