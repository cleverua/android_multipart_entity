package com.cleverua.android.helper;

import android.content.Context;
import android.widget.Toast;

public class ToastHelper {

    public static void showToast(Context context, CharSequence msg) {
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
    }
}
