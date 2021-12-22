package com.campusboy.annotationtest.Utils;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import com.campusboy.annotations.StringIntentKey;

import java.io.Serializable;
import java.lang.reflect.Field;

public class DynamicUtil {
    public static void inject(Activity activity) {
        Intent intent = activity.getIntent();
        // 反射
        for (Field field : activity.getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(StringIntentKey.class)) {
                // 获取注解
                StringIntentKey annotation = field.getAnnotation(StringIntentKey.class);
                String intentKey = annotation.value();
                Log.d("DynamicUtil", "intentKey = " + intentKey);
                // 读取实际的IntentExtra值
                Serializable serializable = intent.getSerializableExtra(intentKey);
                Log.d("serializable", "serializable = " + serializable);
                if (serializable == null) {

                    if (field.getType().isAssignableFrom(String.class)) {
                        serializable = "";
                    }
                }

                try {
                    // 插入值
                    boolean accessible = field.isAccessible();
                    field.setAccessible(true);
                    field.set(activity, serializable);
                    field.setAccessible(accessible);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
