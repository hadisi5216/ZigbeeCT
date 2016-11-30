package com.zhrcedu.zigbee.util;

import android.content.Context;

/**
 * Created by wugang on 2016/6/20.
 */

public class ToastUtil {
    /**
     * 显示toast
     *
     * @param context
     * @param text
     */
    private static android.widget.Toast mToast;

    public static void showToast(Context context, String text) {
        if (context != null && text != null) {
            try {
                if (mToast == null) {
                    mToast = android.widget.Toast.makeText(context, text, android.widget.Toast.LENGTH_SHORT);
                } else {
                    mToast.setText(text);
                    mToast.setDuration(android.widget.Toast.LENGTH_SHORT);
                }
                mToast.show();
            } catch (NullPointerException e) {
            }
        }
    }
}
