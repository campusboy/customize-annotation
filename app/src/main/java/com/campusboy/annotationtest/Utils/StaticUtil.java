package com.campusboy.annotationtest.Utils;

import android.app.Activity;

public class StaticUtil {
    public static void inject(Activity activity) {
        com.campusboy.annotationtest.StaticMapper.bind(activity);
    }
}
