package com.nitrorush.ar;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import androidx.webkit.WebViewAssetLoader;

public class MainActivity extends Activity {
    private static final String GAME_URL = "https://appassets.androidplatform.net/assets/index.html";
    private WebView game;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        hideSystemBars();
        final WebViewAssetLoader assetLoader = new WebViewAssetLoader.Builder()
            .addPathHandler("/assets/", new WebViewAssetLoader.AssetsPathHandler(this))
            .build();

        game = new WebView(this);
        game.setBackgroundColor(0xFF070C1C);
        game.getSettings().setJavaScriptEnabled(true);
        game.getSettings().setDomStorageEnabled(true);
        game.getSettings().setAllowFileAccess(false);
        game.getSettings().setAllowContentAccess(false);
        game.getSettings().setMediaPlaybackRequiresUserGesture(false);
        game.getSettings().setSupportZoom(false);
        if (Build.VERSION.SDK_INT >= 26) {
            game.getSettings().setSafeBrowsingEnabled(true);
        }
        game.setWebChromeClient(new WebChromeClient());
        game.setWebViewClient(new WebViewClient() {
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                return assetLoader.shouldInterceptRequest(request.getUrl());
            }
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return !("appassets.androidplatform.net".equals(request.getUrl().getHost()));
            }
        });
        setContentView(game);
        game.loadUrl(GAME_URL);
    }

    private void hideSystemBars() {
        getWindow().getDecorView().setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            | View.SYSTEM_UI_FLAG_FULLSCREEN
            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
    }

    @Override
    public void onBackPressed() {
        if (game == null) {
            super.onBackPressed();
            return;
        }
        String script = "(function(){"
            + "function visible(id){var e=document.getElementById(id);return e&&!e.classList.contains('hidden');}"
            + "if(visible('pauseScreen')){document.getElementById('resumeBtn').click();return 'ok';}"
            + "if(visible('garage')||visible('settings')){document.querySelector('[data-back=home]').click();return 'ok';}"
            + "if(visible('finish')){document.getElementById('finishHome').click();return 'ok';}"
            + "if(visible('home')){return 'exit';}"
            + "var b=document.getElementById('pauseBtn');if(b){b.click();return 'ok';}"
            + "return 'exit';"
            + "})()";
        game.evaluateJavascript(script, result -> {
            if ("\"exit\"".equals(result)) {
                MainActivity.super.onBackPressed();
            }
        });
    }

    @Override
    protected void onPause() {
        if (game != null) {
            game.evaluateJavascript("window.dispatchEvent(new Event('blur'));", null);
            game.onPause();
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (game != null) game.onResume();
        hideSystemBars();
    }

    @Override
    protected void onDestroy() {
        if (game != null) {
            game.destroy();
            game = null;
        }
        super.onDestroy();
    }
}
