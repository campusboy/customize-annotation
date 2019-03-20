package com.campusboy.annotationtest.Utils;

import android.app.Activity;
import android.content.Intent;

import com.campusboy.annotations.DynamicIntentKey;

import java.io.Serializable;
import java.lang.reflect.Field;

public class DynamicUtil {
    public static void inject(Activity activity) {
        Intent intent = activity.getIntent();
        // 反射
        for (Field field : activity.getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(DynamicIntentKey.class)) {

                // 获取注解
                DynamicIntentKey annotation = field.getAnnotation(DynamicIntentKey.class);
                String intentKey = annotation.value();

                // 读取实际的IntentExtra值
                Serializable serializable = intent.getSerializableExtra(intentKey);

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
