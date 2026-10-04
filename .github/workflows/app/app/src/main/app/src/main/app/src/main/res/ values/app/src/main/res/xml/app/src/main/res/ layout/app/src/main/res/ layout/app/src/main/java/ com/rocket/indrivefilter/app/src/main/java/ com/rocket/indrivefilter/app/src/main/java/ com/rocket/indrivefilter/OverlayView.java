package com.rocket.indrivefilter;

import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Build;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

public class OverlayView {
    private static View view;
    private static WindowManager wm;

    public static void show(Context ctx, String text) {
        if (view != null) { update(text); return; }
        try {
            wm = (WindowManager) ctx.getSystemService(Context.WINDOW_SERVICE);
            view = LayoutInflater.from(ctx).inflate(R.layout.overlay, null);
            ((TextView) view.findViewById(R.id.tv)).setText(text);
            int type = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                : WindowManager.LayoutParams.TYPE_PHONE;
            WindowManager.LayoutParams lp = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                type,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                    | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                PixelFormat.TRANSLUCENT);
            lp.gravity = Gravity.TOP | Gravity.START;
            lp.x = 20; lp.y = 120;
            wm.addView(view, lp);
        } catch (Exception ignored) {}
    }

    public static void update(String text) {
        if (view == null) return;
        try { ((TextView) view.findViewById(R.id.tv)).setText(text); } catch (Exception ignored) {}
    }

    public static void hide(Context ctx) {
        try { if (view != null && wm != null) wm.removeView(view); } catch (Exception ignored) {}
        view = null; wm = null;
    }
}
