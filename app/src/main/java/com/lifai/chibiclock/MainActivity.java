package com.lifai.chibiclock;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.Toast;

/**
 * Full-screen kiosk wrapper around the local web clock.
 *
 * Keeps the screen awake, dims it on a night schedule, nudges the picture a
 * few pixels every minute (LCD/OLED burn-in) and opens the settings screen on
 * a long press or the back key.
 */
public class MainActivity extends Activity {

    private static final long SHIFT_EVERY_MS = 60L * 1000L;
    private static final long BRIGHT_EVERY_MS = 30L * 1000L;

    /** Translation steps in dp, cycled one per minute. */
    private static final int[][] SHIFTS = {
            {0, 0}, {7, 0}, {7, 6}, {0, 6}, {-7, 6}, {-7, 0}, {-7, -6}, {0, -6}
    };

    private WebView web;
    private Prefs prefs;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private int shiftStep = 0;
    private float overscanPx;

    private final Runnable shiftTask = new Runnable() {
        @Override public void run() {
            applyShift();
            handler.postDelayed(this, SHIFT_EVERY_MS);
        }
    };

    private final Runnable brightTask = new Runnable() {
        @Override public void run() {
            applyBrightness();
            handler.postDelayed(this, BRIGHT_EVERY_MS);
        }
    };

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        prefs = new Prefs(this);
        overscanPx = dp(14);

        FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(Color.BLACK);

        web = new WebView(this);
        web.setBackgroundColor(Color.BLACK);
        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setAllowFileAccess(true);
        s.setSupportZoom(false);
        s.setBuiltInZoomControls(false);
        s.setDisplayZoomControls(false);
        s.setUseWideViewPort(true);
        s.setLoadWithOverviewMode(true);
        s.setTextZoom(100);
        if (Build.VERSION.SDK_INT >= 17) {
            s.setMediaPlaybackRequiresUserGesture(false);
        }

        web.setVerticalScrollBarEnabled(false);
        web.setHorizontalScrollBarEnabled(false);
        web.setOverScrollMode(View.OVER_SCROLL_NEVER);
        web.setLongClickable(true);
        web.setOnLongClickListener(new View.OnLongClickListener() {
            @Override public boolean onLongClick(View v) {
                openSettings();
                return true;
            }
        });

        web.setWebViewClient(new WebViewClient() {
            @Override public void onPageFinished(WebView view, String url) {
                pushConfig();
            }

            @Override public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return true; // stay inside the kiosk
            }
        });

        // Slightly larger than the screen so the burn-in shift never shows an edge.
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(-1, -1);
        int neg = (int) -overscanPx;
        lp.setMargins(neg, neg, neg, neg);
        root.addView(web, lp);

        setContentView(root);
        web.loadUrl("file:///android_asset/index.html");
        hideSystemUi();
    }

    @Override
    protected void onResume() {
        super.onResume();
        hideSystemUi();
        applyBrightness();
        pushConfig();
        handler.removeCallbacks(shiftTask);
        handler.post(shiftTask);
        handler.removeCallbacks(brightTask);
        handler.post(brightTask);
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(shiftTask);
        handler.removeCallbacks(brightTask);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) hideSystemUi();
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onBackPressed() {
        openSettings();
    }

    private void openSettings() {
        Toast.makeText(this, R.string.hint_settings, Toast.LENGTH_SHORT).show();
        startActivity(new Intent(this, SettingsActivity.class));
    }

    private void pushConfig() {
        if (web == null) return;
        String js = "window.CC_SET && window.CC_SET(" + prefs.configJson() + ");";
        try {
            web.evaluateJavascript(js, null);
        } catch (Throwable ignored) {
        }
    }

    private void applyBrightness() {
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.screenBrightness = prefs.currentBrightness();
        getWindow().setAttributes(lp);
    }

    private void applyShift() {
        int[] step = SHIFTS[shiftStep % SHIFTS.length];
        shiftStep++;
        web.setTranslationX(dp(step[0]));
        web.setTranslationY(dp(step[1]));
    }

    private void hideSystemUi() {
        View decor = getWindow().getDecorView();
        decor.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    private float dp(float v) {
        return v * getResources().getDisplayMetrics().density;
    }

    private int dp(int v) {
        return Math.round(v * getResources().getDisplayMetrics().density);
    }
}
